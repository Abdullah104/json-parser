import java.io.File

val objectRegex = """(\{.*(?<!,)})""".toRegex()
val arrayRegex = """\[.*(?<!,)]""".toRegex()
val stringRegex = "\"\\w.*\"".toRegex()

fun String.normalized() = replace("\"", "")

fun parseValue(value: Any?): Any? {
    if (value.toString().toIntOrNull() != null) return value.toString().toInt()
    else if (value.toString().toDoubleOrNull() != null) return value.toString().toDouble()
    else if (value.toString().toBooleanStrictOrNull() != null) return value.toString().toBooleanStrict()
    else if (value == "null") return null
    else if (objectRegex.matches(value.toString())) return parseObject(value.toString())
    else if (arrayRegex.matches(value.toString())) return parseArray(value.toString())
    else if (stringRegex.matchEntire(value.toString()) == null) throw IllegalArgumentException()

    return (value as String).normalized() // If all previous conditions fail, then [value] is of type String
}

fun parseObject(stringJson: String): HashMap<String, Any?> {
    val parsedJson = HashMap<String, Any?>()

    val entries = stringJson.removeSurrounding("{", "}").split(",").filter { it.isNotEmpty() }

    for (entry in entries) {
        val entryRegex = "${stringRegex}:.*".toRegex()
        if (entryRegex.matchEntire(entry) == null) throw IllegalArgumentException()

        val keyValueSplitIndex = entry.indexOf(':')
        val key = entry.take(keyValueSplitIndex - 1).normalized()
        val value = parseValue(entry.substring(keyValueSplitIndex + 1))

        parsedJson[key] = value
    }

    return parsedJson
}

fun parseArray(stringArray: String): Array<Any?> {
    val list = mutableListOf<Any?>()

    // Remove surrounding brackets
    var iterator = stringArray.substring(1, stringArray.length - 1)

    while (iterator.isNotBlank()) {
        val objectMatch = objectRegex.matchAt(iterator, 0)
        val arrayMatch = arrayRegex.matchAt(iterator, 0)

        if (objectMatch != null) {
            list.add(parseObject(objectMatch.value))

            iterator = iterator.replace(objectMatch.value, "")

            continue
        }

        if (arrayMatch != null) {
            list.add(parseArray(arrayMatch.value))

            iterator = iterator.replace(arrayMatch.value, "")

            continue
        }

        val nextCommaIndex = iterator.indexOf(',')
        val value = iterator.take(if (nextCommaIndex == -1) iterator.length else nextCommaIndex - 1)

        list.add(parseValue(value))

        iterator = iterator.replace(value, "")
    }

    return list.toTypedArray()
}

fun parseFile(file: File): Any {
    val stringJson = file.readText().replace(Regex("""\n|\s+"""), "")

    return parseValue(stringJson)!!
}

fun HashMap<*, *>.toJsonString(indentations: Int = 1): String {
    val builder = StringBuilder("{")

    if (isNotEmpty()) builder.append('\n')

    entries.forEachIndexed { index, entry ->
        builder.append("${"\t".repeat(indentations + 1)}${entry.key}: ")


        when (val value = entry.value) {
            is HashMap<*, *> -> builder.append(value.toJsonString(indentations + 1))
            is Array<*> -> builder.append(value.toJsonString())
            else -> builder.append("$value")
        }

        if (index < entries.size - 1) builder.append(',')

        builder.append("\n")
    }

    builder.append("${"\t".repeat(if (indentations == 1 || isEmpty()) 0 else indentations)}}")

    return builder.toString()
}

fun Array<*>.toJsonString(): String {
    val builder = StringBuilder("[")

    forEachIndexed { index, value ->
        builder.append(value)

        if (index < size - 1) builder.append(',')
    }

    builder.append("]")
    return builder.toString()
}

fun Any.friendlyString(): String {
    val builder = StringBuilder()

    if (this is HashMap<*, *>) {
        builder.append(this.toJsonString())
    } else if (this is Array<*>) {
        builder.append(this.toJsonString())
    }

    return builder.toString()
}

fun main() {
    for (step in 4 downTo 1) {
        val directory = File("src/tests/step$step")

        for (file in directory.listFiles()!!) try {
            val json = parseFile(file)

            println("${file.path} => ${json.friendlyString()}")
        } catch (_: IllegalArgumentException) {
            System.err.println("Invalid json format at ${file.path}")
        }
    }
}
