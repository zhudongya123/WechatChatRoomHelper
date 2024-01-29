package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

fun <T : Any> toMap(obj: T): Map<String, Any?> {
    return (obj::class as KClass<T>).memberProperties.associate { prop ->
        prop.name to prop.get(obj)?.let { value ->
            if (value::class.isData) {
                toMap(value)
            } else {
                value
            }
        }
    }
}