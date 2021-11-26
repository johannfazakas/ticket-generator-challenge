package com.challenge.bingo.generator

import java.util.Random

private const val TICKETS = 6
private const val COLUMNS = 9
private const val TICKET_ROWS = 3
private const val STRIP_ROWS = TICKETS * TICKET_ROWS
private const val NUMBERS_PER_ROW = 5
private const val NUMBERS_PER_TICKET = TICKET_ROWS * NUMBERS_PER_ROW

private val NUMBERS_BY_COLUMN = mapOf(
    0 to (1..9),
    1 to (10..19),
    2 to (20..29),
    3 to (30..39),
    4 to (40..49),
    5 to (50..59),
    6 to (60..69),
    7 to (70..79),
    8 to (80..90),
).mapValues { it.value.map(::Number) }

private fun stripRowIndex(ticketIndex: Int, ticketRowIndex: Int): Int = ticketIndex * TICKET_ROWS + ticketRowIndex
private fun ticketIndex(stripRowIndex: Int): Int = stripRowIndex / TICKET_ROWS
private fun ticketRowIndex(stripRowIndex: Int): Int = stripRowIndex % TICKET_ROWS
private fun columnsRemaining(columnIndex: Int): Int = COLUMNS - columnIndex - 1

class Strip private constructor() {

    private val _tickets = Array(TICKETS) { Ticket() }

    val tickets: List<Ticket>
        get() = _tickets.toList()

    private val filledByStripRow = Array(STRIP_ROWS) { 0 }
    private val filledByTicketColumn = Array(TICKETS) { Array(COLUMNS) { 0 } }
    private val remainingByStripColumn = Array(COLUMNS) { column -> NUMBERS_BY_COLUMN.getValue(column).size }

    private fun canInsert(stripRowIndex: Int, columnIndex: Int): Boolean =
        _tickets[ticketIndex(stripRowIndex)].canInsert(ticketRowIndex(stripRowIndex), columnIndex) &&
            validateEmptyTicketsCanBeFilled(ticketIndex(stripRowIndex), columnIndex)

    private fun insertSorted(number: Number, stripRowIndex: Int, columnIndex: Int) {
        val ticketIndex = ticketIndex(stripRowIndex)
        val ticketRowIndex = ticketRowIndex(stripRowIndex)

        _tickets[ticketIndex].insertSorted(number, ticketRowIndex, columnIndex)

        filledByStripRow[stripRowIndex(ticketIndex, ticketRowIndex)]++
        filledByTicketColumn[ticketIndex][columnIndex]++
        remainingByStripColumn[columnIndex]--
    }

    private fun getMandatoryRowsByColumn(columnIndex: Int): List<Int> =
        (0 until STRIP_ROWS).filter { filledByStripRow[it] + columnsRemaining(columnIndex) < NUMBERS_PER_ROW }

    private fun validateEmptyTicketsCanBeFilled(ticketIndex: Int, columnIndex: Int): Boolean {
        val emptyExclusiveTickets = (0 until TICKETS)
            .filter { it != ticketIndex }
            .count { filledByTicketColumn[it][columnIndex] == 0 }
        return remainingByStripColumn[columnIndex] - emptyExclusiveTickets > 0
    }

    override fun toString(): String = _tickets
        .mapIndexed { index, ticket -> "Ticket ${index + 1}:\n$ticket" }
        .joinToString(separator = "") { it }

    object Factory {
        fun generate(count: Int, randomGenerator: Random): List<Strip> =
            List(count) { generate(randomGenerator) }

        private fun generate(randomGenerator: Random): Strip =
            Strip().also { (0 until COLUMNS).forEach { column -> fillColumn(it, column, randomGenerator) } }

        private fun fillColumn(strip: Strip, column: Int, randomGenerator: Random) {
            val columnNumbers = NUMBERS_BY_COLUMN.getValue(column).toMutableList()
            val stripRowsToFill = MutableList(STRIP_ROWS) { index -> index }

            strip.getMandatoryRowsByColumn(column).forEach { stripRowIndex ->
                val number = columnNumbers[randomGenerator.nextInt(columnNumbers.size)]
                strip.insertSorted(number, stripRowIndex, column)
                stripRowsToFill.remove(stripRowIndex)
                columnNumbers.remove(number)
            }

            columnNumbers.forEach { number ->
                var inserted = false
                while (!inserted) {
                    val stripRowIndex = stripRowsToFill[randomGenerator.nextInt(stripRowsToFill.size)]
                    stripRowsToFill.remove(stripRowIndex)
                    if (strip.canInsert(stripRowIndex, column)) {
                        strip.insertSorted(number, stripRowIndex, column)
                        inserted = true
                    }
                }
            }
        }
    }
}

class Ticket internal constructor() {

    private val _rows = Array(TICKET_ROWS) { Array<Square>(COLUMNS) { Blank } }

    val rows: List<List<Square>>
        get() = _rows.map { it.toList() }

    val columns: List<List<Square>>
        get() = (0 until COLUMNS).map { column -> List(TICKET_ROWS) { row -> getSquare(row, column) } }

    private val filledByRow = Array(TICKET_ROWS) { 0 }
    private val filledByColumn = Array(COLUMNS) { 0 }
    private var filledSquares = 0

    fun getSquare(rowIndex: Int, columnIndex: Int) = _rows[rowIndex][columnIndex]

    fun canInsert(rowIndex: Int, columnIndex: Int): Boolean {
        return validatePositionIsEmpty(rowIndex, columnIndex) &&
            validateRowLimitNotReached(rowIndex) &&
            validateSquaresLimitWouldNotBeExceeded(columnIndex)
    }

    fun insertSorted(number: Number, rowIndex: Int, columnIndex: Int) {
        _rows[rowIndex][columnIndex] = number

        filledByRow[rowIndex]++
        filledByColumn[columnIndex]++
        filledSquares++

        ensureColumnOrder(columnIndex)
    }

    private fun ensureColumnOrder(columnIndex: Int) {
        if (filledByColumn[columnIndex] <= 1) {
            return
        }
        val numberRowIndexes = (0 until TICKET_ROWS).filter { getSquare(it, columnIndex) is Number }

        for (outer in 1 until numberRowIndexes.size) {
            val key = getSquare(numberRowIndexes[outer], columnIndex) as Number
            var inner = outer
            while (inner > 0 && getSquare(numberRowIndexes[inner - 1], columnIndex) as Number > key) {
                _rows[numberRowIndexes[inner]][columnIndex] = _rows[numberRowIndexes[inner - 1]][columnIndex]
                inner--
            }
            _rows[numberRowIndexes[inner]][columnIndex] = key
        }
    }

    private fun validatePositionIsEmpty(rowIndex: Int, columnIndex: Int): Boolean =
        getSquare(rowIndex, columnIndex) is Blank

    private fun validateRowLimitNotReached(rowIndex: Int): Boolean =
        filledByRow[rowIndex] < NUMBERS_PER_ROW

    private fun validateSquaresLimitWouldNotBeExceeded(columnIndex: Int): Boolean =
        filledSquares + columnsRemaining(columnIndex) < NUMBERS_PER_TICKET

    override fun toString() = _rows
        .asSequence()
        .map { it.joinToString(separator = "\t", postfix = "\n", transform = Square::toString) }
        .joinToString(separator = "") { it }
}

sealed class Square

object Blank : Square() {
    override fun toString(): String = "X"
}

data class Number(val value: Int) : Square(), Comparable<Number> {
    override fun compareTo(other: Number): Int = value.compareTo(other.value)
    override fun toString(): String = value.toString()
}

