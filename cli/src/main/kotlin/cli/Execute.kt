package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import runner.Runner
import java.io.FileInputStream

class Execute : CliktCommand(help = "Execute PrintScript file") {
    private val file by argument().file()

    override fun run() {
        val runner = Runner(listOf(ProgressPrinter()))
        runner.runExecute(FileInputStream(file), CliErrorHandler(), CliPrinter(), CliInputProvider())
    }
}
