package com.challenge.bingo.generator

import java.util.Random

fun main() {
    println(Strip.Factory.generate(1, Random())[0])
}