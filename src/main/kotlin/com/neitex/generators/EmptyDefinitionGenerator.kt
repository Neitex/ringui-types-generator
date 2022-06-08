package com.neitex.generators

class EmptyDefinitionGenerator(private val definition: String) : DefinitionGenerator {
    override val type: GeneratorType
        get() = GeneratorType.EMPTY
    override val name: String =
        definition.substringAfter("private").substringAfter("static").substringAfter("state").substringBefore(" ")

    override fun toKotlinDefinition(): KotlinDefinition = KotlinDefinition(
        arrayOf(), name, "// IGNORED BY CONVERTER\n${
            definition.lines().joinToString(separator = "\n") {
                "// $it"
            }
        }", null, setOf()
    )
}
