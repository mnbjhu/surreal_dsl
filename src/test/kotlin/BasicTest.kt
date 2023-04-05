import core.Linked
import functions.eq
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.Test
import types.RecordLink

class BasicTest: DatabaseTest(TestSchema){

    @Test
    fun `Create test`() {
        runBlocking {
            db.transaction {
                ::UserRecord.create {
                    username setAs "TestUser1"
                    password setAs "Password123!"
                    products setAs listOf<RecordLink<Product, ProductRecord>>()
                }
            } `should contain same` listOf(
                User("TestUser1", "Password123!")
            )
        }
    }


    @Test
    fun `Select all test`(){
        `Create test`()
        runBlocking {
            db.transaction { ::UserRecord.selectAll() } `should contain same`
                    listOf(User("TestUser1", "Password123!"))
        }
    }

    @Test
    fun `Select test`(){
        `Create test`()
        runBlocking {
            db.transaction {
                ::UserRecord.select {
                    where(username eq "TestUser1")
                    password
                }
            } `should contain same` listOf("Password123!")
        }
    }

    @Test
    fun `Update test`() {
        `Create test`()
        runBlocking {
            db.transaction {
                ::UserRecord.update { password setAs "NewPassword123!" }
            } `should contain same` listOf ( User("TestUser1", "NewPassword123!") )
        }
    }

    @Test
    fun `Create with inner object`(){
        runBlocking {
            val stored1 = Data("SomeData1", InnerData("Inner", listOf("first", "second", "third")))
            val stored2 = Data("SomeData2", InnerData("Inner", listOf("third", "second", "first")))
            db.transaction {
                +::DataRecord.createContent(stored1)
                +::DataRecord.createContent(stored2)
                ::DataRecord.selectAll()
            } `should contain same` listOf(stored1, stored2)
        }
    }

    @Test
    fun `Test select record link`() {
        runBlocking {
            db.transaction {
                +::UserRecord.create {  username setAs "TestUser1"; password setAs "Password123!" }
                +::UserRecord.create {  username setAs "TestUser2"; password setAs "Password123!" }

                val entertainment by ::CategoryRecord.create { name setAs "Entertainment" }
                val foodAndDrink by ::CategoryRecord.create { name setAs "Food or Drink" }
                +::CategoryRecord.create { name setAs "Health" }

                +::ProductRecord.create { name setAs "Beans"; categories setAs foodAndDrink.select { id } }
                +::ProductRecord.create { name setAs "Avocado"; categories setAs foodAndDrink.select { id } }
                ::ProductRecord.create { name setAs "Wine"; categories setAs (foodAndDrink.select{ id } + entertainment.select { id }) }
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
                +::UserRecord.create {
                    username setAs "TestUser1"
                    password setAs "Password123!"
                }
                +::UserRecord.create {
                    username setAs "TestUser2"
                    password setAs "Password123!"
                }

                val entertainment by ::CategoryRecord.create { name setAs "Entertainment" }
                val foodAndDrink by ::CategoryRecord.create { name setAs "Food or Drink" }
                val health by ::CategoryRecord.create { name setAs "Health" }

                +::ProductRecord.create {
                    name setAs "Beans"
                    categories setAs foodAndDrink.select { id } + health.select { id }
                }
                +::ProductRecord.create {
                    name setAs "Avocado"
                    categories setAs foodAndDrink.select { id }
                }
                +::ProductRecord.create {
                    name setAs "Wine"
                    categories setAs (foodAndDrink.select{ id } + entertainment.select { id })
                }
                +::ProductRecord.selectAll {
                    where(name eq "Wine")
                    fetch(categories)
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
            val userConnection = server
                .namespace("test")
                .database("test")
                .signup(UserScope, UserCredentials("newtest123", "123"))
            userConnection.transaction {
                ::UserRecord.selectAll()
            }
        }
    }

    /*
        @Test
        fun websocketTest(){
            runBlocking {
                server
                    .namespace("test")
                    .database("test")
                    .signup(UserScope, User("newtest", "123"))
                server
                    .namespace("test")
                    .database("test")
                    .signInToWebsocket(UserScope, User("newtest", "123"))
                    .transaction { UserTable.selectAll() }.also { println(it) }
            }
        }


        @Test
        fun liveWebsocketTest(){
            `Test fetch`()
            runBlocking {
                server
                    .namespace("test")
                    .database("test")
                    .signup(UserScope, UserCredentials("newtest123", "123"))
                server
                    .namespace("test")
                    .database("test")
                    .signInToWebsocket(UserScope, UserCredentials("newtest123", "123"))
                    .liveTransaction { ::UserRecord.selectAll() }.also { println(it) }
                while (true){}
            }
        }

         */
}