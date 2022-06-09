package com.zdy.project.wechat_chatroom_helper.wechat.utils


fun Any.printAllField(): String {
    val clazz = this::class.java
    val joinToString = clazz.fields.joinToString {
        val value = it[this]
        "[${it.name} = $value]"
    }

    return "class = $clazz, value = $this, field = $joinToString"
}

fun Any.printAllDeclaredField(): String {
    val clazz = this::class.java
    val joinToString = clazz.declaredFields.joinToString {
        val value = it[this]
        "[${it.name} = $value]"
    }

    return "class = $clazz, value = $this, field = $joinToString"
}