package unit.dao

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.domain.dao.ArticleDao
import kotlin.test.Test


@ExtendWith(MockKExtension::class)
class ArticleDaoUnitTest {
    private lateinit var articleDao: ArticleDao

    @BeforeEach
    fun setup(){
        articleDao = ArticleDao()
    }

    @Test
    fun `When findbyid, return the article`(){
//       val art = articleDao.findById()
    }



}