package com.lindar.challenge.bingo

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isIn
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.time.Duration
import java.util.Random

internal class StripFactoryTest {

    private val randomGenerator = Random()

    @Test
    fun `should generate strip with 6 tickets`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        assertThat(strip.tickets, hasSize(6))
    }

    @Test
    fun `should generate strip with tickets containing 3 rows and 9 columns`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        strip.tickets.forEach { ticket ->
            assertThat(ticket.rows, hasSize(3))
            ticket.rows.forEach { assertThat(it, hasSize(9)) }
            assertThat(ticket.columns, hasSize(9))
            ticket.columns.forEach { assertThat(it, hasSize(3)) }
        }
    }

    @Test
    fun `should generate strip containing all 90 unique numbers`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        val distinctNumbersInStrip = strip.tickets
            .flatMap { it.rows }
            .flatten()
            .mapNotNull(Square::number)
            .toSet()
        assertThat(distinctNumbersInStrip, hasSize(90))
        distinctNumbersInStrip.forEach { assertThat(it, isIn((1..90).toList())) }
    }

    @Test
    fun `should generate strip with ticket rows containing 5 numbers and 4 blanks`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        val rows = strip.tickets.flatMap { it.rows }
        rows.forEach { row ->
            assertThat(row.filter(Square::isEmpty), hasSize(4))
            assertThat(row.filter(Square::isFilled), hasSize(5))
        }
    }

    @Test
    fun `should generate strip with ticket columns with size from 1 to 3`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        val columns = strip.tickets.flatMap { it.columns }
        columns.forEach { column ->
            assertThat(column.count(Square::isFilled), isIn((1..3).toList()))
        }
    }

    @Test
    fun `should generate strip with columns containing the correct numbers`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        strip.tickets.forEach { ticket ->
            ticket.columns.forEachIndexed { column, values ->
                when (column) {
                    0 -> values.mapNotNull(Square::number).forEach { assertThat(it, isIn((1..9).toList())) }
                    1 -> values.mapNotNull(Square::number).forEach { assertThat(it, isIn((10..19).toList())) }
                    2 -> values.mapNotNull(Square::number).forEach { assertThat(it, isIn((20..29).toList())) }
                    3 -> values.mapNotNull(Square::number).forEach { assertThat(it, isIn((30..39).toList())) }
                    4 -> values.mapNotNull(Square::number).forEach { assertThat(it, isIn((40..49).toList())) }
                    5 -> values.mapNotNull(Square::number).forEach { assertThat(it, isIn((50..59).toList())) }
                    6 -> values.mapNotNull(Square::number).forEach { assertThat(it, isIn((60..69).toList())) }
                    7 -> values.mapNotNull(Square::number).forEach { assertThat(it, isIn((70..79).toList())) }
                    8 -> values.mapNotNull(Square::number).forEach { assertThat(it, isIn((80..90).toList())) }
                }
            }
        }
    }

    @Disabled
    @Test
    fun `should generate strip with tickets containing column values sorted ascending`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        strip.tickets.forEach { ticket ->
            ticket.columns.forEach { column ->
                val columnNumbers = column.mapNotNull(Square::number)
                assertThat(columnNumbers, `is`(columnNumbers.sorted()))
            }
        }
    }

    @Test
    fun `should generate 10k strips in 1 second`() {
        assertTimeout(Duration.ofSeconds(1)) { Strip.Factory.generate(10_000, randomGenerator)[0] }
    }
}