package types

import FieldDefinition
import kotlinx.serialization.KSerializer

interface ReturnType<T> {
    val serializer: KSerializer<T>
    val reference: String
    fun createReference(reference: String): ReturnType<T>
    fun getFieldTypeBounds(): Map<String, String>
}