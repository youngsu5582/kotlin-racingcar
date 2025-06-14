package config

import car.Engine

class FixedEngine(private val flag: Boolean) : Engine {
    override fun move(): Boolean {
        return flag
    }
}