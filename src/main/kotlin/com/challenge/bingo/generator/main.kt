package com.challenge.bingo.generator

import java.time.Duration
import java.time.Instant
import java.util.Random

const val SILENT_ARG = "silent"
const val DEFAULT_COUNT = 1

fun main(args: Array<String>) {
    val count = getCount(args)
    val start = Instant.now()

    val strips = Strip.Factory.generate(count, Random())

    val end = Instant.now()
    val duration = Duration.between(start, end)
    if (isLoud(args)) {
        strips.forEachIndexed { index, strip -> println("Strip ${index + 1}:\n$strip\n${"-".repeat(80)}") }
    }
    println("$count strip${if (count > 1) "s were" else " was"} generated in ${duration.toMillis()}ms")
}

private fun getCount(args: Array<String>): Int = args
    .asSequence()
    .mapNotNull { it.toIntOrNull() }
    .filter { it > 0 }
    .firstOrNull() ?: DEFAULT_COUNT

private fun isLoud(args: Array<String>): Boolean = SILENT_ARG !in args
