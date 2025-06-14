package car

import mu.KotlinLogging

class ThresholdEngine(
    private val powerGenerator: PowerGenerator
) : Engine {

    private val log = KotlinLogging.logger {}

    companion object {
        const val THRESHOLD = 5
    }

    override fun move(): Boolean {
        val power = powerGenerator.generate()
        if (power.isGreat(THRESHOLD)) {
            log.debug { "$power is over than threshold($THRESHOLD)." }
            return true
        }
        return false
    }
}