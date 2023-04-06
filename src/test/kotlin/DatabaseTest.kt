import core.SurrealSchema
import scopes.SurrealServer
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach

abstract class DatabaseTest(private val schema: SurrealSchema) {

    @BeforeEach
    fun resetData(){
        runBlocking {
            db.remove()
            db.define()
            db.setSchema(schema)
        }
    }


    companion object {
        val server = SurrealServer(
            host = "localhost",
            port = 8000,
        )

        val db = runBlocking {
            server
                .namespace("test")
                .database("test")
                .connectAsAdmin("root", "root")
        }
    }
}