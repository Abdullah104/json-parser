import java.io.File

fun main() {
    for (step in 2 downTo 1) {
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

    val parsedJson = HashMap<String, Any>()

    val entries = stringJson.removeSurrounding("{", "}").split(",").filter { it.isNotEmpty() }

    for (entry in entries) {
        val entryPartRegex = "(\"\\w*\")".toRegex()
        val entryRegex = "$entryPartRegex:$entryPartRegex".toRegex()
        val matches = entryRegex.matchEntire(entry)

        if (matches == null) {
            indicateInvalidFormat(file)

            return
        }


        val keyValuePair = entry.split(":").map { it.replace("\"", "") }


        val key = keyValuePair.first()
        val value = keyValuePair[1]

        parsedJson[key] = value
    }

    println("${file.path} => $parsedJson")
}

fun indicateInvalidFormat(file: File) = System.err.println("Invalid json format at ${file.path}")
