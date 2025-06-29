package unit.dao

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.domain.dao.ArticleDao
import org.matamercer.domain.dao.TransactionManager
import kotlin.test.Test


@ExtendWith(MockKExtension::class)
class ArticleDaoUnitTest {
    private lateinit var articleDao: ArticleDao
    private lateinit var transactionManager: TransactionManager

    @BeforeEach
    fun setup(){
        articleDao = ArticleDao()

    }

    @Test
    fun `When findbyid, return the article`(){
        transactionManager.wrap { conn ->
            val article = articleDao.findById(conn, 1)
            assert(article != null) { "Article with ID 1 should exist" }
        }
    }



}