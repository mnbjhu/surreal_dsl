import core.transaction
import types.linked

suspend fun main(){
    schema.forEach { println(it.getDefinition()) }
    transaction {
        val user1 by UserTable.createContent(User("Testing", "Testing"))
        val user2 by UserTable.createContent(User("Testing2", "pass"))
        val product by ProductTable.createContent(Product("TestProduct", listOf()))
        UserTable.create {
            it.username setAs "username"
            it.password setAs "password"
            it.products setAs product.select { it.id }
        }
        /*
        UserTable.select {
            it.products
                .linked { categories.o }
                .linked { name }

         */
    }.also { println(it) }
}
