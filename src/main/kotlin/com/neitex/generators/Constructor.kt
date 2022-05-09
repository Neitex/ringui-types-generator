package com.neitex.generators

import com.neitex.*

data class Constructor(private val originalDefinition: String) : DefinitionGenerator {
    override val name: String = "constructor"
    private val comment = COMMENT_REGEX.find(originalDefinition)?.value?.plus("\n") ?: ""
    private val arguments =
        originalDefinition.replaceComment("").trim().trimIndent().substringAfter("(").substringBeforeLast(");")
            .removeSuffix(";").replaceAll(listOf(Pair("(", ""), Pair(")", ""))).splitArguments()
            .mapIndexed { index, it ->
                Pair(
                    it.first.treatArgumentName("argument$index"),
                    it.second.treatArgumentType()
                )
            }

    override fun toKotlinDefinition(): Pair<String, Array<RequiredImport>> = Pair("${comment}constructor${
        arguments.joinToString(separator = ", ", prefix = "(", postfix = ")") {
            "${it.first.removeSuffix("?")}: ${it.second}"
        }
    }", arguments.map { it.second.findImportedThings() }.flatten().toTypedArray()
    )
}
