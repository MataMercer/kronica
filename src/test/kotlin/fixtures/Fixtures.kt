package fixtures

import org.matamercer.domain.models.*
import org.matamercer.security.UserRole

object Fixtures {
    val rootUser = User(
        id = 1,
        name = "root",
        email = "example@gmail.com",
        role = UserRole.ROOT
    )

    val maliciousUser = User(
        id = 2,
        name = "mallory",
        email = "Mallory@gmail.com",
        role = UserRole.AUTHENTICATED_USER
    )

    val testArticle = NewArticle(
        title = "title",
        body = "body",
        author = rootUser,
        attachments = emptyList(),
        nsfw = false,
        tags = listOf(NewTag(name = "red_tag"), NewTag(name = "tall_tag"))
    )

    val testCharacter = Character(
        id = 1,
        name = "Amuro",
        body = "lorem ipsum",
        author = rootUser,
        attachments = emptyList(),
        traits = listOf(Trait(name = "mobile suit", value = "gundam"), Trait(name = "allegiance", value = "londo bell")),
        nsfw = false,
    )

    val testTimeline = Timeline(
        id = 1,
        name = "First Timeline",
        description = "This is a timeline about aliens.",
        author = rootUser,
        nsfw = false,
    )
}