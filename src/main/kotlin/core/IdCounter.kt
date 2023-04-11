package core

object IdCounter {
    private var id: Long = 1
    fun next() = id++
}