package com.neitex.generators

import com.neitex.STATIC_IMPORTS_MAP
import java.io.File

private val notThatComplexRegexToMatchTypeAliasesAndEnums =
    Regex(
        "(?>export)?\\s?(?>declare)?\\s?(?>default)?\\s?(?>(?>type)|(?>enum))\\s+(?>[\\w\\d\\_\\<\\>]*)\\s+(?>\\=|\\:)?\\s?(?:(?:\\{[\\w\\W\\n]*?^\\})|(?:[a-zA-Z\\<\\\"\\=\\>\\[\\]\\;]+))",
        setOf(RegexOption.MULTILINE)
    )

private val reallyComplexRegexToMatchInterfacesAndClasses =
    // "Anything can be achieved with complex enough Regular expression" (c) someone
    Regex(
        "((?:export)?\\s*(?:declare)?\\s*(?:default)?\\s*(?:(?:interface)|(?:class)|(?:global))\\s*(?:[\\w\\d\\_\\<\\>]*)\\s*(?:(?:extends)|(?:implements))?(?:[\\w\\d\\<\\>\\,\\W\\S]*?)\\s*(?:\\{[\\w\\W\\n]*?^\\}))",
        setOf(RegexOption.MULTILINE)
    )

data class FileDefinition(val file: File, val module: String?, val Package: KotlinPackage) {
    private fun String.splitFileBody() =
        notThatComplexRegexToMatchTypeAliasesAndEnums.findAll(this).map { it.value }.toList()
            .plus(reallyComplexRegexToMatchInterfacesAndClasses.findAll(this).map { it.value }.toList())

    private val definition = file.readLines().filterNot { it.startsWith("import") }.joinToString(separator = "\n")

    fun writeToFile(file: File) {
        val (childDefinitions, imports) = definition.splitFileBody().map {
            parseExpression(it)
        }.map {
            it.toKotlinDefinition()
        }.let {
            Pair(it.map { it.first }, it.map { it.second }.flatMap { it.toList() }.toSet())
        }
        file.writeText("""${
            module?.let {
                """@file:JsModule("$it")
@file:JsNonModule"""
            } ?: ""
        }
package ${Package.packagePath}
${
            imports.joinToString(separator = "\n", prefix = "\n", postfix = "\n") {
                "import ${STATIC_IMPORTS_MAP[it]}"
            }
        }
${
            childDefinitions.joinToString(separator = "\n") {
                it
            }
        }
""")
    }
}
