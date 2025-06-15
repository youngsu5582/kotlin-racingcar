package race

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RaceCountTest {
    @Test
    fun `retry count must be greater than 0`() {
        assertThrows<IllegalArgumentException> {
            RaceCount(0)
        }
    }

    @Test
    fun `can't accept long numeric range`() {
        assertThrows<IllegalArgumentException> {
            val value = Int.MAX_VALUE.toLong() + 1
            RaceCount(value)
        }
    }

    @Test
    fun `startPosition must be smaller than endPosition`() {
        assertThrows<IllegalArgumentException> {
            RaceCount(2, 1)
        }
    }
}