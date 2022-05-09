package com.neitex.generators

import com.neitex.FUNCTION_REGEX
import com.neitex.replaceAll
import com.neitex.replaceComment

typealias RequiredImport = String

val typescriptGarbage = listOf("export", "declare", "default")

interface DefinitionGenerator {
    val name: String
    fun toKotlinDefinition(): Pair<String, Array<RequiredImport>>
}

data class KotlinPackage(private val packageString: String) {
    val physicalPath = packageString.replace("-", "_").replace(".", "/")
    val packagePath = packageString.replace("-", "_")
    override fun toString(): String = packagePath
}

fun parseExpression(expression: String): DefinitionGenerator {
    val definition =
        expression.replaceAll(typescriptGarbage.map { Pair(it, "") }).replaceComment("").trim().trimIndent()
    return when {
        definition.startsWith("type ") -> TypeAlias(expression)
        definition.startsWith("interface") -> Interface(expression)
        definition.startsWith("class ") ->
            Class(expression)
        definition.startsWith("enum") -> Enum(expression)
        definition.startsWith("global ") -> parseExpression(expression.substringAfter("{\n").substringBefore("}"))
        definition.contains(Regex("^(private)|(static)|(state)")) ->
            EmptyDefinitionGenerator(expression)
        definition.startsWith("constructor") || definition.startsWith("new") -> Constructor(expression)
        definition.startsWith("const") -> Const(expression)
        definition.contains(FUNCTION_REGEX) -> Function(
            expression
        )
        else -> Value(expression)
    }
}

