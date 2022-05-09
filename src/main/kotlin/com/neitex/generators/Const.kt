package com.neitex.generators

import com.neitex.COMMENT_REGEX
import com.neitex.convertToKotlinTypes
import com.neitex.findImportedThings
import com.neitex.replaceComment

class Const(private val originalDefinition: String) : DefinitionGenerator {
    override val name: String =
        originalDefinition.replaceComment("").replace("const ", "").substringBefore(" ").trim().trimIndent()
    private val comment = COMMENT_REGEX.find(originalDefinition)?.value?.plus("\n") ?: ""

    override fun toKotlinDefinition(): Pair<String, Array<RequiredImport>> {
        Regex("const\\s([\\w\\_\\d]*)\\s*\\=\\s*\\\"(.*)\\\";").find(originalDefinition.replaceComment("").trim())
            ?.let {
                return Pair("${comment}external val $name: String /* ${it.groupValues[2]} */", arrayOf())
            }
        Regex("const\\s([\\w\\_\\d]*)\\s*\\=\\s*(\\d*);").find(originalDefinition.replaceComment("").trim())?.let {
            return Pair("${comment}external val $name: Int /* ${it.groupValues[2]} */", arrayOf())
        }
        Regex("const\\s([\\w\\_\\d]*)\\s*\\=\\s*\\{\\n").find(originalDefinition.replaceComment("").trim())?.let {
            val upperDefinition = Interface(
                name + "Interface",
                originalDefinition.substringAfter("{\n").substringBefore("\n};"),
                false
            ).toKotlinDefinition()
            return Pair("${upperDefinition.first}\n${comment}external val $name: ${name + "Interface"}", upperDefinition.second)
        }
        return originalDefinition.replace("const", "external val").convertToKotlinTypes().removeSuffix(";").let {
            Pair(it, it.findImportedThings().toTypedArray())
        }
    }

}
