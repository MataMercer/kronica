package fixtures

import org.matamercer.domain.models.Article
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.User
import org.matamercer.security.UserRole

class Fixtures {


    val rootUser = User(
        id = 1,
        name = "Root",
        email = "example@gmail.com",
        role  = UserRole.ROOT
    )

    val maliciousUser = User(
        id = 2,
        name = "Mallory",
        email = "Mallory@gmail.com",
        role  = UserRole.AUTHENTICATED_USER
    )

    val testFile = FileModel(
        id = 1,
        name = "testfile.png",
        author = rootUser,
    )

    val testArticle = Article(
        id = 1,
        title = "title",
        body = "body",
        author = rootUser,
        attachments = listOf(testFile)
    )


}