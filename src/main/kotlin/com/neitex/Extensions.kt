package com.neitex

import java.util.*
import kotlin.random.Random
import kotlin.random.asJavaRandom

private val MINUS_LETTER = Regex("""-(\w)""")
private val SPACE_LETTER = Regex("""\s(\w)""")

private val toUpperCase: (MatchResult) -> CharSequence = {
    it.groupValues[1].uppercase(Locale.getDefault())
}

internal fun String.kebabToCamel(): String = replace(MINUS_LETTER, toUpperCase).replace(SPACE_LETTER, toUpperCase)

fun String.replaceAll(replaceList: List<Pair<String, String>>) = replaceList.fold(this) { string, replacement ->
    string.replace(replacement.first, replacement.second)
}

fun String.toLegalJsName() = if ('-' in this) "`${this.replace("'", "")}`" else this

fun String.findImportedThings() =
    STATIC_IMPORTS_MAP.keys.filter { this.contains(it) }.let { list ->
        list + DYNAMIC_IMPORTS.keys.filter { !list.contains(it) && this.contains(it) }
    }.toSet()

/**
 * Takes insides of brackets: constructor( INSIDES.splitArguments() )
 */
fun String.splitArguments() = if (this.isNotBlank()) {
    val levelDownCharacters = sequenceOf('(', '<')
    val levelUpCharacters = sequenceOf(')', '>')

    val argumentsList = mutableListOf<Pair<String, String>>()
    var currentDeepLevel = 0
    var name = ""
    var passedName = false
    var type = ""
    for (character in (this + "\n")) {
        if (character == '\n') {
            argumentsList += Pair(name.trim(), type.trim())
            continue
        }
        if (character == ':') {
            passedName = true
            continue
        }
        if (!passedName) {
            name += character
            continue
        }
        if (character in levelDownCharacters) {
            type += character
            currentDeepLevel++
            continue
        } else if (character in levelUpCharacters) {
            type += character
            currentDeepLevel--
            continue
        }
        if (character == ',') {
            if (currentDeepLevel == 0) {
                argumentsList += Pair(name.trim(), type.trim())
                name = ""
                type = ""
                passedName = false
            } else {
                type += character
            }
            continue
        }
        type += character
    }
    argumentsList
} else listOf()

/**
 * Note: Assumes code is properly formatted and there is no such cases as "...class uwu { {...} }" (multiple brackets on a single line)
 */
fun String.splitBody(): List<String> {
    val randomEndNote = Random.asJavaRandom().nextLong().toString()
    val expressionsList = mutableListOf<String>()
    var currentLevel = 0
    var currentExpression = ""
    for (string in ("$this\n$randomEndNote").lines()) {
        if (string == randomEndNote) {
            expressionsList += currentExpression
            continue
        }
        currentExpression += "\n$string"
        if (string.trim().contains("{"))
            currentLevel++
        else if (string.trim().contains("}"))
            currentLevel--
        if (string.trim().endsWith(";")) {
            if (currentLevel == 0) {
                expressionsList += currentExpression.takeIf { it.isNotBlank() } ?: string
                currentExpression = ""
            }
            continue
        }
    }
    return expressionsList
        .filter { it.isNotBlank() }
}

fun String.replaceComment(newValue: String) = this.replace(COMMENT_REGEX, newValue)

// I don't like my naming either.
fun String.treatArgumentType() =
    Regex(".*&.*").replace(this) {
        "dynamic /* ${it.value} */"
    }.convertToKotlinTypes()
// Main reason for this function to exist is that we may not fear to get out of scope of the argument while using Regex.
// Whole string is supposed to be an argument, so we may do whatever we want with it :).


fun String.treatArgumentName(nameIfInvalid: String) =
    if (this.startsWith("{")) nameIfInvalid else this


fun String.convertToKotlinTypes() =
    this.replaceAll(STANDARD_TYPE_MAP.map { it.toPair() } + Pair("??", "?") + Pair("?:", ":") /* (event?:...)  */).let {
        REGEX_REPLACERS.entries.fold(it) { string, replacer ->
            string.replace(replacer.key, replacer.value)
        }
    }.replaceAll(DYNAMIC_GENERATED_TYPES.map { Pair(it.key, it.value) }).let {
        DYNAMIC_REGEX_REPLACERS.entries.fold(it) { string, replacer ->
            string.replace(replacer.key, replacer.value)
        }
    }
