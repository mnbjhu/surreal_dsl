package types

import kotlinx.serialization.KSerializer

interface ReturnType<T> {
    val serializer: KSerializer<T>
    var reference: String?
    fun createReference(reference: String): ReturnType<T>
    fun getFieldTypeBounds(): Map<String, String>
    fun <U: ReturnType<*>>U.withReference(reference: String): U =  apply { this.reference = reference }
}