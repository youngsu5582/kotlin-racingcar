package car

data class Position(
    private var value: Int
) {
    constructor() : this(0)

    companion object {
        private const val MIN_POS: Int = 0
    }

    fun forward() = run { value += 1 }

    fun backward() = check(value - 1 >= MIN_POS) { "car.Position is greater than: $MIN_POS" }
        .also { value -= 1 }

    fun toInt() = value
}