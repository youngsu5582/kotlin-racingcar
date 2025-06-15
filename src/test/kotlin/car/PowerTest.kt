package car

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PowerTest {
    @Test
    fun `power must be positive number`() {
        assertThrows<IllegalArgumentException> {
            Power(-1)
        }
    }
}