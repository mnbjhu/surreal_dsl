package types

import kotlinx.serialization.KSerializer

interface ReturnType<T> {
    val serializer: KSerializer<T>
    val reference: String
    fun createReference(reference: String): ReturnType<T>
    fun getFieldDefinition(tableName: String): String
}