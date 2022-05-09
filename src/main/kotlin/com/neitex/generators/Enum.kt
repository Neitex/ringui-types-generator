package com.neitex.generators

import com.neitex.replaceAll
import com.neitex.replaceComment

data class Enum(private val originalDefinition: String) : DefinitionGenerator {
    override val name: String =
        originalDefinition.replaceComment("").replaceAll(typescriptGarbage.map { Pair(it, "") } + Pair(
            "enum", ""
        )).substringBefore("{").trimIndent().trim()
    private val bodySplit =
        originalDefinition.replaceComment("").substringAfter("{").substringBefore("}").split(",")
            .map { it.split("=").let { Pair(it.first().trim().trimIndent(), it[1].trimIndent().trim()) } }

    private val annotation = """@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")"""+"\n"+ "@JsName(/* Enum */ ${
        bodySplit.joinToString(
            prefix = "\"\"\"({",
            postfix = "})\"\"\"",
            separator = ", "
        ) {
            "${it.first}: '${it.second.replace("\"", "")}'"
        }
    } /* Enum */)"

    override fun toKotlinDefinition(): Pair<String, Array<RequiredImport>> = Pair(
        "$annotation\nexternal enum class $name ${
            bodySplit.joinToString(
                separator = ",\n",
                prefix = "{\n",
                postfix = "\n}"
            ) {
                it.first
            }
        }", arrayOf()
    )

}
