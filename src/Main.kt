import java.io.File

val objectRegex = """(\{.*(?<!,)})""".toRegex()
val arrayRegex = """\[.*(?<!,)]""".toRegex()
val stringRegex = "\"\\w.*\"".toRegex()

fun indicateInvalidFormat(file: File) = System.err.println("Invalid json format at ${file.path}")

fun parseValue(value: Any?): Any? {
    if (value.toString().toIntOrNull() != null) return value.toString().toInt()
    else if (value.toString().toDoubleOrNull() != null) return value.toString().toDouble()
    else if (value.toString().toBooleanStrictOrNull() != null) return value.toString().toBooleanStrict()
    else if (value == "null") return null
    else if (objectRegex.matches(value.toString())) return parseObject(value.toString())
    else if (arrayRegex.matches(value.toString())) return parseArray(value.toString())
    else if (stringRegex.matchEntire(value.toString()) == null) throw IllegalArgumentException()

    return value // If all previous conditions fail, then [value] is of type String
}

fun parseObject(stringJson: String): HashMap<String, Any?> {

    if (!objectRegex.matches(stringJson)) throw IllegalArgumentException()

    val parsedJson = HashMap<String, Any?>()

    val entries = stringJson.removeSurrounding("{", "}").split(",").filter { it.isNotEmpty() }

    for (entry in entries) {
        val entryRegex = "${stringRegex}:.*".toRegex()
        if (entryRegex.matchEntire(entry) == null) throw IllegalArgumentException()

        val keyValuePair = entry.split(":")
        val key = keyValuePair.first().replace("\"", "")
        val value = parseValue(keyValuePair[1])

        parsedJson[key] = value
    }

    return parsedJson
}

fun parseArray(stringArray: String): Array<Any?> {
    val list = mutableListOf<Any?>()

    return list.toTypedArray()
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
