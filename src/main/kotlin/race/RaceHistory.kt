package race

import car.CarHistory

data class RaceHistory(
    val finished: Boolean = false,
    val raceIndex: Int,
    val carHistory: List<CarHistory>
) {
    fun winner(): List<CarHistory> {
        val maxPosition = carHistory.maxOf { it.carPosition }
        return carHistory.filter { it.carPosition == maxPosition }
    }

    fun isFinished() = finished
}