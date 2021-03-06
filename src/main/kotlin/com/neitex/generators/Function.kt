package com.neitex.generators

import com.neitex.*
import com.neitex.STANDARD_TYPE_MAP

data class Function(private val originalDefinition: String) : DefinitionGenerator {
    override val type: GeneratorType
        get() = GeneratorType.FUNCTION
    private val parsedFunction =
        FUNCTION_REGEX.find(originalDefinition.replaceComment("").replaceAll(typescriptGarbage.map { Pair(it, "") }))!!
    private val comment = COMMENT_REGEX.find(originalDefinition)?.value?.plus("\n") ?: ""
    override val name = parsedFunction.groupValues[1]
    private val customTypes = parsedFunction.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }
    private val argumentsList: List<Pair<String, String>>? =
        parsedFunction.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }?.splitArguments()?.mapIndexed { index, it ->
            Pair(
                it.first.treatArgumentName("argument$index"), it.second.treatArgumentType()
            )
        }
    private val returnType =
        parsedFunction.groupValues.getOrNull(4)?.convertToKotlinTypes()?.removeSuffix(";")?.takeIf { it.isNotBlank() }

    override fun toKotlinDefinition(): KotlinDefinition = KotlinDefinition(
        arrayOf(),
        name,
        "${comment}fun ${
            (customTypes?.convertToKotlinTypes()?.let {
                "$it "
            }) ?: ""
        }$name${
            argumentsList?.joinToString(separator = ", ", prefix = "(", postfix = ")") { argument ->
                "${argument.first.replace("?", "")}: ${
                    argument.second.replaceAll(STANDARD_TYPE_MAP.map { it.toPair() }).let {
                        if (argument.first.endsWith("?") && !it.endsWith("?")) "$it?" else it
                    }
                }"
            } ?: "()"
        }${if (returnType?.isNotBlank() == true && returnType != "Unit") ": $returnType" else ""}",
        null,
        (argumentsList?.map { it.second.findImportedThings() }?.flatten()?.toSet()
            ?: setOf()) + (returnType?.findImportedThings() ?: setOf())
    )

}
