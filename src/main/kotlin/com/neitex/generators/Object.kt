package com.neitex.generators

import com.neitex.convertToKotlinTypes
import com.neitex.findImportedThings
import com.neitex.replaceAll
import com.neitex.splitBody

data class Object(private val originalDefinition: String) : DefinitionGenerator {
    override val name =
        originalDefinition.substringBefore("{").substringAfter("const").trimIndent().trim().removeSuffix(":")
    private val implementsOrExtends =
        Regex("(?>(?>implements)|(?>extends))\\s(.*)\\s*(?>\\s*\\{)").find(originalDefinition)?.groupValues?.firstOrNull()
            ?.convertToKotlinTypes()?.replaceAll(listOf(Pair("extends ", ""), Pair("implements ", "")))
            ?.removeSuffix("{")?.trim()?.trimIndent()
    private val nestedGenerators = originalDefinition.substringAfter("{").substringBeforeLast("}").splitBody()
        .map { parseExpression(it).toKotlinDefinition() }

    override fun toKotlinDefinition(): Pair<String, Array<RequiredImport>> =
        Pair("external object $name${implementsOrExtends?.let { ": $it " } ?: ""} ${
            nestedGenerators.map { it.first }.joinToString(separator = "\n", prefix = "{\n", postfix = "\n}") {
                "    $it${if (it.startsWith("val")) " = definedExternally" else ""}"
            }
        }",
            (nestedGenerators.map { it.second }.flatMap { it.toSet() } + (implementsOrExtends?.findImportedThings()
                ?: listOf())).toTypedArray())
}
