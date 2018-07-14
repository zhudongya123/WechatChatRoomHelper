package com.zdy.project.wechat_chatroom_helper.plugins

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.InputType
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.gh0u1l5.wechatmagician.spellbook.C
import com.zdy.project.wechat_chatroom_helper.LogUtils
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
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

    var time = 4000L

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {


        classLoader = lpparam.classLoader

        //   if (lpparam.processName != "com.tencent.mm") return
//        if (!lpparam.processName .contains("dkmodel")) return

        try {
            XposedHelpers.findClass(DB, lpparam.classLoader)

            hookLog()
            hookDataBase()
            hookSayHiPage()

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    inner class DataModel {
        var ticket: String? = null
        var scene = 0
        var sayhiuser: String? = null
        var isAdd = 0
    }

    inner class MyListAdapter(val context: Context, val m: Class<*>, val au: Class<*>) : BaseAdapter() {

        private val cursor: Cursor = XposedHelpers.callMethod(msgDataBase, "rawQueryWithFactory",
                msgDataBaseFactory, "SELECT * FROM LBSVerifyMessage where isSend = 0 ORDER BY createtime desc", null, null) as Cursor

        private val data = mutableListOf<DataModel>()

        init {

            while (cursor.moveToNext()) {

                val type = cursor.getInt(cursor.getColumnIndex("type"))
                val scene = cursor.getInt(cursor.getColumnIndex("scene"))
                val createtime = cursor.getLong(cursor.getColumnIndex("createtime"))
                val talker = cursor.getString(cursor.getColumnIndex("talker"))
                val content = cursor.getString(cursor.getColumnIndex("content"))
                val sayhiuser = cursor.getString(cursor.getColumnIndex("sayhiuser"))
                val sayhiencryptuser = cursor.getString(cursor.getColumnIndex("sayhiencryptuser"))
                val ticket = cursor.getString(cursor.getColumnIndex("ticket"))
                val flag = cursor.getInt(cursor.getColumnIndex("flag"))

                XposedBridge.log("LBSVerifyMessage, type = $type, scene = $scene, createtime = $createtime, talker = $talker, " +
                        "content = $content, sayhiuser = $sayhiuser, sayhiencryptuser = $sayhiencryptuser, ticket = $ticket" +
                        ", flag = $flag")

                val rcontactResult = XposedHelpers.callMethod(msgDataBase, "rawQueryWithFactory",
                        msgDataBaseFactory, "SELECT * FROM rcontact where username = '$sayhiuser'", null, null) as Cursor

                data.add(DataModel().also {
                    it.ticket = ticket
                    it.scene = scene
                    it.sayhiuser = sayhiuser
                    it.isAdd = rcontactResult.count
                })
            }

        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            val context = parent.context as Context

            val itemView = LinearLayout(context)
            val data = data[position]

            itemView.gravity = Gravity.CENTER_VERTICAL
            itemView.orientation = LinearLayout.HORIZONTAL

            val name = TextView(context)
            val addition = TextView(context)

            name.text = data.sayhiuser
            addition.text = if (data.isAdd == 0) "未添加" else "已经添加"

            addition.setPadding(100, 0, 0, 0)

            itemView.addView(name)
            itemView.addView(addition)

            itemView.setOnClickListener {

                val addContactClass = m
                val constructor = XposedHelpers.findConstructorExact(addContactClass, String::class.java, String::class.java, Int::class.java)
                constructor.isAccessible = true
                val m = constructor.newInstance(data.sayhiuser, data.ticket, data.scene)
                val auDF = XposedHelpers.callStaticMethod(au, "DF")
                XposedHelpers.callMethod(auDF, "a", m, 0)
            }

            return itemView
        }

        override fun getItem(position: Int) = data[position]

        override fun getItemId(position: Int) = position.toLong()

        override fun getCount() = data.size

        fun addAllContact() {

            val filter = data.filter { it.isAdd == 0 }

            if (filter.isEmpty()) return
            val addHandler = AddHandler(filter, context)
            addHandler.sendMessageDelayed(Message.obtain(addHandler, 0), time)
        }

        inner class AddHandler(var list: List<DataModel>, var context: Context) : Handler(context.mainLooper) {

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                val index = msg.what

                if (index < list.size) {

                    val item = list.get(index)

                    val addContactClass = m
                    val constructor = XposedHelpers.findConstructorExact(addContactClass, String::class.java, String::class.java, Int::class.java)
                    constructor.isAccessible = true
                    val m = constructor.newInstance(item.sayhiuser, item.ticket, item.scene)
                    val auDF = XposedHelpers.callStaticMethod(au, "DF")
                    XposedHelpers.callMethod(auDF, "a", m, 0)

                    Toast.makeText(context, "当前第$index 个，共${list.size}个, 当前间隔 $time 毫秒", Toast.LENGTH_SHORT).show()

                    if (index < list.size - 1)
                        sendMessageDelayed(Message.obtain(this, index + 1), time)

                }
            }
        }

    }


    private fun hookSayHiPage() {

        val m = XposedHelpers.findClass("com.tencent.mm.pluginsdk.model.m", classLoader)
        val au = XposedHelpers.findClass("com.tencent.mm.model.au", classLoader)


        XposedHelpers.findAndHookMethod(Activity::class.java, "onCreate",
                Bundle::class.java, object : XC_MethodHook() {

            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.thisObject::class.java.simpleName == "NearbySayHiListUI") {


                }
            }
        })

        XposedHelpers.findAndHookMethod(XposedHelpers.findClass(
                "com.tencent.mm.plugin.nearby.ui.NearbySayHiListUI", classLoader),
                "initView", object : XC_MethodHook() {


            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)

                val thisObject = param.thisObject

                XposedHelpers.callMethod(thisObject, "addTextOptionMenu", 1, "鸡掰", object : MenuItem.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {

                        val myListAdapter = MyListAdapter(thisObject as Activity, m, au)

                        val dialogView = getDialogView(thisObject as Activity)
                        val listView = dialogView.findViewById<ListView>(android.R.id.list) as ListView
                        val editText = dialogView.findViewById<EditText>(android.R.id.edit) as EditText

                        editText.hint = "默認四秒只能輸入數字"
                        editText.setText("4000")
                        listView.adapter = myListAdapter



                        AlertDialog.Builder(thisObject as Activity).setTitle("自动添加好友").setView(dialogView)
                                .setPositiveButton("一键添加所有未添加好友", object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, which: Int) {

                                        this@AddContactLBSMessageHandler.time = editText.text.toString().toInt().toLong()
                                        myListAdapter.addAllContact()
                                        dialog.dismiss()
                                    }
                                }).show()

                        return true
                    }
                })

            }

            fun getDialogView(context: Context): ViewGroup {

                val listView = ListView(context)
                listView.id = android.R.id.list


                val editText = EditText(context)
                editText.id = android.R.id.edit
                editText.inputType = InputType.TYPE_CLASS_NUMBER


                val container = LinearLayout(context)
                container.orientation = LinearLayout.VERTICAL

                container.setPadding(100, 60, 100, 60)
                container.addView(listView)
                container.addView(editText)

                return container
            }


        })


        XposedBridge.log("findClass.methods = " + m.methods.joinToString { it.toString() + ", \n" })
        XposedBridge.log("findClass.methods = " + m.declaredMethods.joinToString { it.toString() + ", \n" })


        XposedHelpers.findAndHookConstructor(m, String::class.java, String::class.java,
                Int::class.java, object : XC_MethodHook() {

            override fun beforeHookedMethod(param: MethodHookParam) {
                super.beforeHookedMethod(param)

                val arg0 = param.args[0] as String
                val arg1 = param.args[1] as String
                val arg2 = param.args[2] as Int

                XposedBridge.log("LBSVerifyMessage, arg0 = $arg0, arg1 = $arg1, arg2 = $arg2")
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)
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
                            //        LogUtils.log("level = " + param.method.name + ", name = $str1, value = $str2")

                        } else {
                            val objArr = param.args[2] as Array<Any>

                            val format = String.format(str2, *objArr)

                            //    LogUtils.log("level = " + param.method.name + ", name = $str1, value = $format")
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