import java.io.File

fun indicateInvalidFormat(file: File) = System.err.println("Invalid json format at ${file.path}")

fun parseObject(stringJson: String): HashMap<String, Any?> {
    val objectRegex = """(\{.*(?<!,)\})""".toRegex()

    if (!objectRegex.matches(stringJson)) throw IllegalArgumentException()

    val parsedJson = HashMap<String, Any?>()

    val entries = stringJson.removeSurrounding("{", "}").split(",").filter { it.isNotEmpty() }

    for (entry in entries) {
        val stringRegex = "\"\\w*\"".toRegex()
        val entryRegex = "${stringRegex}:.*".toRegex()
        if (entryRegex.matchEntire(entry) == null) throw IllegalArgumentException()

        val keyValuePair = entry.split(":")
        val key = keyValuePair.first().replace("\"", "")
        var value: Any? = keyValuePair[1]

        if (value.toString().toIntOrNull() != null) value = value.toString().toInt()
        else if (value.toString().toDoubleOrNull() != null) value = value.toString().toDouble()
        else if (value.toString().toBooleanStrictOrNull() != null) value = value.toString().toBooleanStrict()
        else if (value == "null") value = null
        else if (stringRegex.matchEntire(value.toString()) == null) throw IllegalArgumentException()

        parsedJson[key] = value
    }

    return parsedJson
}


fun parseFile(file: File) {
    try {
        val stringJson = file.readText().replace(Regex("""\n|\s+"""), "")
        val json = parseObject(stringJson)

        println("${file.path} => $json")
    } catch (_: IllegalArgumentException) {
        indicateInvalidFormat(file)
    }
}


fun main() {
    for (step in 3 downTo 1) {
        val directory = File("src/tests/step$step")

        for (file in directory.listFiles()!!) parseFile(file)
    }
}
