package com.neitex.generators

import com.neitex.*

data class Class(private val originalDefinition: String) : DefinitionGenerator {
    override val name = originalDefinition.substringAfter("class ").substringBefore(" ").removeSuffix(":")
    private val implementsOrExtends =
        Regex("(?>(?>implements)|(?>extends))\\s(.*)\\s*(?>\\s*\\{)").find(originalDefinition)?.groupValues?.getOrNull(1)
            ?.replaceAll(listOf(Pair("extends ", ""), Pair("implements ", "")))
            ?.removeSuffix("{")?.trim()?.trimIndent()

    private val nestedGenerators =
        originalDefinition.replaceComment("").substringAfter("{").substringBeforeLast("}").splitBody()
            .map { parseExpression(it) }
    private val comment = COMMENT_REGEX.find(originalDefinition.substringBefore("class $name"))?.groupValues?.get(1)

    override fun toKotlinDefinition(): Pair<String, Array<RequiredImport>> =
        Pair("${comment?.plus("\n") ?: ""}external class $name${implementsOrExtends?.let { ": ${"$it ".convertToKotlinTypes()}" /* Race conditions with type aliases go brrrrrr */ } ?: ""} ${
            nestedGenerators.joinToString(separator = "\n", prefix = "{\n", postfix = "\n}") {
                "${if (it.name in OVERRIDDEN_PROPERTIES) "override " else ""}${it.toKotlinDefinition().first}".prependIndent(
                    "    "
                )
            }
        }",
            (nestedGenerators.map { it.toKotlinDefinition().second }
                .flatMap { it.toSet() } + (implementsOrExtends?.convertToKotlinTypes()?.findImportedThings()
                ?: listOf())).toTypedArray())

}
