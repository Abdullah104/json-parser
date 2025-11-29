import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) = parseString(File(args.first()).readText())

fun parseString(string: String) {
    val cleanedString = string.replace("\n", "")
    val objectRegex = """(\{.*\})""".toRegex()

    if (!objectRegex.matches(cleanedString)) indicateInvalidFormat()

//    val json = HashMap<String, Any>()
//
//    val entries = string.split(",")
//
//    println(entries)
//
//    for (entry in entries) {
//        val entryRegex = "\"([A-Za-z])\\w+\"".toRegex()
//        val matches = entryRegex.matchEntire(entry)
//
//        println(matches)
//
//
//        val keyValuePair = entry.split(":")
//
//
//        val key = keyValuePair.first()
//        val value = keyValuePair[1]
//    }
//
//    return
//
//    val builder = StringBuilder().append("{}")
//    println(builder)
//
//    exitProcess(0)
}

fun indicateInvalidFormat() {
    System.err.println("Invalid json format")

    exitProcess(1)
}
