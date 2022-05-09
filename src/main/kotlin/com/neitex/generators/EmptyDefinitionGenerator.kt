package com.neitex.generators

class EmptyDefinitionGenerator(private val definition: String) : DefinitionGenerator {
    override val name: String = "Unknown"
    override fun toKotlinDefinition(): Pair<String, Array<RequiredImport>> =
        Pair(definition.lines().joinToString(separator = "\n") { """// $it""" }, arrayOf())
}
