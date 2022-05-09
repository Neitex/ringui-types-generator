package com.neitex

// Some of these definitions are taken from https://github.com/karakum-team/types-kotlin. All credit for those lines goes to contributors :)

internal const val UNIT = "Unit"

const val SUPPRESS_ILLEGAL_CHARS = """@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")"""

internal const val STRING = "String"

val COMMENT_REGEX =
    Regex("(\\/\\*\\*\\n[\\w\\W]*?\\*\\/)\\n") // Regex("(\\/\\*\\*\\n[a-zA-Z0-9\\@\\*\\s\\n\\ \\{\\}\\.\\,\\<\\>]*\\*\\/)\\n")
val UNION_REGEX = Regex("\\s*(\\w*\\ \\|\\ \\w*)")
val FUNCTION_REGEX =
    Regex("([\\w\\d\\_]+)(\\<[\\w\\d\\s\\<\\>\\:\\|\\?\\,]*\\>)?\\(([\\w\\d\\?\\<\\>\\,\\s\\:\\|\\(\\)\\=\\[\\]]*?)\\)\\s*\\:\\s*([\\w\\d\\:\\s\\<\\>\\[\\]\\.\\|\\(\\)\\=]*);")

internal val STANDARD_TYPE_MAP = mapOf(
    "any" to "Any",
    "object" to "Any",
    "{}" to "Any",
    "unknown" to "Any",

    "boolean" to "Boolean",
    "string" to STRING,

    "never" to "Nothing",


    "number" to "Int",

    "void" to UNIT,
    "null" to "Nothing?",
    "undefined" to "Nothing?",

    "Date" to "kotlin.js.Date",

    "false" to "Boolean /* false */",
    "true" to "Boolean /* true */",

    "() => T" to "() -> T",
    "() => Boolean" to "() -> Boolean",
    "=>" to "->",

    "Int | String" to """String /* Int | String */""", //TODO: Fix comments in cases like "fun remove(key: String /* String /* String | Int */ */?)" (components/alert-service/alert-service)
    "String | Int" to """String /* String | Int */""",

    "React.MouseEvent" to "MouseEvent",
    " | Nothing?" to "?",
    "String | Unit" to "Any",
    " extends " to ": ",
    "React.ReactPortal" to "ReactPortal",
    "JSX.Element" to "ReactNode?",
    "val:" to """`val`:"""
)

val STATIC_IMPORTS_MAP = mapOf(
    "HTMLAttributes" to "react.dom.html.HTMLAttributes",
    "PureComponent" to "react.PureComponent",
    "MouseEvent" to "react.dom.MouseEvent",
    "HTMLIFrameElement" to "org.w3c.dom.HTMLIFrameElement",
    "ReactNode" to "react.ReactNode",
    "HTMLDivElement" to "org.w3c.dom.HTMLDivElement",
    "State>" to "react.State",
    "ReactPortal" to "react.ReactPortal",
    "React.ReactPortal" to "react.ReactPortal",
    "HTMLAttributes" to "react.dom.html.HTMLAttributes",
    " Component" to "react.Component",
    "HTMLSpanElement" to "org.w3c.dom.HTMLSpanElement",
    "NativeMouseEvent" to "react.dom.events.NativeMouseEvent",
    "MouseEvent" to "react.dom.events.MouseEvent",
    "HTMLElement" to "org.w3c.dom.HTMLElement",
    "Theme" to "ringui.global.Theme", // it is not really static, but it is always generated
    "ReadonlyArray" to "kotlinx.js.ReadonlyArray",
    "Props" to "react.Props",
    "Node" to "org.w3c.dom.Node",
    "Component" to "react.Component",
    "Record" to "kotlinx.js.Record"
)

val OVERRIDDEN_PROPERTIES = listOf("render", "componentDidMount", "componentWillUnmount")

val DYNAMIC_CHANGES_MAP = mutableMapOf<String, String>()

val REGEX_REPLACERS = mapOf<Regex, (MatchResult) -> CharSequence>(Regex("(?>readonly\\s)*([\\w]*)(?>\\[\\])") to {
    "${if (it.groupValues.first().startsWith("readonly")) "Readonly" else ""}Array<${
        it.groupValues.first().replaceAll(listOf(Pair("readonly ", ""), Pair("[]", "")))
    }>"
}, Regex("\\?:\\s?") to {
    ": "
}, Regex("(?>MouseEvent\\<)([\\w\\[\\]\\:\\.\\s\\,\\<\\>]*)\\>") to {
    "MouseEvent<${it.groupValues[1]}, NativeMouseEvent>"
}, Regex("(\\[[a-zA-Z\\s\\|0-9\\,\\<\\>\\[\\]\\|\\?]+\\])") to {
    """dynamic /* ${it.groupValues[1]} */"""
}, UNION_REGEX to {
    """dynamic /* ${it.groupValues.drop(1).joinToString(separator = ",")} */"""
}, Regex("(typeof [\\w\\[\\]\\:\\.\\s\\,\\<\\>]*)") to {
    """dynamic /* ${it.groupValues[1]} */"""
}, Regex("(keyof [\\w\\[\\]\\:\\.\\d]*)") to {
    """Any /* ${it.groupValues[1]} */"""
}, Regex("PureComponent<([\\w\\W]*?)>") to {
    "PureComponent<${it.groupValues[1].removeSuffix(", State")}, State>"
}, Regex("HTMLAttributes<([\\w\\_\\,\\<\\>]*?), State>") to {
    "HTMLAttributes<${it.groupValues[1]}>"
}, Regex("Partial<([\\w\\W]*?)>") to {
    it.groupValues[1]
}, Regex("Component<([\\w\\_\\<\\>]*?)>") to {
    "Component<${it.groupValues[1]}, State>"
})
