import CategoryTable.permissions
import core.*
import functions.and
import functions.eq
import kotlinx.coroutines.selects.select
import kotlinx.serialization.Serializable
import types.*
import kotlin.time.Duration


@Serializable
data class User(val username: String, val password: String)

class UserRecord(reference: String): RecordType<User>(reference){
    override val id by idOf(this)
    val username by stringType
    val password by stringType
    val products by array(recordLink(ProductTable))
    override fun createReference(reference: String) = UserRecord(reference)

    override fun ReturnScope.decode() = User(!::username, !::password)

    override fun EncodeScope.encode(value: User){
        ::username setAs value.username
        ::password setAs value.password
    }
}

object CategoryTable: Table<Category, CategoryRecord>("category", TypeProducer(CategoryRecord("category")))
object ProductTable: Table<Product, ProductRecord>("product", TypeProducer(ProductRecord("product")))
object UserTable: Table<User, UserRecord>("user", TypeProducer(UserRecord("user"))){
    override fun PermissionScope.permissions(record: UserRecord) {
        permissionsFor(PermissionType.Delete){
            scope(UserScope) { auth ->
                BooleanType.TRUE
            }
        }
    }
}
object DataTable: Table<Data, DataRecord>("data", TypeProducer(DataRecord("data")))

@Serializable
data class Category(val name: String)


class CategoryRecord(reference: String): RecordType<Category>(reference) {

    override val id by idOf(this)
    val name by stringType
    override fun ReturnScope.decode() = Category(!::name)

    override fun EncodeScope.encode(value: Category){
        ::name setAs value.name
    }

    override fun createReference(reference: String) = CategoryRecord(reference)

}
@Serializable
data class Product(val name: String, val categories: List<Linked<Category>> = listOf())

class ProductRecord(reference: String): RecordType<Product>(reference){
    override val id by idOf(this)
    val name by stringType
    val categories by array(recordLink(CategoryTable))

    override fun ReturnScope.decode() = Product(!::name, !::categories)
    override fun EncodeScope.encode(value: Product) {
        ::name setAs value.name
        ::categories setAs value.categories
    }

    override fun createReference(reference: String) = ProductRecord(reference)
}

@Serializable
data class InnerData(val stringData: String, val arrayData: List<String>)

class InnerDataType(reference: String): SurrealObject<InnerData>(reference){
    val stringData by stringType
    val arrayData by array(stringType)
    override fun ReturnScope.decode() = InnerData(!::stringData, !::arrayData)

    override fun EncodeScope.encode(value: InnerData) {
        ::stringData setAs value.stringData
        ::arrayData setAs value.arrayData
    }

    override fun createReference(reference: String) = InnerDataType(reference)
}

val innerDataType = TypeProducer(InnerDataType("dummy"))

@Serializable
data class Data(val name: String, val innerData: InnerData)
class DataRecord(reference: String): RecordType<Data>(reference){
    override val id by idOf(this)
    val name by stringType
    val innerData by innerDataType
    override fun ReturnScope.decode() = Data(!::name, !::innerData)


    override fun EncodeScope.encode(value: Data) {
        ::name setAs value.name
        ::innerData setAs value.innerData
    }

    override fun createReference(reference: String) = DataRecord(reference)
}


object TestSchema: SurrealSchema(listOf(UserTable, ProductTable, CategoryTable, DataTable), listOf(UserScope))

object UserScope: Scope<User, UserRecord, User, UserRecord>() {
    override val name: String = "user_session"
    override val signupType = UserRecord("dummy")
    override val signInType = UserRecord("dummy")
    override val sessionDuration: Duration = Duration.parse("2h")

    override fun TransactionScope.signup(credentials: UserRecord): SurrealArray<User, UserRecord> {
        return UserTable.create {
            username setAs credentials.username
            password setAs credentials.password
            products setAs listOf<RecordLink<Product, ProductRecord>>()
        }
    }

    override fun TransactionScope.signIn(credentials: UserRecord): SurrealArray<User, UserRecord> {
        return UserTable.selectAll {
            where((username eq credentials.username) and (password eq credentials.password))
        }
    }
}

