package car

data class Cars(
    private val cars: List<Car>
) {

    fun move() = cars.map { it.move() }

    fun current() = cars.map { it.current() }
}