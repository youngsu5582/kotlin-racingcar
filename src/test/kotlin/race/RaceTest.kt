package race

import car.Car
import car.Cars
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RaceTest {

    @Test
    fun `race progresses until race count ends`() {
        val race = Race(
            cars = Cars(listOf(Car("A"))),
            raceCount = RaceCount(0, 1)
        )
        val history = race.progress()
        assertThat(history.finished).isFalse()
        assertThat(history.raceIndex).isOne()
    }

    @Test
    fun `race finishes when race count reaches the end`() {
        val race = Race(
            cars = Cars(listOf(Car("A"))),
            raceCount = RaceCount(1, 1)
        )
        val history = race.progress()
        assertThat(history.finished).isTrue()
        assertThat(history.raceIndex).isOne()
    }
}