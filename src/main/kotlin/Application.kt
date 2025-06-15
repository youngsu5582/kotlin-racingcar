import race.Race
import view.InputView.readCars
import view.InputView.readRaceCount
import view.OutputView.printRaceHistory

fun main() {
    val cars = readCars()
    val raceCount = readRaceCount()
    val race = Race(cars, raceCount)

    while (true) {
        val history = race.progress()
        printRaceHistory(history)
        if (history.isFinished()) break
    }
}