package car

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test

class PositionTest {

    @Test
    fun `can't move backward when position is zero`() {
        val position = Position(0)
        assertThrows<IllegalStateException> {
            position.backward()
        }
    }

    @Test
    fun `forward return next Position`() {
        val position = Position(1)
        val forwardPosition = position.forward()
        assertThat(forwardPosition.toInt()).isEqualTo(2)
    }

    @Test
    fun `position first class object`() {
        val position = Position(1)
        assertThat(position).isEqualTo(Position(1))
    }
}
