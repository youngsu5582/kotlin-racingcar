package config

import car.Power
import car.PowerGenerator

class ListPowerGenerator(
    private val value: List<Int>
) : PowerGenerator {
    private var index = 0

    constructor(vararg values: Int) : this(values.toList())

    override fun generate(): Power {
        return Power(value[index++])
    }
}