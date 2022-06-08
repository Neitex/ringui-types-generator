package com.neitex.generators

import com.neitex.DYNAMIC_IMPORTS
import com.neitex.STATIC_IMPORTS_MAP
import java.io.File

private val notThatComplexRegexToMatchTypeAliasesAndEnums = Regex(
    "(?>export)?\\s?(?>declare)?\\s?(?>default)?\\s?(?>(?>type)|(?>enum))\\s+(?>[\\w\\d_<>]*)\\s+(?>[=:])?\\s?(?:\\{[\\w\\W\\n]*?^}|[a-zA-Z<\"=>\\[\\];]+)",
    setOf(RegexOption.MULTILINE)
)

private val reallyComplexRegexToMatchInterfacesAndClasses =
// "Anything can be achieved with complex enough Regular expression"
    //                                              (c) someone
    Regex(
        "((?:export)?\\s*(?:declare)?\\s*(?:default)?\\s*(?:interface|class|global)\\s*[\\w\\d_<>]*\\s*(?:extends|implements)?[\\w\\d<>,\\W\\S]*?\\s*\\{[\\w\\W\\n]*?^})",
        setOf(RegexOption.MULTILINE)
    )

data class FileDefinition(val file: File, val module: String?, val Package: KotlinPackage) {
    private fun String.splitFileBody() =
        notThatComplexRegexToMatchTypeAliasesAndEnums.findAll(this).map { it.value }.toList()
            .plus(reallyComplexRegexToMatchInterfacesAndClasses.findAll(this).map { it.value }.toList())

    private val definition = file.readLines().filterNot { it.startsWith("import") }.joinToString(separator = "\n")

    fun writeToFile(file: File) {
        val generators = definition.splitFileBody().map {
            parseExpression(it)
        }
        val definedElements = generators.map { it.name }
        val (childDefinitions, imports) = generators.map {
            it.toKotlinDefinition()
        }.let {
            Pair(it.map { it.additionalDefinitions }.flatMap { it.toList() }.distinctBy { it.name },
                it.map { it.imports }.flatMap { it.toList() }.filterNot { definedElements.contains(it) }
                    .map { "${STATIC_IMPORTS_MAP[it] ?: (DYNAMIC_IMPORTS[it])}" }
                    .filter { !it.startsWith(Package.packagePath) }.toSet()
            )
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
                "import $it"
            }.let { if (it.isNotBlank()) "$it\n" else ""}
        }${
            childDefinitions.joinToString(separator = "\n") {
                it.toKotlinDefinition().toString()
            }
        }
${
            generators.map { it.toKotlinDefinition().copy(additionalDefinitions = arrayOf()) }
                .joinToString(separator = "\n") { it.toString() }
        }
"""
        )
        definedElements.forEach {
            DYNAMIC_IMPORTS[it] = "${Package.packagePath}.$it"
            DYNAMIC_IMPORTS.remove("")
        }
    }
}
