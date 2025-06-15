package config

import car.CarHistory

fun generateCarHistory(carName: String) = CarHistory(carName, 0)
fun generateCarHistory(carName: String, position: Int) = CarHistory(carName, position)