package unit.service

import fixtures.Fixtures
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.jdbc.TransactionManager
import org.matamercer.domain.models.FileModel
import org.matamercer.domain.models.NewArticle
import org.matamercer.domain.models.User
import org.matamercer.domain.services.ArticleService
import org.matamercer.web.Forms.CreateArticleForm

@ExtendWith(MockKExtension::class)
class ArticleServiceTest {

    @MockK(relaxUnitFun = true)
    private lateinit var articleDaoSql: ArticleDao
    @MockK(relaxUnitFun = true)
    private lateinit var transactionManager: TransactionManager


    @InjectMockKs(injectImmutable = true)
    private lateinit var articleService: ArticleService

    //fixtures
    private lateinit var testArticle: NewArticle
    private lateinit var testArticleForm: CreateArticleForm
    private lateinit var testFile:FileModel
    private lateinit var testUser: User
    private lateinit var testMaliciousUser: User

    @BeforeEach
    fun setup(){
        clearAllMocks()
        testUser = Fixtures.rootUser
        testMaliciousUser = Fixtures.maliciousUser
        testArticle = Fixtures.testArticle
        testArticleForm = CreateArticleForm(title = "title", body = "body")

        //make callbacks pass through transact
//        every { transactionManager.wrap({}) } answers { (firstArg<()->Unit>())()}
    }



    @Test
    fun `When findById, return the article`(){
//        every { articleDaoSql.findById(any()) } returns testArticle
//        val a = articleService.getById(1)
//        assertThat(a.id).isEqualTo(testArticle.id)
    }

//
//    @Test
//    fun `When create article, create the article and return the id`(){
//        every { articleDaoSql.create(any(), any()) } returns testArticle.id!!
//        val art = articleService.create(testArticleForm, testUser)
//
//        //TODO: verfiy files creation is done.
//
//        assertThat(art).isEqualTo(testArticle.id)
//    }
//
//    @Test
//    fun `When deleteById, delete the article`(){
//        every { articleDaoSql.findById(any(), any()) } returns testArticle
//        articleService.deleteById(testUser,testArticle.id)
//        verify { articleDaoSql.deleteById(any(), testArticle.id!!) }
//    }
//
//    @Test
//    fun `When deleteById is called from a malicious user, throw a forbidden response`(){
//        every { articleDaoSql.findById(any(), any()) } returns testArticle
//            val thrown = assertThrows<ForbiddenResponse> {
//                articleService.deleteById(testMaliciousUser,testArticle.id)
//                verify { articleDaoSql.deleteById(any(), testArticle.id!!) wasNot Called}
//            }
//            val exception = ForbiddenResponse()
//            assertEquals(exception.message, thrown.message)
//    }

}