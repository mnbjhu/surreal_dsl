import core.Table
import core.TypeProducer
import kotlinx.serialization.Serializable
import types.*


@Serializable
data class User(val username: String, val password: String)

class UserRecord(reference: String): RecordType<User>(reference, User.serializer()){
    override val id by idOf(this)
    val username by stringType
    val password by stringType
    val products by array(recordLink(ProductTable))
    override fun createReference(reference: String): ReturnType<User> {
        return UserRecord(reference)
    }
}

object CategoryTable: Table<Category, CategoryRecord>("category", TypeProducer(CategoryRecord("category")))
object ProductTable: Table<Product, ProductRecord>("product", TypeProducer(ProductRecord("product")))
object UserTable: Table<User, UserRecord>("user", TypeProducer(UserRecord("user")))
object DataTable: Table<Data, DataRecord>("data", TypeProducer(DataRecord("data")))

@Serializable
data class Category(val name: String)


class CategoryRecord(reference: String): RecordType<Category>(reference, Category.serializer()) {

    override val id by idOf(this)
    val name by stringType
    override fun createReference(reference: String) = CategoryRecord(reference)

}
@Serializable
data class Product(val name: String, val categories: List<String>)

class ProductRecord(reference: String): RecordType<Product>(reference, Product.serializer()){
    override val id by idOf(this)
    val name by stringType
    val categories by array(recordLink(CategoryTable))
    override fun createReference(reference: String) = ProductRecord(reference)
}

@Serializable
data class InnerData(val stringData: String, val arrayData: List<String>)

class InnerDataType(reference: String): SurrealObject<InnerData>(InnerData.serializer(), reference){
    val stringData by stringType
    val arrayData by array(stringType)
    override fun createReference(reference: String) = InnerDataType(reference)
}

val innerDataType = TypeProducer(InnerDataType("dummy"))

@Serializable
data class Data(val name: String, val inner: InnerData)
class DataRecord(reference: String): RecordType<Data>(reference, Data.serializer()){
    override val id by idOf(this)
    val name by stringType
    val inner by innerDataType
    override fun createReference(reference: String) = DataRecord(reference)
}


val schema = listOf(UserTable, ProductTable, CategoryTable, DataTable)

