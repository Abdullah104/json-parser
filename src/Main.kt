import java.io.File

fun main() {
    for (step in 3 downTo 1) {
        val directory = File("src/tests/step$step")

        for (file in directory.listFiles()!!) parseFile(file)
    }
}

fun parseFile(file: File) {
    val stringJson = file.readText().replace(Regex("""\n|\s+"""), "")
    val objectRegex = """(\{.*(?<!,)\})""".toRegex()

    if (!objectRegex.matches(stringJson) || stringJson.endsWith(",}")) {
        indicateInvalidFormat(file)

        return
    }

    val parsedJson = HashMap<String, Any?>()

    val entries = stringJson.removeSurrounding("{", "}").split(",").filter { it.isNotEmpty() }

    for (entry in entries) {
        val stringRegex = "\"\\w*\"".toRegex()
        val entryRegex = "${stringRegex}:.*".toRegex()
        val matches = entryRegex.matchEntire(entry)

        if (matches == null) {
            indicateInvalidFormat(file)

            return
        }

        val keyValuePair = entry.split(":")
        val key = keyValuePair.first().replace("\"", "")
        var value: Any? = keyValuePair[1]

        if (value.toString().toIntOrNull() != null) value = value.toString().toInt()
        else if (value.toString().toDoubleOrNull() != null) value = value.toString().toDouble()
        else if (value.toString().toBooleanStrictOrNull() != null) value = value.toString().toBooleanStrict()
        else if (value == "null") value = null
        else if (stringRegex.matchEntire(value.toString()) == null) {
            indicateInvalidFormat(file)

            return
        }

        parsedJson[key] = value
    }

    println("${file.path} => $parsedJson")
}

fun indicateInvalidFormat(file: File) = System.err.println("Invalid json format at ${file.path}")
