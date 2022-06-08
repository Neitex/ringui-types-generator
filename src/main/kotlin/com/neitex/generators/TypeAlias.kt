package com.neitex.generators

import com.neitex.*

data class TypeAlias(private val originalDefinition: String) : DefinitionGenerator {
    override val type: GeneratorType
        get() = GeneratorType.TYPEALIAS
    private val comment = COMMENT_REGEX.find(originalDefinition)?.value
    override val name: String =
        originalDefinition.substringBefore("=").replaceAll(typescriptGarbage.map { Pair(it, "") } + Pair("type", ""))
            .dropLast(1).trim().trimIndent().toLegalJsName()
    private val upperBoundDefinitionGenerators = originalDefinition.substringAfter("=").trim().trimIndent().let {
        if (it.startsWith("{\n")) Interface(
            "${name}Interface", it.substringAfter("{\n").substringBeforeLast("\n}"), true // typealiases are always external
        )
        else null
    }
    private val aliasedTo =
        (upperBoundDefinitionGenerators?.name ?: originalDefinition.substringAfter("=").trim().trimIndent()
            .convertToKotlinTypes().removeSuffix(";")).also {
            DYNAMIC_GENERATED_TYPES[name] = it
        }

    override fun toKotlinDefinition(): KotlinDefinition =
        KotlinDefinition(upperBoundDefinitionGenerators?.let { arrayOf(it) } ?: arrayOf(),
            name,
            "${comment?.plus("\n") ?: ""}// typealias $name = $aliasedTo",
            null,
            setOf())
}
