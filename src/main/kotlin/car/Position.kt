package car

data class Position(
    private val value: Int
) {
    constructor() : this(0)

    companion object {
        private const val MIN_POS: Int = 0
    }

    fun forward() = Position(value + 1)

    fun backward() = check(value - 1 >= MIN_POS) {
        throw IllegalStateException("car.Position is greater than: $MIN_POS")
    }.run { Position(value - 1) }

    fun toInt() = value
}