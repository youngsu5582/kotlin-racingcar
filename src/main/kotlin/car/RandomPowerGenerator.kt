package car

import kotlin.random.Random

class RandomPowerGenerator : PowerGenerator {
    private val random = Random.Default

    companion object {
        const val MIN_POWER = 0
        const val MAX_POWER = 10
    }

    override fun generate(): Power {
        return Power(random.nextInt(MIN_POWER, MAX_POWER))
    }
}