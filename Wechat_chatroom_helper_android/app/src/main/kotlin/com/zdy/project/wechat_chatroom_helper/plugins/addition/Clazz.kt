package com.zdy.project.wechat_chatroom_helper.plugins.addition

import de.robv.android.xposed.XposedHelpers

class Clazz(classLoader: ClassLoader) {
    val m = XposedHelpers.findClass("com.tencent.mm.pluginsdk.model.m", classLoader)
    val au = XposedHelpers.findClass("com.tencent.mm.model.au", classLoader)
    val NearbySayHiListUI = XposedHelpers.findClass("com.tencent.mm.plugin.nearby.ui.NearbySayHiListUI", classLoader)
    val c = XposedHelpers.findClass("com.tencent.mm.model.c", classLoader)


    val Boolean = Boolean::class.java
    val Int = Int::class.java
    val Iterator = java.util.Iterator::class.java
    val Long = Long::class.java
    val Map = Map::class.java
    val Object = Object::class.java
    val String = String::class.java

    val Bundle = android.os.Bundle::class.java
    val ContentValues = android.content.ContentValues::class.java
    val Context = android.content.Context::class.java
    val View = android.view.View::class.java
    val ViewGroup = android.view.ViewGroup::class.java

    val ByteArray = ByteArray::class.java
    val IntArray = IntArray::class.java
    val ObjectArray = Array<Any>::class.java
    val StringArray = Array<String>::class.java
}