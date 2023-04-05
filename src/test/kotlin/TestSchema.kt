import core.*
import functions.*
import kotlinx.coroutines.selects.select
import kotlinx.serialization.Serializable
import types.*
import kotlin.time.Duration
import functions.Crypto


@Serializable
data class User(val username: String, val password: String)

class UserRecord: RecordType<User>("user") {
    override val id by idOf(this)
    val username by StringType
        .assert { SurrealString.length(it) greaterThan 8 }
    val password by StringType
    val products by array(recordLink(::ProductRecord))

    override fun ReturnScope.decode() = User(!::username, !::password)

    override fun EncodeScope.encode(value: User) {
        ::username setAs value.username
        ::password setAs value.password
    }
}



/*
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


 */

@Serializable
data class Category(val name: String)


class CategoryRecord: RecordType<Category>("category") {

    override val id by idOf(this)
    val name by StringType
    override fun ReturnScope.decode() = Category(!::name)

    override fun EncodeScope.encode(value: Category){
        ::name setAs value.name
    }


}
@Serializable
data class Product(val name: String, val categories: List<Linked<Category>> = listOf())

class ProductRecord: RecordType<Product>("product"){
    override val id by idOf(this)
    val name by StringType
    val age by LongType
    val categories by array(recordLink(::CategoryRecord))

    override fun ReturnScope.decode() = Product(!::name, !::categories)
    override fun EncodeScope.encode(value: Product) {
        ::name setAs value.name
        ::categories setAs value.categories
    }

}

@Serializable
data class InnerData(val stringData: String, val arrayData: List<String>)

@Serializable
data class UserCredentials(val username: String, val password: String)
class UserCredentialsType: SurrealObject<UserCredentials>(){
    val username by StringType
    val password by StringType
    override fun ReturnScope.decode() = UserCredentials(!::username, !::password)

    override fun EncodeScope.encode(value: UserCredentials) {
        ::username setAs value.username
        ::password setAs value.password
    }
}

class InnerDataType: SurrealObject<InnerData>(){
    val stringData by StringType
    val arrayData by array(StringType)
    override fun ReturnScope.decode() = InnerData(!::stringData, !::arrayData)

    override fun EncodeScope.encode(value: InnerData) {
        ::stringData setAs value.stringData
        ::arrayData setAs value.arrayData
    }

}

val innerDataType = TypeProducer(InnerDataType())

@Serializable
data class Data(val name: String, val innerData: InnerData)
class DataRecord: RecordType<Data>("data"){
    override val id by idOf(this)
    val name by StringType
    val innerData by innerDataType
    override fun ReturnScope.decode() = Data(!::name, !::innerData)


    override fun EncodeScope.encode(value: Data) {
        ::name setAs value.name
        ::innerData setAs value.innerData
    }

}

object TestSchema: SurrealSchema(listOf(::UserRecord, ::ProductRecord, ::CategoryRecord, ::DataRecord), listOf(UserScope))



object UserScope: Scope<UserCredentials, UserCredentialsType, UserCredentials, UserCredentialsType, User, UserRecord>() {
    override val name: String = "user_session"
    override val signupType = UserCredentialsType()
    override val signInType = UserCredentialsType()
    override val tokenType = UserRecord()
    override val sessionDuration: Duration = Duration.parse("2h")

    override fun TransactionScope.signup(credentials: UserCredentialsType): SurrealArray<User, UserRecord> {
        return ::UserRecord.create {
            username setAs credentials.username
            password setAs Crypto.Argon2.generate(credentials.password)
            products setAs listOf<RecordLink<Product, ProductRecord>>()
        }
    }

    override fun TransactionScope.signIn(credentials: UserCredentialsType): SurrealArray<User, UserRecord> {
        return ::UserRecord.selectAll {
            where(
                (username eq credentials.username)
                    and
                Crypto.Argon2.compare(password, credentials.password)
            )
        }
    }
}

