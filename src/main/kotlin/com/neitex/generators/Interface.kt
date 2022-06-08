package com.neitex.generators

import com.neitex.*

class Interface : DefinitionGenerator {
    override val type: GeneratorType
        get() = GeneratorType.INTERFACE
    override val name: String
    private val extendsFrom: String?
    private val genericTypes: String?
    private var nestedGenerators: List<DefinitionGenerator>
    private val isExternal: Boolean

    constructor(originalDefinition: String) {
        name = originalDefinition.replaceComment("").substringBefore("{")
            .replaceAll(typescriptGarbage.map { Pair(it, "") } + Pair("interface", "")).substringBefore("extends")
            .trim().trimIndent().toLegalJsName()
        genericTypes =
            originalDefinition.replaceComment("").substringBefore("{").substringBefore("extends").substringAfter(name)
                .trimIndent().trim().takeIf { it.startsWith("<") }?.convertToKotlinTypes()
        nestedGenerators =
            originalDefinition.replaceComment("").substringAfter("{").substringBeforeLast("}").trim().trimIndent()
                .splitBody().map { parseExpression(it) }
        extendsFrom = (if ("extends" !in originalDefinition.replaceComment("").substringBefore("{")) null else {
            originalDefinition.replaceComment("").substringAfter("extends").substringBefore("{").trim()
                .replaceAll(STANDARD_TYPE_MAP.map { it.toPair() }).convertToKotlinTypes()
        }).let {
            when {
                name == "State" -> "${it?.plus(", ") ?: ""}react.State"
                name.endsWith("Props") -> "${it?.plus(", ") ?: ""}Props"
                else -> it
            }
        }
        isExternal = true
    }

    constructor(name: String, originalBodyDefinition: String, isExternal: Boolean) {
        this.name = name
        extendsFrom = null
        genericTypes = null
        nestedGenerators = originalBodyDefinition.trim().trimIndent().splitBody().map { parseExpression(it) }
        this.isExternal = isExternal
    }

    override fun toKotlinDefinition(): KotlinDefinition {
        val definitions = nestedGenerators.map { it.toKotlinDefinition() }
        val definition = KotlinDefinition(arrayOf(),
            name,
            "${if (isExternal) "external " else ""}interface $name${genericTypes ?: ""}${if (extendsFrom != null) ": $extendsFrom" else ""}",
            definitions.map {
                if (it.name == "constructor")
                    "// $it"
                else it.toString()
            }.joinToString(separator = "", prefix = "", postfix = "") {
                it.lines().map { if (!it.startsWith("    ")) it.prependIndent("    ") else it }.joinToString(separator = "\n")
            },
            definitions.map { it.imports }.flatten().toSet() + (extendsFrom?.findImportedThings() ?: setOf())
        )
        return definition
    }
}
