import functions.eq
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.Test
import types.RecordLink


class BasicTest: DatabaseTest(TestSchema){
    @Test
    fun `Create test`(){
        runBlocking {
            db.transaction {
                UserTable.create {
                    it.username setAs "TestUser1"
                    it.password setAs "Password123!"
                    it.products setAs listOf<RecordLink<Product, ProductRecord>>()
                }
            } `should contain same` listOf (
                User("TestUser1", "Password123!")
            )
        }
    }

    @Test
    fun `Select test`(){
        `Create test`()
        runBlocking {
            db.transaction {
                UserTable.select {
                    where(it.username eq "TestUser1")
                    it.password
                }
            } `should contain same` listOf (
                "Password123!"
            )
        }
    }

    @Test
    fun `Update test`(){
        `Create test`()
        runBlocking {
            db.transaction {
                UserTable.update { it.password setAs "NewPassword123!" }
            } `should contain same` listOf ( User("TestUser1", "NewPassword123!") )
        }
    }

    @Test
    fun `Create with inner object`(){
        runBlocking {
            val stored1 = Data("SomeData1", InnerData("Inner", listOf("first", "second", "third")))
            val stored2 = Data("SomeData2", InnerData("Inner", listOf("third", "second", "first")))
            db.transaction {
                +DataTable.createContent(stored1)
                +DataTable.createContent(stored2)
                DataTable.selectAll()
            } `should contain same` listOf(stored1, stored2)
        }
    }

    @Test
    fun `Test select record link`() {
        val entertainment = Category("Entertainment")
        val foodOrDrink = Category("Food or Drink")
        val health = Category("Health")

        val beans = Product("Beans")
        val avocado = Product("Avocado")
        val wine = Product("Wine")


        val user1 = User("TestUser1", "Password123!")
        val user2 = User("TestUser2", "Password123!")

        runBlocking {
            db.transaction {
                +UserTable.create {  it.username setAs "TestUser1"; it.password setAs "Password123!" }
                +UserTable.create {  it.username setAs "TestUser2"; it.password setAs "Password123!" }

                val entertainment by CategoryTable.create { it.name setAs "Entertainment" }
                val foodAndDrink by CategoryTable.create { it.name setAs "Food or Drink" }
                val health by CategoryTable.create { it.name setAs "Health" }

                +ProductTable.create { it.name setAs "Beans"; it.categories setAs foodAndDrink.select { it.id } }
                +ProductTable.create { it.name setAs "Avocado"; it.categories setAs foodAndDrink.select { it.id } }
                ProductTable.create { it.name setAs "Wine"; it.categories setAs (foodAndDrink.select{ it.id } + entertainment.select { it.id }) }
            }
        }
    }
}