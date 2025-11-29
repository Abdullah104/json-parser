import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) = parseString(File(args.first()).readText())

fun parseString(string: String) {
    if (!(string.startsWith('{') && string.endsWith('}'))) {
        System.err.println("Invalid json format")

        exitProcess(1)
    }

    val json = HashMap<String, Any>()

    val builder = StringBuilder().append("{}")
    println(builder)

    exitProcess(0)
}
