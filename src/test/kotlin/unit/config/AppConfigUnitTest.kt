package unit.config

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.config.AppConfig
import org.matamercer.config.reader.ConfigReader


@ExtendWith(MockKExtension::class)
class AppConfigUnitTest {

    @MockK(relaxUnitFun = true)
    private lateinit var configReader: ConfigReader

    @Test
    fun `test app config`(){
        configReader = mockk<ConfigReader>()
        every { configReader.get(any()) } returns "0"
        with(AppConfig){
            registerConfigReader(configReader)
            reload()
            assert(discordOauthClientId == "0")
            assert(discordOauthClientSecret == "0")
            assert(uploadSizeLimit == 0)
            assert(uploadUserSizeLimit == 0)
            assert(maxFileNameLength == 0)
            assert(maxAttachmentCount == 0)
            assert(maxImageWidth == 0)
            assert(maxImageHeight == 0)
        }
    }

    @Test
    fun `test app config not a number values`(){
        configReader = mockk<ConfigReader>()
        every { configReader.get(any()) } returns "0"
        every { configReader.get("maxImageWidth") } returns "derp"
        AppConfig.registerConfigReader(configReader)
        assertThrows<IllegalStateException> {
            AppConfig.reload()
        }
    }

    @Test
    fun `test app config with null values`(){
        configReader = mockk<ConfigReader>()
        every { configReader.get(any()) } returns null
        AppConfig.registerConfigReader(configReader)
        assertThrows<IllegalStateException> {
            AppConfig.reload()
        }
    }


}