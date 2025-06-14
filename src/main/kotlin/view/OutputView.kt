package view

import car.CarHistory
import race.RaceHistory

object OutputView {
    fun printRaceHistory(raceHistory: RaceHistory) {
        if (raceHistory.isFinished()) {
            printWithPadding {
                println("Race is Ended!")
                println("Race Winner is ====================")
                printCarHistory(raceHistory.winner())
            }
        } else {
            printWithPadding {
                println("Race is process. round: ${raceHistory.raceIndex}")
                printCarHistory(raceHistory.carHistory)
            }
        }
    }

    private fun printCarHistory(carHistories: List<CarHistory>) {
        carHistories.forEach {
            println("${it.carName} : ${it.carPosition}")
        }
    }

    private fun printWithPadding(temp: () -> Unit) {
        padding()
        temp.invoke()
        padding()
    }


    private fun padding() = println("\n")
}