package car

data class Power(val power: Int) {
    fun isGreat(value: Int): Boolean = power >= value
}