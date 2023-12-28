package com.zdy.project.wechat_chatroom_helper.wechat.utils

import java.lang.reflect.Modifier


fun Any.printAllField(): String {
    val clazz = this::class.java
    val joinToString = clazz.fields.filter { !Modifier.isStatic(it.modifiers) }.joinToString {
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