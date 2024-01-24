package controllers

import io.javalin.json.JavalinJackson
import io.javalin.json.toJsonString
import io.javalin.testtools.JavalinTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.matamercer.domain.models.User
import org.matamercer.security.UserRole
import org.matamercer.setupApp

class UserControllerTest {
    private val app = setupApp() // inject any dependencies you might have
    private val testUser = User(
        id = 1,
        name = "Root",
        email = "example@gmail.com",
        role  = UserRole.ROOT
    )
    private val testUsers = mutableListOf<User>(
       testUser
    )

    @Test
    fun `GET to fetch users returns list of users`() = JavalinTest.test(app) { server, client ->
        assertThat(client.get("/users").code).isEqualTo(200)
//        assertThat(client.get("/users").body?.string()).isEqualTo(usersJson)
    }
}