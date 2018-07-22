package com.zdy.project.wechat_chatroom_helper.plugins

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
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
import java.util.*

class AddContactLBSMessageHandler : IXposedHookLoadPackage {


    val Logclass = "com.tencent.mm.sdk.platformtools.x"

    val DB = "com.tencent.wcdb.database.SQLiteDatabase"
    val DBF = "com.tencent.wcdb.database.SQLiteDatabase\$CursorFactory"
    val DBSIGN = "com.tencent.wcdb.support.CancellationSignal"

    lateinit var classLoader: ClassLoader

    lateinit var msgDataBase: Any
    var msgDataBaseFactory: Any? = null

    var time = 3000L

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {


        classLoader = lpparam.classLoader

        //  if (lpparam.processName != "com.tencent.mm") return
//        if (!lpparam.processName .contains("dkmodel")) return

        //  if (System.currentTimeMillis() > 1531627200000) return

        try {
            XposedHelpers.findClass(DB, lpparam.classLoader)

            hookLog()
            hookDataBase()
         //   hookSayHiPage()
            hookFConversation()

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun hookFConversation() {

        val FMessageConversationUI = XposedHelpers.findClass("com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI", classLoader)

        XposedHelpers.findAndHookMethod(XposedHelpers.findClass(
                "com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI", classLoader),
                "initView", object : XC_MethodHook() {


            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)

                val thisObject = param.thisObject as Activity

                XposedHelpers.callMethod(thisObject, "addTextOptionMenu", 700, "清除已接受", object : MenuItem.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {

                        try {
                            XposedHelpers.callMethod(msgDataBase, "delete",
                                    "fmessage_conversation", "state=?", arrayOf("1"))
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }

                        thisObject.finish()
                        thisObject.startActivity(Intent(thisObject, FMessageConversationUI))

                        return true
                    }
                })

                XposedHelpers.callMethod(thisObject, "addTextOptionMenu", 701, "清除未接受", object : MenuItem.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {

                        AlertDialog.Builder(thisObject).setTitle("提示")
                                .setMessage("您确定需要清除未接受的好友邀请？")
                                .setNegativeButton("取消", object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                        dialog.dismiss()
                                    }
                                })
                                .setPositiveButton("确定", object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                        try {
                                            XposedHelpers.callMethod(msgDataBase, "delete",
                                                    "fmessage_conversation", "state=?", arrayOf("0"))
                                        } catch (e: Throwable) {
                                            e.printStackTrace()
                                        }


                                        Handler(thisObject.mainLooper).postDelayed(object : Runnable {
                                            override fun run() {
                                                thisObject.finish()
                                                thisObject.startActivity(Intent(thisObject, FMessageConversationUI))
                                            }

                                        }, 500)
                                    }

                                }).show()

                        return true
                    }
                })


                val cursor: Cursor = XposedHelpers.callMethod(msgDataBase, "rawQueryWithFactory",
                        msgDataBaseFactory, "SELECT * FROM fmessage_conversation ORDER BY lastModifiedTime desc", null, null) as Cursor

                while (cursor.moveToNext()) {

                    var string = ""

                    for (index in 0 until cursor.columnCount) {
                        string = string + ", " + cursor.getColumnName(index) + " = " + cursor.getString(index)
                    }
                    XposedBridge.log("fmessage_conversation" + string)
                }
            }
        })


    }

    inner class DataModel {
        var ticket: String? = null
        var nickname: String? = null
        var scene = 0
        var sayhiuser: String? = null
        var isAdd = 0
    }

    inner class MyListAdapter(val context: Activity, val m: Class<*>, val au: Class<*>,val ui:Class<*>) : BaseAdapter() {

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


                var isAddFriend = 0

                if (rcontactResult.count > 0) {
                    rcontactResult.moveToNext()
                    val encryptUsername = rcontactResult.getString(rcontactResult.getColumnIndex("encryptUsername"))
                    val type = rcontactResult.getInt(rcontactResult.getColumnIndex("type"))

                    if (encryptUsername != null && !encryptUsername.isEmpty()) {
                        if (type == 3 || type == 5 || type == 7)
                            isAddFriend = 1
                    }
                }

                data.add(DataModel().also {
                    it.ticket = ticket
                    it.scene = scene
                    it.sayhiuser = sayhiuser
                    it.isAdd = isAddFriend
                })

                rcontactResult.close()
            }

        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            val context = parent.context as Context

            val itemView = LinearLayout(context)
            val data = data[position]

            itemView.gravity = Gravity.CENTER_VERTICAL
            itemView.orientation = LinearLayout.HORIZONTAL

            val name = TextView(context)
            name.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f)
            name.maxLines = 1
            val addition = TextView(context)
            addition.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            addition.maxLines = 1


            name.text = data.sayhiuser
            addition.text = if (data.isAdd == 0) "未添加" else "已添加"
            addition.setTextColor(if (data.isAdd == 0) 0xFFFF8888.toInt() else 0xFF40C040.toInt())

            addition.setPadding(0, 0, 0, 0)

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

            val data = data.filter { it.isAdd == 0 }

            if (data.isEmpty()) return
//            val addHandler = AddHandler(data, context)
//            addHandler.sendMessageDelayed(Message.obtain(addHandler, 0), time)

            val timer = Timer()

            val timerTask = object : TimerTask() {

                var position = 0

                override fun run() {
                    if (position == data.size) {

                        timer.cancel()
                        cancel()

                        return
                    }

                    val item = data[position]
                    val addContactClass = m
                    val constructor = XposedHelpers.findConstructorExact(addContactClass, String::class.java, String::class.java, Int::class.java)
                    constructor.isAccessible = true
                    val m = constructor.newInstance(item.sayhiuser, item.ticket, item.scene)
                    val auDF = XposedHelpers.callStaticMethod(au, "DF")
                    XposedHelpers.callMethod(auDF, "a", m, 0)

                    Handler(context.mainLooper).post { Toast.makeText(context, "当前第${position + 1} 个，共${data.size}个, 当前间隔 $time 毫秒", Toast.LENGTH_SHORT).show() }

                    position++
                }
            }
            timer.schedule(timerTask, 0, time)

        }

        fun clearAllContact() {
            val data = data.filter { it.isAdd == 1 }
            if (data.isEmpty()) return

            data.forEach {
                val sayhiuser = it.sayhiuser

                try {
                    XposedHelpers.callMethod(msgDataBase, "delete",
                            "LBSVerifyMessage", "sayhiuser=?", arrayOf(sayhiuser))
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

            Handler(context.mainLooper).postDelayed(object : Runnable {
                override fun run() {
                    context.finish()
                    context.startActivity(Intent(context, ui))
                }

            }, 500)

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

        val NearbySayHiListUI = XposedHelpers.findClass("com.tencent.mm.plugin.nearby.ui.NearbySayHiListUI", classLoader)

        XposedHelpers.findAndHookMethod(NearbySayHiListUI, "initView", object : XC_MethodHook() {


            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)

                val thisObject = param.thisObject

                XposedHelpers.callMethod(thisObject, "addTextOptionMenu", 700, "鸡", object : MenuItem.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {

                        val myListAdapter = MyListAdapter(thisObject as Activity, m, au,NearbySayHiListUI)

                        val dialogView = getDialogView(thisObject as Activity)
                        val listView = dialogView.findViewById<ListView>(android.R.id.list) as ListView
                        val editText = dialogView.findViewById<EditText>(android.R.id.edit) as EditText

                        editText.hint = "默认3秒"
                        editText.setText("3000")
                        listView.adapter = myListAdapter

                        AlertDialog.Builder(thisObject as Activity).setTitle("自动添加好友").setView(dialogView)
                                .setPositiveButton("一键添加好友", object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                        this@AddContactLBSMessageHandler.time = editText.text.toString().toInt().toLong()
                                        myListAdapter.addAllContact()
                                        dialog.dismiss()
                                    }
                                }).setNeutralButton("清除已接受", object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                        myListAdapter.clearAllContact()
                                        dialog.dismiss()
                                    }
                                }).show()

                        return true
                    }
                })

            }

            fun getDialogView(context: Context): ViewGroup {

                val listView = ListView(context)
                listView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600)
                listView.id = android.R.id.list

                val editText = EditText(context)
                editText.id = android.R.id.edit
                editText.background = null
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                editText.setPadding(0, 28, 0, 8)


                val container = LinearLayout(context)
                container.orientation = LinearLayout.VERTICAL

                container.setPadding(100, 60, 100, 60)
                container.addView(listView)
                container.addView(editText)

                return container
            }


        })

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

}