import java.io.File

val objectRegex = """(\{(\n?.*?)+(?<!,)})\n?""".toRegex()
val arrayRegex = """\[.*?(?<!,)]""".toRegex()
val stringRegex = "\".*?\"".toRegex()
val booleanRegex = "true|false".toRegex()
val nullRegex = "null".toRegex()
val numberRegex = """\d+""".toRegex()
val controlCharactersRegex = """[\x00-\x1F]""".toRegex()

fun getClosingCharacterIndex(string: String, closingCharacter: Char, openingCharacter: Char): Int {
    var count = 0

    string.forEachIndexed { index, ch ->
        if (ch == openingCharacter) count++
        if (ch == closingCharacter) count--

        if (count == 0) return index
    }

    return -1
}

fun parseValue(value: Any?): Any? {
    // Numbers
    if ("""[^0](\d|\.)+""".toRegex().matches(value.toString())) {
        if (value.toString().toIntOrNull() != null) return value.toString().toInt()
        else if (value.toString().toDoubleOrNull() != null) return value.toString().toDouble()
    }

    else if (value.toString().toBooleanStrictOrNull() != null) return value.toString().toBooleanStrict()
    else if (value == "null") return null
    else if (objectRegex.matches(value.toString())) return parseObject(value.toString())
    else if (arrayRegex.matches(value.toString())) return parseArray(value.toString())

    else if (stringRegex.matchEntire(value.toString()) == null || value.toString()
            .contains(controlCharactersRegex)
    ) throw IllegalArgumentException()

    return (value as String).replace("\"", "")
}

fun parseObject(stringJson: String): HashMap<String, Any?> {
    val parsedJson = HashMap<String, Any?>()

    val entriesRegex =
        """$stringRegex\s*:\s*($stringRegex|$booleanRegex|$nullRegex|$numberRegex|$objectRegex|$arrayRegex)""".toRegex()

    val matches = entriesRegex.findAll(stringJson)
    var iterator = stringJson

    for (match in matches) {
        val group = (match.groups.first() ?: throw IllegalArgumentException())
        val groupValue = group.value
        val separatorIndex = groupValue.indexOf(':')

        val key = groupValue.take(separatorIndex).trim()
        val rawValue = groupValue.drop(separatorIndex + 1).trim()

        parsedJson[key.replace("\"", "")] = parseValue(rawValue)

        iterator = iterator.replace(groupValue, "")
    }

    // Clean iterator
    iterator = iterator.replace("[,\n{}]".toRegex(), "").removeSurrounding("{", "}").trim()

    if (iterator.isNotEmpty()) throw IllegalArgumentException()

    return parsedJson
}

fun parseArray(stringArray: String): Array<Any?> {
    val list = mutableListOf<Any?>()

    // Remove surrounding brackets
    var iterator = stringArray.substring(1, stringArray.length - 1).trim()

    while (iterator.isNotBlank()) {
        if (iterator.first() == '{') {
            val endIndex = getClosingCharacterIndex(iterator, '}', '{')

            if (endIndex == -1) throw IllegalArgumentException()

            list.add(parseObject(iterator.take(endIndex + 1)))

            iterator = iterator.drop(endIndex + 2).trim()

            continue
        }

        if (iterator.first() == '[') {
            val endIndex = getClosingCharacterIndex(iterator, ']', '[')

            if (endIndex == -1) throw IllegalArgumentException()

            list.add(parseArray(iterator.take(endIndex + 1)))

            iterator = iterator.drop(endIndex + 2).trim()

            continue
        }

        val nextCommaIndex = iterator.indexOf(',')
        val value = iterator.take(if (nextCommaIndex == -1) iterator.length else nextCommaIndex)

        list.add(parseValue(value.trim()))

        iterator = iterator.drop(if (nextCommaIndex == -1) value.length + 2 else nextCommaIndex + 1).trim()
    }

    return list.toTypedArray()
}

fun parseFile(file: File): Any {
    val stringJson = file.readText()

    if (!(objectRegex.matches(stringJson) || arrayRegex.matches(stringJson))) throw IllegalArgumentException()

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
    for (step in 5 downTo 1) for (file in File("src/tests/step$step").listFiles()!!)
//    val file = File("src/tests/step5/fail25.json")
    try {
        val json = parseFile(file)

        println("${file.path}: ${json.friendlyString()}")
    } catch (_: IllegalArgumentException) {
        System.err.println("Invalid json format at ${file.path}")
    }
}
