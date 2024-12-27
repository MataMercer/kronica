package unit.service

import fixtures.Fixtures
import io.javalin.http.ForbiddenResponse
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.dao.TransactionManager
import org.matamercer.domain.models.Article
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.User
import org.matamercer.domain.services.ArticleService
import org.matamercer.web.CreateArticleForm
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class ArticleServiceUnitTest {

    @MockK(relaxUnitFun = true)
    private lateinit var articleDaoSql: ArticleDao
    @MockK(relaxUnitFun = true)
    private lateinit var transactionManager: TransactionManager


    @InjectMockKs(injectImmutable = true)
    private lateinit var articleService: ArticleService

    //fixtures
    private lateinit var testArticle: Article
    private lateinit var testArticleForm: CreateArticleForm
    private lateinit var testFile:FileModel
    private lateinit var testUser: User
    private lateinit var testMaliciousUser: User
    private lateinit var fixtures: Fixtures

    @BeforeEach
    fun setup(){
        clearAllMocks()
        fixtures = Fixtures()
        testUser = fixtures.rootUser
        testMaliciousUser = fixtures.maliciousUser
        testArticle = fixtures.testArticle
        testArticleForm = CreateArticleForm(title = "title", body = "body")

        //make callbacks pass through transact
        every { transactionManager.wrap({}) } answers { (firstArg<()->Unit>())()}
    }



    @Test
    fun `When findById, return the article`(){
        every { articleDaoSql.findById(any(), any()) } returns testArticle
        val art = articleService.getById(1)
        assertThat(art.id).isEqualTo(testArticle.id)
    }

    @Test
    fun `When create article, create the article and return the id`(){
        every { articleDaoSql.create(any(), any()) } returns testArticle.id!!
        val art = articleService.create(testArticleForm, testUser)

        //TODO: verfiy files creation is done.

        assertThat(art).isEqualTo(testArticle.id)
    }

    @Test
    fun `When deleteById, delete the article`(){
        every { articleDaoSql.findById(any(), any()) } returns testArticle
        articleService.deleteById(testUser,testArticle.id)
        verify { articleDaoSql.deleteById(any(), testArticle.id!!) }
    }

    @Test
    fun `When deleteById is called from a malicious user, throw a forbidden response`(){
        every { articleDaoSql.findById(any(), any()) } returns testArticle
            val thrown = assertThrows<ForbiddenResponse> {
                articleService.deleteById(testMaliciousUser,testArticle.id)
                verify { articleDaoSql.deleteById(any(), testArticle.id!!) wasNot Called}
            }
            val exception = ForbiddenResponse()
            assertEquals(exception.message, thrown.message)
    }

}