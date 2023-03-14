import kotlinx.serialization.Serializable


@Serializable
data class User(val username: String, val password: String)

class UserRecord(reference: String): RecordType<User>(reference, User.serializer()){
    override val id by idOf(this)
    val username by stringType
    val password by stringType

    override fun createReference(reference: String): ReturnType<User> {
        return UserRecord(reference)
    }
}

object CategoryTable: Table<Category, CategoryRecord>("category", TypeProducer(CategoryRecord("dummy")))
object ProductTable: Table<Product, ProductRecord>("product", TypeProducer(ProductRecord("dummy")))
object UserTable: Table<User, UserRecord>("user", TypeProducer(UserRecord("dummy")))

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

val schema = listOf(UserTable, ProductTable, CategoryTable)

