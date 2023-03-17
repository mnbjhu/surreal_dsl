package core

import RecordType
import types.SurrealArray

open class Table<T, U: RecordType<T>>(val name: String, inner: U): SurrealArray<T, U>(inner, name){
    constructor(name: String, type: TypeProducer<T, U>): this(name, type.createReference(name))
    fun getDefinition(): String = inner.getDefinition()
}