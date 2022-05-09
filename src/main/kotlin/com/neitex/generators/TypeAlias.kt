package com.neitex.generators

import com.neitex.*

data class TypeAlias(private val originalDefinition: String) : DefinitionGenerator {
    override val name: String =
        originalDefinition.substringBefore("=").replaceAll(typescriptGarbage.map { Pair(it, "") } + Pair("type", ""))
            .dropLast(1).trim().trimIndent().toLegalJsName()
    private val upperBoundDefinitionGenerators = originalDefinition.substringAfter("=").trim().trimIndent().let {
        if (it.startsWith("{\n")) Interface(
            "${name}Interface", it.substringAfter("{\n").substringBeforeLast("\n}"), false
        )
        else null
    }
    private val aliasedTo =
        (upperBoundDefinitionGenerators?.name ?: originalDefinition.substringAfter("=").trim().trimIndent()
            .convertToKotlinTypes().removeSuffix(";")).let {
            DYNAMIC_CHANGES_MAP[name] = it
            it
        }

    override fun toKotlinDefinition(): Pair<String, Array<RequiredImport>> {
        return Pair(
            "${upperBoundDefinitionGenerators?.toKotlinDefinition()?.first?.plus("\n") ?: ""}// typealias ${name.toLegalJsName()} = ${
                upperBoundDefinitionGenerators?.name ?: aliasedTo
            }", upperBoundDefinitionGenerators?.toKotlinDefinition()?.second ?: arrayOf()
        )
    }
}
