import core.Linked
import core.SurrealSchema
import core.Table
import core.TypeProducer
import kotlinx.serialization.Serializable
import types.*


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
object UserTable: Table<User, UserRecord>("user", TypeProducer(UserRecord("user")))
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
data class Data(val name: String, val inner: InnerData)
class DataRecord(reference: String): RecordType<Data>(reference){
    override val id by idOf(this)
    val name by stringType
    val innerData by innerDataType
    override fun ReturnScope.decode() = Data(!::name, !::innerData)


    override fun EncodeScope.encode(value: Data) {
        ::name setAs value.name
        ::innerData setAs value.inner
    }

    val inner by innerDataType
    override fun createReference(reference: String) = DataRecord(reference)
}


object TestSchema: SurrealSchema(listOf(UserTable, ProductTable, CategoryTable, DataTable))

