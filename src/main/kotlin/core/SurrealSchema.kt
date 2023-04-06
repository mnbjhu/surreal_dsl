package core

import types.Table
import kotlin.reflect.KFunction0

open class SurrealSchema(val tables: List<KFunction0<Table<*>>>, val scopes: List<Scope<*, *, *, *, *, *>>) {

}