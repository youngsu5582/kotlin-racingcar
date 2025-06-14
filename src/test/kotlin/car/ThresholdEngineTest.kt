package car

import config.ListPowerGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ThresholdEngineTest {
    companion object {
        @JvmStatic
        fun power_and_move(): Stream<Arguments> = Stream.of(
            Arguments.of(5, true),
            Arguments.of(4, false)
        )
    }

    @ParameterizedTest(name = "if power {0}, move {1}")
    @MethodSource("power_and_move")
    fun `move returns expected based on power`(power: Int, expected: Boolean) {
        val engine = ThresholdEngine(
            ListPowerGenerator(power)
        )
        val result = engine.move()

        assertThat(result).isEqualTo(expected)
    }
}
