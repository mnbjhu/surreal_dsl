package core

sealed interface Linked<T> {
    data class Reference<T>(val name: String): Linked<T>
    data class Actual<T>(val record: T): Linked<T>
}