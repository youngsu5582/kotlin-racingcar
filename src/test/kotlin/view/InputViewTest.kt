package view

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import java.io.ByteArrayInputStream
import java.io.InputStream

class InputViewTest {
    private lateinit var originalSystemIn: InputStream

    @BeforeEach
    fun setUp() {
        originalSystemIn = System.`in`
    }

    @AfterEach
    fun tearDown() {
        System.setIn(originalSystemIn)
    }

    @Test
    fun `readRaceCount throws exception when input is null (EOF)`() {
        // Given - simulate EOF by providing empty input stream
        val emptyInput = ByteArrayInputStream(byteArrayOf())
        System.setIn(emptyInput)

        // When & Then
        assertThatThrownBy {
            InputView.readRaceCount()
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `readRaceCount throws exception when input is not integer`() {
        // Given
        val invalidInput = "not_a_number\n"
        System.setIn(ByteArrayInputStream(invalidInput.toByteArray()))

        // When & Then
        assertThatThrownBy {
            InputView.readRaceCount()
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `readRaceCount throws exception when input is empty string`() {
        // Given
        val emptyStringInput = "\n"
        System.setIn(ByteArrayInputStream(emptyStringInput.toByteArray()))

        // When & Then
        assertThatThrownBy {
            InputView.readRaceCount()
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `readRaceCount works correctly with valid input`() {
        // Given
        val validInput = "5\n"
        System.setIn(ByteArrayInputStream(validInput.toByteArray()))

        // When
        val result = InputView.readRaceCount()

        // Then
        assertThat(result.toInt()).isEqualTo(0) // startCount should be 0
    }

    @Test
    fun `readCars throws exception when input is null`() {
        // Given
        val emptyInput = ByteArrayInputStream(byteArrayOf())
        System.setIn(emptyInput)

        // When & Then
        assertThatThrownBy {
            InputView.readCars()
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}