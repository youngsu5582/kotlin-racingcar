package car

data class Power(val power: Int) {
    init {
        require(power >= 0) { "Power must be non-negative power:$power" }
    }

    fun isGreat(value: Int): Boolean = power >= value
}