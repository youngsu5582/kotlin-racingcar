package race

import car.Cars

class Race(
    private val cars: Cars,
    private val raceCount: RaceCount,
) {

    fun progress(): RaceHistory {
        if (raceCount.progress()) {
            return RaceHistory(
                finished = false,
                raceIndex = raceCount.toInt(),
                carHistory = cars.move()
            )
        }
        return RaceHistory(
            finished = true,
            raceIndex = raceCount.toInt(),
            carHistory = cars.current()
        )
    }
}