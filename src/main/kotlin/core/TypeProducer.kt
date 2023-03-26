package core

import types.ReturnType

open class TypeProducer<T, U: ReturnType<T>>(private val dummy: U) {
    fun createReference(reference: String): U = dummy.createReference(reference) as U
}