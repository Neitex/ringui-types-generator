package com.neitex

class LambdaType(definition: String) {
    val inputType: String = LAMBDA_REGEX.find(definition)?.groupValues?.get(1) ?: ""
    val outputType: String = (LAMBDA_REGEX.find(definition)?.groupValues?.get(2) ?: "").let {
        if (LAMBDA_REGEX.containsMatchIn(it)) {
            "(${LambdaType(it)})"
        } else {
            it
        }
    }

    override fun toString(): String = "(${inputType.convertToKotlinTypes()}) -> ${outputType.convertToKotlinTypes()}"
}
