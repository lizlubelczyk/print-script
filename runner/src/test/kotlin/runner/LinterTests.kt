package runner

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import runner.utils.ErrorCollector
import runner.utils.TestObserver
import java.io.File
import java.util.stream.Stream

class LinterTests {
    companion object {
        @JvmStatic
        fun data(): Stream<String> {
            return Stream.of(
                "valid-camelcase",
                "invalid-camelcase",
            )
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    fun testFormatter(directory: String) {
        val observer = TestObserver()
        val errorHandler = ErrorCollector()
        val runner =
            Runner(
                listOf(
                    observer,
                ),
            )

        val file = File("src/test/resources/linter/$directory/main.ps")
        val config = "src/test/resources/linter/$directory/config.json"

        runner.runAnalyze(file, config, errorHandler)

        println(errorHandler.getErrors())

        if (directory.startsWith("invalid")) {
            assert(errorHandler.getErrors().isNotEmpty())
        } else {
            assert(errorHandler.getErrors().isEmpty())
        }
    }
}
