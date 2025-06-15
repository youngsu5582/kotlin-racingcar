package race

data class RaceCount(
    private var startCount: Int = 0,
    private val endCount: Int
) {
    companion object {
        private const val MIN_VALUE = 1
        private const val MAX_VALUE = Int.MAX_VALUE
    }

    constructor(endCount: Int) : this(0, endCount)

    constructor(value: Long) : this(
        endCount = if (value in MIN_VALUE..MAX_VALUE) {
            value.toInt()
        } else {
            throw IllegalArgumentException(
                "value must be in $MIN_VALUE ~ $MAX_VALUE (was $value)"
            )
        }
    )

    init {
        require(endCount in MIN_VALUE..MAX_VALUE) {
            "value must be in $MIN_VALUE ~ $MAX_VALUE (was $endCount)"
        }
        require(startCount <= endCount) {
            "startCount must be smaller than endCount. (startCount: $startCount, endCount: $endCount)"
        }
    }

    fun progress(): Boolean =
        if (startCount >= endCount) false
        else {
            startCount++
            true
        }

    fun toInt() = startCount
}

