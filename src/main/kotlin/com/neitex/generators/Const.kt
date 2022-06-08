package com.neitex.generators

import com.neitex.COMMENT_REGEX
import com.neitex.convertToKotlinTypes
import com.neitex.findImportedThings
import com.neitex.replaceComment

class Const(private val originalDefinition: String) : DefinitionGenerator {
    override val type: GeneratorType
        get() = GeneratorType.CONST
    override val name: String =
        originalDefinition.replaceComment("").replace("const ", "").substringBefore(" ").trim().trimIndent()
    private val comment = COMMENT_REGEX.find(originalDefinition)?.value?.plus("\n") ?: ""

    override fun toKotlinDefinition(): KotlinDefinition {
        val definition = originalDefinition.replaceComment("").trim()
        Regex("const\\s([\\w_\\d]*)\\s*=\\s*\"(.*)\";").find(definition)?.let {
            return KotlinDefinition(
                arrayOf(), name, "${comment}external val $name: String /* ${it.groupValues[2]} */", null, setOf()
            )
        }
        Regex("const\\s([\\w_\\d]*)\\s*=\\s*(\\d*);").find(definition)?.let {
            return KotlinDefinition(
                arrayOf(), name, "${comment}external val $name: Int /* ${it.groupValues[2]} */", null, setOf()
            )
        }
        Regex("const\\s([\\w_\\d]*)\\s*=\\s*\\{\\n").find(originalDefinition.replaceComment("").trim())?.let {
            val upperDefinition = Interface(
                name + "Interface", originalDefinition.substringAfter("{\n").substringBefore("\n};"), false
            )
            return KotlinDefinition(
                arrayOf(upperDefinition), name, "${comment}external val $name: ${upperDefinition.name}", null, setOf()
            )
        }
        return originalDefinition.replace("const", "external val").convertToKotlinTypes().removeSuffix(";").let {
            KotlinDefinition(arrayOf(), name, it, null, it.findImportedThings())
        }
    }

}
