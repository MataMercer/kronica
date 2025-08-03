package unit.config

import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.matamercer.config.reader.ArgsReader

@ExtendWith(MockKExtension::class)
class ArgsReaderUnitTest {

    @MockK(relaxUnitFun = true)
    private lateinit var argsReader: ArgsReader

    @Test
    fun `test args reader`(){
        val args = arrayOf("-test","answer", "-test2", "answer2")
        argsReader = ArgsReader(args)


        assert(argsReader.get("test") == "answer")
        assert(argsReader.get("test2") == "answer2")
    }
}