package com.neitex.generators

import com.neitex.*

data class Class(private val originalDefinition: String) : DefinitionGenerator {
    override val type: GeneratorType
        get() = GeneratorType.CLASS
    override val name =
        originalDefinition.substringAfter("class ").substringBefore(" ").substringBefore("<").removeSuffix(":")
    private val generic = originalDefinition.substringBefore("{").substringAfter(name).substringBefore("implements")
        .substringBefore("extends").substringAfter("<").substringBefore(">").takeIf {
            it.isNotBlank()
        }?.convertToKotlinTypes()?.let {
            if (it.contains("=")) {
                val defaults = Regex("(\\w) ?= ([\\w\\[\\]<>]+,?)").findAll(it).map {
                    Pair(it.groups[1]!!.value, it.groups[2]!!.value)
                }.toList()
                DYNAMIC_REGEX_REPLACERS.put(Regex("$name ?(?![<\\w])")) {
                    "$name${defaults.joinToString(separator = ", ", prefix = "<", postfix = ">") { it.second }}"
                }
                it.replaceAll(defaults.map { Pair("${it.first} = ${it.second}", "${it.first}/* = ${it.second}*/") })
            } else it
        }
    private val implementsOrExtends =
        Regex("(?>(?>implements)|(?>extends))\\s(\\w*[<>\\w\\s,:]*?)?(?>\\s*\\{)").find(originalDefinition)?.groupValues?.getOrNull(
            1
        )?.replaceAll(listOf(Pair("extends ", ""), Pair("implements ", "")))?.removeSuffix("{")?.trim()?.trimIndent()
            ?.convertToKotlinTypes()

    private val nestedGenerators =
        originalDefinition.replaceComment("").substringAfter("{").substringBeforeLast("}").splitBody()
            .map { parseExpression(it) }
    private val comment = COMMENT_REGEX.find(originalDefinition.substringBefore("class $name"))?.groupValues?.get(1)

    override fun toKotlinDefinition(): KotlinDefinition = KotlinDefinition(
        arrayOf(),
        name,
        "${comment?.plus("\n") ?: ""}external class $name${generic?.let { "<$it> " } ?: ""}${implementsOrExtends?.let { ": $it " } ?: ""}",
        nestedGenerators.joinToString(separator = "\n", prefix = "", postfix = "") {
            if (implementsOrExtends != null) it.toKotlinDefinition()
                .toString(implementsOrExtends.replaceComment("").substringBefore("<").trim()).prependIndent("    ")
            else it.toKotlinDefinition().toString().prependIndent("    ")
        },
        nestedGenerators.map { it.toKotlinDefinition().imports }.flatten()
            .toSet() + (implementsOrExtends?.findImportedThings() ?: setOf())
    )

}
