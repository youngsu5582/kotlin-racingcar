package race

import config.generateCarHistory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RaceHistoryTest {
    @Test
    fun `winners calculated with position`() {
        val history = RaceHistory(
            finished = true,
            raceIndex = 1,
            carHistory = listOf(
                generateCarHistory("A", 1),
                generateCarHistory("B", 2),
                generateCarHistory("C", 2)
            )
        )
        val winner = history.winner()
        assertThat(winner).hasSize(2)
            .extracting<String> { it.carName }
            .containsExactly("B", "C");
    }
}