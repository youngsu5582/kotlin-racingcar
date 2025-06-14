package car

import config.FixedEngine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CarTest {

    companion object {
        @JvmStatic
        fun flag_and_position(): Stream<Arguments> = Stream.of(
            Arguments.of(true, 1),
            Arguments.of(false, 0)
        )
    }

    @ParameterizedTest
    @DisplayName("car moved with engiene")
    @MethodSource("flag_and_position")
    fun `moved with Engine`(flag: Boolean, position: Int) {
        val car = Car("car", FixedEngine(flag), Position(0))
        val result = car.move()
        assertThat(result.carPosition).isEqualTo(position)
    }
}

