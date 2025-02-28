package fixtures

import org.matamercer.domain.models.Article
import org.matamercer.domain.models.Character
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.Timeline
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

//    val testFile = FileModel(
//        id = 1,
//        name = "testfile.png",
//        author = rootUser,
//    )

    val testArticle = Article(
        id = 1,
        title = "title",
        body = "body",
        author = rootUser,
        attachments = emptyList()
    )

    val testCharacter = Character(
        id = 1,
        name = "Amuro",
        gender = "male",
        birthday = "Oct 11, 2024",
        firstSeen = "Episode 1",
        status = "alive",
        age = 20,
        body = "lorem ipsum",
        author = rootUser,
        attachments = emptyList(),
    )

    val testTimeline = Timeline(
        id = 1,
        name= "First Timeline",
        description = "This is a timeline about aliens."
    )
}