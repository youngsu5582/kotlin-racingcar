package view

import car.Car
import car.Cars
import race.RaceCount

object InputView {
    private val SCANNER = System.`in`.bufferedReader()

    fun readCars(): Cars {
        println("Please Input Car Names (EX: Car1,Car2,Car3)")
        val input = SCANNER.readLine()
        return Cars(input.split(",").map { Car(it) })
    }

    fun readRaceCount(): RaceCount {
        println("Please Input Race Count (EX: 5)")
        val input = SCANNER.readLine()
        return RaceCount(input.toInt())
    }
}