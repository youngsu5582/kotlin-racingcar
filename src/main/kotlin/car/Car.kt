package car

import mu.KotlinLogging

data class Car(
    private val name: String,
    private val engine: Engine,
    private val position: Position,
) {
    private val log = KotlinLogging.logger {}

    constructor(name: String) : this(name, ThresholdEngine(RandomPowerGenerator()), Position(0))

    fun move() = engine.move()
        .also { moved ->
            if (moved) {
                position.forward()
                log.debug { "$name moved success! (position: $position)" }
            }
        }
        .let { current() }

    fun current() = CarHistory(name, position.toInt())
}