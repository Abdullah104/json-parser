import java.io.File

val objectRegex = """(\{.*(?<!,)})""".toRegex()
val arrayRegex = """\[.*(?<!,)]""".toRegex()
val stringRegex = "\"\\w.*\"".toRegex()

fun indicateInvalidFormat(file: File) = System.err.println("Invalid json format at ${file.path}")

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

//        if (objectRegex.matchesAt(iterator, 0)) {
//            var objectEndIndex = 1
//            var curlyBracesStack = 1
//
//            while (curlyBracesStack != 0 && objectEndIndex < stringArray.length) {
//                if (iterator[objectEndIndex] == '{') curlyBracesStack++
//                if (iterator[objectEndIndex] == '}') curlyBracesStack--
//
//                if (curlyBracesStack != 0) objectEndIndex++
//            }
//
//            if (curlyBracesStack != 0) throw IllegalArgumentException()
//        }
    }

    return list.toTypedArray()
}


fun parseFile(file: File) {
    try {
        val stringJson = file.readText().replace(Regex("""\n|\s+"""), "")
        val json = parseValue(stringJson)

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
