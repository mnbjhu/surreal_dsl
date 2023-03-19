import core.Linked
import core.SurrealServer
import functions.eq
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
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
            }.first().apply {
                name `should be equal to` "Wine"
                categories.size `should be equal to` 2
                categories.forEach { it `should be instance of` Linked.Reference::class }
            }
        }
    }

    @Test
    fun `Test fetch`() {
        runBlocking {
            db.transaction {
                +UserTable.create {  it.username setAs "TestUser1"; it.password setAs "Password123!" }
                +UserTable.create {  it.username setAs "TestUser2"; it.password setAs "Password123!" }

                val entertainment by CategoryTable.create { it.name setAs "Entertainment" }
                val foodAndDrink by CategoryTable.create { it.name setAs "Food or Drink" }
                +CategoryTable.create { it.name setAs "Health" }

                +ProductTable.create { it.name setAs "Beans"; it.categories setAs foodAndDrink.select { it.id } }
                +ProductTable.create { it.name setAs "Avocado"; it.categories setAs foodAndDrink.select { it.id } }
                +ProductTable.create { it.name setAs "Wine"; it.categories setAs (foodAndDrink.select{ it.id } + entertainment.select { it.id }) }
                ProductTable.selectAll {
                    where(it.name eq "Wine")
                    fetch(it.categories)
                }
            }.first().apply {
                name `should be equal to` "Wine"
                categories.size `should be equal to` 2
                categories.forEach { it `should be instance of` Linked.Actual::class }
                categories.map{ (it as Linked.Actual).record.name } `should contain same` listOf("Entertainment", "Food or Drink")
            }
        }
    }

    @Test
    fun `Test Schema Generator`(){
        TestSchema.tables.forEach {
            println(it.getDefinition())
        }
        TestSchema.scopes.forEach {
            println(it.getDefinition())
        }
    }

    @Test
    fun `signup test`(){
        runBlocking {
            SurrealServer.signup("test", "test", UserScope, User("test", "test"))
        }
    }
}