import core.Auth
import core.SurrealServer
import core.transaction
import functions.eq
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.Test
import types.RecordLink


class BasicTest: DatabaseTest(testSchema){
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
                UserTable.update {
                    it.password setAs "NewPassword123!"
                }
            } `should contain same` listOf (
                User("TestUser1", "NewPassword123!")
            )
        }
    }
}