package com.zdy.project.wechat_chatroom_helper.plugins

import android.app.Activity
import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import com.gh0u1l5.wechatmagician.spellbook.C
import com.zdy.project.wechat_chatroom_helper.LogUtils
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class AddContactLBSMessageHandler : IXposedHookLoadPackage {


    val Logclass = "com.tencent.mm.sdk.platformtools.x"

    val DB = "com.tencent.wcdb.database.SQLiteDatabase"
    val DBF = "com.tencent.wcdb.database.SQLiteDatabase\$CursorFactory"
    val DBSIGN = "com.tencent.wcdb.support.CancellationSignal"

    lateinit var classLoader: ClassLoader


    lateinit var msgDataBase: Any
    var msgDataBaseFactory: Any? = null

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {


        classLoader = lpparam.classLoader

        if (lpparam.processName != "com.tencent.mm") return
//        if (!lpparam.processName .contains("dkmodel")) return


//        try {
//            XposedHelpers.findClass(DB, lpparam.classLoader)
//            hookLog()
//            hookDataBase()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }


        hookLog()
        hookDataBase()
        hookSayHiPage()
    }

    private fun hookSayHiPage() {
        XposedHelpers.findAndHookMethod(Activity::class.java, "onCreate",
                Bundle::class.java, object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.thisObject::class.java.simpleName == "NearbySayHiListUI") {

                    val cursor = XposedHelpers.callMethod(msgDataBase, "rawQueryWithFactory",
                            msgDataBaseFactory, "SELECT * FROM LBSVerifyMessage where isSend = 0 ORDER BY createtime desc", null, null) as Cursor


                    while (cursor.moveToNext()) {
                        for (index in 0 until cursor.columnCount) {
                            val name = cursor.columnNames[index]
                            val value = cursor.getString(cursor.getColumnIndex(name))

                            LogUtils.log("LBSVerifyMessage, name = $name, value = $value")
                        }

                    }


                }

            }
        })

       // "SELECT * FROM LBSVerifyMessage where isSend = 0 ORDER BY createtime desc"
    }

    private fun hookDataBase() {
        LogUtils.log("Msghook1")

        XposedHelpers.findAndHookMethod(XposedHelpers.findClass(DB, classLoader), "rawQueryWithFactory",
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

                LogUtils.log("Msghook, onDatabaseQuerying, sql = $sql, selectionArgs  = ${selectionArgs?.joinToString { it }},  ")

            }

        })

        XposedHelpers.findAndHookMethod(XposedHelpers.findClass(DB, classLoader), "insertWithOnConflict",
                C.String, C.String, C.ContentValues, C.Int, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {

                msgDataBase = param.thisObject

                val thisObject = param.thisObject
                val table = param.args[0] as String
                val nullColumnHack = param.args[1] as String?
                val initialValues = param.args[2] as ContentValues?
                val conflictAlgorithm = param.args[3] as Int

                LogUtils.log("Msghook, onDatabaseInserted, table = $table ,nullColumnHack = $nullColumnHack ,initialValues = $initialValues, conflictAlgorithm = $conflictAlgorithm")

            }

        })

    }

    private fun hookLog() {
        val logClass = XposedHelpers.findClass(Logclass, classLoader)

        val list = logClass.methods.filter { it.genericParameterTypes.size == 3 }
                .filter { it.parameterTypes[0].name == String::class.java.name }
                .filter { it.parameterTypes[1].name == String::class.java.name }


        list.forEach {
            XposedHelpers.findAndHookMethod(logClass, it.name, it.parameterTypes[0].canonicalName,
                    it.parameterTypes[1].canonicalName, it.parameterTypes[2].canonicalName, object : XC_MethodHook() {

                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        val str1 = param.args[0] as String
                        val str2 = param.args[1] as String

                        if (param.args[2] == null) {
                            LogUtils.log("level = " + param.method.name + ", name = $str1, value = $str2")

                        } else {
                            val objArr = param.args[2] as Array<Any>

                            val format = String.format(str2, *objArr)

                            LogUtils.log("level = " + param.method.name + ", name = $str1, value = $format")
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }


    }

    //SELECT * FROM LBSVerifyMessage where isSend = 0 ORDER BY createtime desc LIMIT 8

}