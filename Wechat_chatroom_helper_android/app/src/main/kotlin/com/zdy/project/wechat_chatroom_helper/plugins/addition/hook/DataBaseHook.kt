package com.zdy.project.wechat_chatroom_helper.plugins.addition.hook

import android.content.ContentValues
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.plugins.addition.Clazz
import com.zdy.project.wechat_chatroom_helper.plugins.addition.SpecialPluginEntry.Companion.DB
import com.zdy.project.wechat_chatroom_helper.plugins.addition.SpecialPluginEntry.Companion.DBF
import com.zdy.project.wechat_chatroom_helper.plugins.addition.SpecialPluginEntry.Companion.DBSIGN
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object DataBaseHook {

    lateinit var msgDataBase: Any
    lateinit var msgDataBaseFactory: Any
    lateinit var C: Clazz


     fun hook(classLoader: ClassLoader) {
        LogUtils.log("Msghook1")

        val DBClass = XposedHelpers.findClass(DB, classLoader)


        XposedHelpers.findAndHookMethod(DBClass, "rawQueryWithFactory",
                XposedHelpers.findClass(DBF, classLoader), C.String, C.StringArray, C.String,
                XposedHelpers.findClass(DBSIGN, classLoader), object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {

                msgDataBase = param.thisObject
                if (param.args[0] != null)
                    msgDataBaseFactory = param.args[0]


                val thisObject = param.thisObject
                val factory = param.args[0]
                val sql = param.args[1] as String
                val selectionArgs = param.args[2] as Array<String>?
                val editTable = param.args[3] as String?
                val cancellation = param.args[4]

                LogUtils.log("QWEA, Msghook, onDatabaseQuerying, sql = $sql, selectionArgs  = ${selectionArgs?.joinToString { it }},  ")

            }

        })

        XposedHelpers.findAndHookMethod(DBClass, "insertWithOnConflict",
                C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {
                msgDataBase = param.thisObject

                val thisObject = param.thisObject
                val table = param.args[0] as String
                val nullColumnHack = param.args[1] as String?
                val initialValues = param.args[2] as ContentValues?
                val conflictAlgorithm = param.args[3] as Int

                LogUtils.log("QWEA, Msghook, onDatabaseInserted, table = $table ,nullColumnHack = $nullColumnHack ,initialValues = $initialValues, conflictAlgorithm = $conflictAlgorithm")

            }

        })

        XposedHelpers.findAndHookMethod(
                DBClass, "delete",
                C.String, C.String, C.StringArray, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val thisObject = param.thisObject
                val table = param.args[0] as String
                val whereClause = param.args[1] as String?
                val whereArgs = param.args[2] as Array<String>?

                LogUtils.log("QWEA, Msghook, onDatabaseDeleted, table = $table ,whereClause = $whereClause ," +
                        "whereArgs = ${if (whereArgs != null && !whereArgs.isEmpty()) whereArgs.joinToString { it } else ""}")
            }

        })

        XposedHelpers.findAndHookMethod(
                DBClass, "updateWithOnConflict",
                C.String, C.ContentValues, C.String, C.StringArray, C.Int, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {
                val thisObject = param.thisObject
                val table = param.args[0] as String
                val values = param.args[1] as ContentValues
                val whereClause = param.args[2] as String?
                val whereArgs = param.args[3] as Array<String>?
                val conflictAlgorithm = param.args[4] as Int

                LogUtils.log("QWEA, Msghook, onDatabaseDeleted, table = $table , values = $values, whereClause = $whereClause ," +
                        "whereArgs = ${if (whereArgs != null && !whereArgs.isEmpty()) whereArgs.joinToString { it } else ""}, conflictAlgorithm = $conflictAlgorithm")
            }
        })

        XposedHelpers.findAndHookMethod(
                DBClass, "executeSql",
                C.String, C.ObjectArray, XposedHelpers.findClass(DBSIGN, classLoader), object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {
                val thisObject = param.thisObject
                val sql = param.args[0] as String
                val bindArgs = param.args[1] as Array<Any?>?
                val cancellation = param.args[2]

                LogUtils.log("QWEA, Msghook, onDatabaseDeleted, sql = $sql ," +
                        "bindArgs = ${if (bindArgs != null && bindArgs.isEmpty()) bindArgs.joinToString { it.toString() } else ""}, cancellation = $cancellation")
            }
        })
    }
}