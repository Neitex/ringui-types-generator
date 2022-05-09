package com.neitex.generators

import com.neitex.*

data class Value(private val originalDefinition: String) : DefinitionGenerator {
    private val comment = COMMENT_REGEX.find(originalDefinition)?.value?.plus("\n") ?: ""
    override val name: String =
        originalDefinition.replaceComment("").substringBefore(":").replace("private", "").replace("?", "")
            .replace("readonly", "").trimIndent().trim().toLegalJsName()
            .replace(Regex("(\\/\\*\\*\\n)[a-zA-Z\\ \\*\\n\\t0-9\\@\\,\\'\\\"\\_.]*(\\n*\\s\\*\\/)"), "").trim()
            .trimIndent().replace("\n", "")
    private val isReadonly = originalDefinition.replaceComment("").trim().trimIndent().startsWith("readonly")
    private val upperBoundDefinitionGenerators =
        originalDefinition.replaceComment("").substringAfter(":").trim().trimIndent().let {
            if (it.startsWith("{\n")) Interface(name.let {
                it.first().uppercase() + it.drop(1)
            }, it.trimIndent().trim().substringAfter("{\n").substringBeforeLast("};"), false)
            else null
        }
    private val type =
        if (upperBoundDefinitionGenerators == null) originalDefinition.substringAfter(":").trim().trimIndent().let {
            if (it.startsWith("(")) {
                Regex("\\(?\\(([\\w\\W]*?)\\) => ([\\w\\W]*?)(?:\\)|;)").find(
                    it
                )?.groupValues?.drop(1)?.let {
                        "${
                            it.dropLast(1).joinToString(
                                separator = ", ", prefix = "(", postfix = ")"
                            ) // TODO: Make sure no thing like "({something, something2}: String) -> Unit" comes to the generated code
                        } -> ${it.last()}"
                    }?.convertToKotlinTypes()
            } else it.convertToKotlinTypes()
        }?.removeSuffix(";") else null

    override fun toKotlinDefinition(): Pair<String, Array<RequiredImport>> = Pair(
        "${upperBoundDefinitionGenerators?.toKotlinDefinition()?.first?.plus("\n") ?: ""}${if (name.contains('`')) "\n$SUPPRESS_ILLEGAL_CHARS\n" else ""}${if (isReadonly) "val" else "var"} $name: ${upperBoundDefinitionGenerators?.name ?: type}",
        type.toString().findImportedThings().toTypedArray()
    )

}
