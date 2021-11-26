package com.challenge.bingo.generator

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isIn
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
            .filterIsInstance<Number>()
            .map { it.value }
            .toSet()
        assertThat(distinctNumbersInStrip, hasSize(90))
        distinctNumbersInStrip.forEach { assertThat(it, isIn((1..90).toList())) }
    }

    @Test
    fun `should generate strip with ticket rows containing 5 numbers and 4 blanks`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        val rows = strip.tickets.flatMap { it.rows }
        rows.forEach { row ->
            assertThat(row.filterIsInstance<Blank>(), hasSize(4))
            assertThat(row.filterIsInstance<Number>(), hasSize(5))
        }
    }

    @Test
    fun `should generate strip with ticket columns with size from 1 to 3`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        val columns = strip.tickets.flatMap { it.columns }
        columns.forEach { column ->
            assertThat(column.count { it is Number }, isIn((1..3).toList()))
        }
    }

    @Test
    fun `should generate strip with columns containing the correct numbers`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        strip.tickets.forEach { ticket ->
            ticket.columns.forEachIndexed { column, values ->
                when (column) {
                    0 -> values.filterIsInstance<Number>().map { it.value }
                        .forEach { assertThat(it, isIn((1..9).toList())) }
                    1 -> values.filterIsInstance<Number>().map { it.value }
                        .forEach { assertThat(it, isIn((10..19).toList())) }
                    2 -> values.filterIsInstance<Number>().map { it.value }
                        .forEach { assertThat(it, isIn((20..29).toList())) }
                    3 -> values.filterIsInstance<Number>().map { it.value }
                        .forEach { assertThat(it, isIn((30..39).toList())) }
                    4 -> values.filterIsInstance<Number>().map { it.value }
                        .forEach { assertThat(it, isIn((40..49).toList())) }
                    5 -> values.filterIsInstance<Number>().map { it.value }
                        .forEach { assertThat(it, isIn((50..59).toList())) }
                    6 -> values.filterIsInstance<Number>().map { it.value }
                        .forEach { assertThat(it, isIn((60..69).toList())) }
                    7 -> values.filterIsInstance<Number>().map { it.value }
                        .forEach { assertThat(it, isIn((70..79).toList())) }
                    8 -> values.filterIsInstance<Number>().map { it.value }
                        .forEach { assertThat(it, isIn((80..90).toList())) }
                }
            }
        }
    }

    @Test
    fun `should generate strip with tickets containing column values sorted ascending`() {
        val strip = Strip.Factory.generate(1, randomGenerator)[0]

        strip.tickets.forEach { ticket ->
            ticket.columns.forEach { column ->
                val columnNumbers = column.filterIsInstance<Number>()
                assertThat(columnNumbers, `is`(columnNumbers.sorted()))
            }
        }
    }

    // it takes about 350ms on my local
    @Test
    fun `should generate 10k strips in 1 second`() {
        assertTimeout(Duration.ofSeconds(1)) { Strip.Factory.generate(10_000, randomGenerator) }
    }
}