import core.Auth
import core.SurrealSchema
import core.SurrealServer
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
        private val server = SurrealServer(
            host = "localhost",
            port = 8000,
            auth = Auth.Root(
                username = "root",
                password = "root"
            )
        )

        val db = server
            .namespace("test")
            .database("test")
    }
}