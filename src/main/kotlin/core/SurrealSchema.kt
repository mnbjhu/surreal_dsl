package core

import RecordType
import types.ReturnType
import kotlin.reflect.KFunction0

open class SurrealSchema(val tables: List<KFunction0<RecordType<*>>>, val scopes: List<Scope<*, *, *, *, *, *>>) {

}