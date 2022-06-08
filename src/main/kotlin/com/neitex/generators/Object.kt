package com.neitex.generators

import com.neitex.convertToKotlinTypes
import com.neitex.replaceAll
import com.neitex.splitBody

data class Object(private val originalDefinition: String) : DefinitionGenerator {
    override val type: GeneratorType
        get() = GeneratorType.OBJECT
    override val name =
        originalDefinition.substringBefore("{").substringAfter("const").trimIndent().trim().removeSuffix(":")
    private val implementsOrExtends =
        Regex("(?>(?>implements)|(?>extends))\\s(.*)\\s*(?>\\s*\\{)").find(originalDefinition)?.groupValues?.firstOrNull()
            ?.convertToKotlinTypes()?.replaceAll(listOf(Pair("extends ", ""), Pair("implements ", "")))
            ?.removeSuffix("{")?.trim()?.trimIndent()
    private val nestedGenerators = originalDefinition.substringAfter("{").substringBeforeLast("}").splitBody()
        .map { parseExpression(it).toKotlinDefinition() }

    override fun toKotlinDefinition(): KotlinDefinition = KotlinDefinition(
        arrayOf(),
        name,
        "external object $name${implementsOrExtends?.let { ": $it " } ?: ""}",
        nestedGenerators.map { it.toString() }.joinToString(separator = "\n", prefix = "{\n", postfix = "\n}") {
            "    $it${if (it.startsWith("val")) " = definedExternally" else ""}"
        },
        nestedGenerators.map { it.imports }.flatten().toSet()
    )
}
