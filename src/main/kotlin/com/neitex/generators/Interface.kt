package com.neitex.generators

import com.neitex.*
import com.neitex.STANDARD_TYPE_MAP

class Interface : DefinitionGenerator {
    override val name: String
    private val extendsFrom: String?
    private var nestedGenerators: List<DefinitionGenerator>
    private val isExternal: Boolean

    constructor(originalDefinition: String) {
        name = originalDefinition.substringBefore("{")
            .replaceAll(typescriptGarbage.map { Pair(it, "") } + Pair("interface", "")).substringBefore("extends")
            .trim().trimIndent().toLegalJsName()
        nestedGenerators =
            originalDefinition.substringAfter("{").substringBeforeLast("}").trim().trimIndent().splitBody()
                .map { parseExpression(it) }
        extendsFrom =
            (if ("extends" !in originalDefinition.substringBefore("{")) null else {
                originalDefinition.substringAfter("extends").substringBefore("{").trim()
                    .replaceAll(STANDARD_TYPE_MAP.map { it.toPair() }).convertToKotlinTypes()
            }).let {
                if (name.endsWith("Props"))
                    "${it?.plus(", ") ?: ""}Props"
                else it
            }
        isExternal = true
    }

    constructor(name: String, originalBodyDefinition: String, isExternal: Boolean) {
        this.name = name
        extendsFrom = null
        nestedGenerators = originalBodyDefinition.trim().trimIndent().splitBody().map { parseExpression(it) }
        this.isExternal = isExternal
    }

    override fun toKotlinDefinition(): Pair<String, Array<RequiredImport>> {
        val definitions = nestedGenerators.map { it.toKotlinDefinition() }
        return Pair("${if (isExternal) "external " else ""}interface $name ${if (extendsFrom != null) ": $extendsFrom" else ""} ${
            definitions.map { it.first }.joinToString(separator = "\n", prefix = "{\n", postfix = "\n}") {
                it.prependIndent("    ")
            }
        }", definitions.map { it.second }.flatMap { it.toSet() }.toList().toTypedArray()
        )
    }
}
