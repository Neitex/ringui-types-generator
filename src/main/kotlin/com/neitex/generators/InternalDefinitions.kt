package com.neitex.generators

import com.neitex.FUNCTION_REGEX
import com.neitex.OVERRIDDEN_PROPERTIES
import com.neitex.FINAL_PROPERTIES
import com.neitex.replaceAll
import com.neitex.replaceComment

typealias RequiredImport = String

val typescriptGarbage = listOf("export", "declare", "default")

interface DefinitionGenerator {
    val name: String
    val type: GeneratorType
    fun toKotlinDefinition(): KotlinDefinition
}

/**
 * @param defImports Imports of definition itself - imports of additional definitions will be appended automatically
 */
data class KotlinDefinition(
    val additionalDefinitions: Array<DefinitionGenerator>,
    val name: String,
    val header: String,
    val body: String?,
    private val defImports: Set<RequiredImport>
) {
    val imports: Set<RequiredImport> =
        (additionalDefinitions.map { it.toKotlinDefinition().imports }.flatten() + defImports).toSet()

    /**
     * Generates String representation of definition
     */
    override fun toString(): String {
        return """${
            additionalDefinitions.joinToString(separator = "\n") {
                it.toKotlinDefinition().toString()
            }
        }$header${
            body?.let {
                " {\n$it\n}"
            } ?: ""
        }
        """
    }

    fun toString(extendedFrom: String): String {
        return """${
            additionalDefinitions.joinToString(separator = "\n") {
                it.toKotlinDefinition().toString(extendedFrom)
            }
        }${
            if (OVERRIDDEN_PROPERTIES[extendedFrom]?.contains(
                    name
                ) == true
            ) "override " else ""
        }$header${
            body?.let {
                " {\n$it\n}"
            } ?: ""
        }
        """.let {
            if (FINAL_PROPERTIES[extendedFrom]?.contains(name) == true) it.lines().joinToString(separator = "\n") {
                "// $it"
            }
            else it
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KotlinDefinition

        if (!additionalDefinitions.contentEquals(other.additionalDefinitions)) return false
        if (name != other.name) return false
        if (header != other.header) return false
        if (body != other.body) return false

        return true
    }

    override fun hashCode(): Int {
        var result = additionalDefinitions.contentHashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + header.hashCode()
        result = 31 * result + body.hashCode()
        return result
    }

}

data class KotlinPackage(private val packageString: String) {
    val physicalPath = packageString.replace("-", "_").replace(".", "/")
    val packagePath = packageString.replace("-", "_")
    override fun toString(): String = packagePath
}

fun parseExpression(expression: String): DefinitionGenerator {
    val definition =
        expression.replaceAll(typescriptGarbage.map { Pair(it, "") }).replaceComment("").trim().trimIndent()
    return when {
        definition.startsWith("type ") -> TypeAlias(expression)
        definition.startsWith("interface") -> Interface(expression)
        definition.startsWith("class ") -> Class(expression)
        definition.startsWith("enum") -> Enum(expression)
        definition.startsWith("global ") -> parseExpression(expression.substringAfter("{\n").substringBefore("}"))
        definition.contains(Regex("^ *(private\\s?)|(static\\s?)|(state\\s?\\{)")) -> EmptyDefinitionGenerator(
            expression
        )
        definition.startsWith("constructor") || definition.startsWith("new ") -> Constructor(expression)
        definition.startsWith("const") -> Const(expression)
        definition.contains(FUNCTION_REGEX) -> Function(
            expression
        )
        else -> Value(expression)
    }
}


// List of Kotlin primitive types
val kotlinPrimitiveTypes = listOf(
    "Boolean", "Byte", "Char", "Double", "Float", "Int", "Long", "Short", "String"
)

// List of Kotlin/JS specific types
val kotlinJsTypes = listOf(
    "Array",
    "Date",
    "Error",
    "Function",
    "Map",
    "Number",
    "Object",
    "Promise",
    "RegExp",
    "Set",
    "String",
    "Symbol",
    "WeakMap",
    "WeakSet"
)
