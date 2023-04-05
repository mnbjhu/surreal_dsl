package core

import FieldSettings
import types.ReturnType

open class TypeProducer<T, U: ReturnType<T>>(private val dummy: U) {
    var settings: FieldSettings? = null
    fun createReference(reference: String): U = dummy.createReference(reference) as U
}