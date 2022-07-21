package com.neitex.generators

import com.neitex.*

data class Value(private val originalDefinition: String) : DefinitionGenerator {
    override val type: GeneratorType
        get() = GeneratorType.VALUE
    private val comment = COMMENT_REGEX.find(originalDefinition)?.value?.plus("\n") ?: ""
    override val name: String =
        originalDefinition.replaceComment("").substringBefore(":").replace("private", "").replace("?", "")
            .replace("readonly", "").trimIndent().trim().toLegalJsName()
            .replace(Regex("(/\\*\\*\\n)[a-zA-Z *\\n\\t0-9@,'\"_.]*(\\n*\\s\\*/)"), "").trim()
            .trimIndent().replace("\n", "")
    private val isReadonly = originalDefinition.replaceComment("").trim().trimIndent().startsWith("readonly")
    private val upperBoundDefinitionGenerators =
        originalDefinition.replaceComment("").substringAfter(":").trim().trimIndent().let {
            if (it.startsWith("{\n")) Interface(name.let {
                it.first().uppercase() + it.drop(1)
            }, it.trimIndent().trim().substringAfter("{\n").substringBeforeLast("};"), false)
            else null
        }
    private val valType =
        if (upperBoundDefinitionGenerators == null) originalDefinition.substringAfter(":").trim().trimIndent().let {
            if (LAMBDA_REGEX.containsMatchIn(it)) LambdaType(it).toString() else it.convertToKotlinTypes()
        } else null

    override fun toKotlinDefinition(): KotlinDefinition =
        KotlinDefinition(upperBoundDefinitionGenerators?.let { arrayOf(it) } ?: arrayOf(),
            name,
            "$comment${if (name.contains('`')) "\n$SUPPRESS_ILLEGAL_CHARS\n" else ""}${if (isReadonly) "val" else "var"} $name: ${upperBoundDefinitionGenerators?.name ?: valType}",
            null,
            valType?.findImportedThings()?.toSet() ?: setOf())

}
