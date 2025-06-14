package car

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
}
