package race

import car.CarHistory

data class RaceHistory(
    val finished: Boolean = false,
    val raceIndex: Int,
    val carHistory: List<CarHistory>
) {
    fun winner(): List<CarHistory> {
        if (carHistory.isEmpty()) return emptyList()
        // maxOf would raise NoSuchElementException when collection is empty
        val maxPosition = carHistory.maxOf { it.position }
        return carHistory.filter { it.position == maxPosition }
    }

    fun isFinished() = finished
}