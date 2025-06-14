package config

import car.Power
import car.PowerGenerator

/**
 * PowerGenerator For Testing
 * construct with int varargs
 * return each Power element with int value
 */
class ListPowerGenerator(
    private val value: List<Int>
) : PowerGenerator {
    private var index = 0

    constructor(vararg values: Int) : this(values.toList())

    override fun generate(): Power {

        // Caller must ensure enough values are provided.
        return Power(value[index++])
    }
}