package com.zdy.project.wechat_chatroom_helper.plugins.addition.hook

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.database.Cursor
import android.view.MenuItem
import android.widget.EditText
import android.widget.ListView
import com.zdy.project.wechat_chatroom_helper.plugins.addition.Clazz
import com.zdy.project.wechat_chatroom_helper.plugins.addition.DataModel
import com.zdy.project.wechat_chatroom_helper.plugins.addition.MyListAdapter
import com.zdy.project.wechat_chatroom_helper.plugins.addition.SpecialPluginEntry
import com.zdy.project.wechat_chatroom_helper.plugins.addition.SpecialPluginEntry.Companion.dbf
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import org.xml.sax.InputSource
import java.io.StringReader
import de.robv.android.xposed.XposedBridge
import java.util.*


object ShakeHook {

    lateinit var C: Clazz

    var sensorFlag = false

    fun hook(classLoader: ClassLoader) {


        XposedHelpers.findAndHookMethod(C.ShakeSayHiListUI, "initView", object : XC_MethodHook() {


            override fun afterHookedMethod(param: MethodHookParam) {

                val thisObject = param.thisObject as Activity

                XposedHelpers.callMethod(thisObject, "addTextOptionMenu", 702, "加摇", object : MenuItem.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {

                        val cursor: Cursor = XposedHelpers.callMethod(DataBaseHook.msgDataBase, "rawQueryWithFactory",
                                DataBaseHook.msgDataBaseFactory, "SELECT * FROM shakeverifymessage where isSend = 0 ORDER BY createtime desc", null, null) as Cursor

                        val data = mutableListOf<DataModel>()

                        while (cursor.moveToNext()) {
                            val content = cursor.getString(cursor.getColumnIndex("content"))
                            val sayhi = cursor.getString(cursor.getColumnIndex("sayhiuser"))

                            val db = dbf.newDocumentBuilder()
                            val sr = StringReader(content)
                            val doc = db.parse(InputSource(sr))

                            val fromnickname = doc.getElementsByTagName("msg").item(0).attributes.getNamedItem("fromnickname").nodeValue

                            if (data.any { it.sayhiuser == sayhi }) continue

                            data.add(DataModel().apply {
                                ticket = doc.getElementsByTagName("msg").item(0).attributes.getNamedItem("ticket").nodeValue
                                sayhiuser = sayhi
                                isAdd = if (isUserYourFriend(sayhi)) 1 else 0
                                username = fromnickname
                            })

                        }


                        val myListAdapter = MyListAdapter(thisObject as Activity, data, C)

                        val dialogView = MyListAdapter.getDialogView(thisObject as Activity)
                        val listView = dialogView.findViewById<ListView>(android.R.id.list) as ListView
                        val editText = dialogView.findViewById<EditText>(android.R.id.edit) as EditText

                        editText.hint = "默认${SpecialPluginEntry.time}秒"
                        editText.setText(SpecialPluginEntry.time.toString())
                        listView.adapter = myListAdapter

                        AlertDialog.Builder(thisObject as Activity).setTitle("自动添加好友").setView(dialogView)
                                .setPositiveButton("一键添加好友", object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                        SpecialPluginEntry.time = editText.text.toString().toInt().toLong()
                                        myListAdapter.addAllContact()
                                        dialog.dismiss()
                                    }
                                }).show()



                        return true
                    }
                })

            }
        })

        XposedHelpers.findAndHookMethod(C.ShakeReportUI, "initView", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {

                val thisObject = param.thisObject as Activity

                XposedHelpers.callMethod(thisObject, "addTextOptionMenu", 702, "给我摇", object : MenuItem.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        //sensorFlag = true
                        return true
                    }
                })
            }

        })

        XposedBridge.hookAllMethods(XposedHelpers.findClass("android.hardware.SystemSensorManager\$SensorEventQueue", classLoader),
                "dispatchSensorEvent", object : XC_MethodHook() {


            override fun beforeHookedMethod(param: MethodHookParam) {

                if (sensorFlag) {

                    (param.args[1] as FloatArray)[0] = (Random().nextFloat() - 0.5f) * 10f + 12f
                    (param.args[1] as FloatArray)[1] = (Random().nextFloat() - 0.5f) * 10f + 12f

                    sensorFlag = false

                }
            }
        })
    }

    fun isUserYourFriend(username: String): Boolean {

        val cursor: Cursor = XposedHelpers.callMethod(DataBaseHook.msgDataBase, "rawQueryWithFactory",
                DataBaseHook.msgDataBaseFactory, "SELECT * FROM rcontact where username = '$username'", null, null) as Cursor

        if (cursor.count == 0) {
            return false
        }

        if (cursor.count == 1) {

            while (cursor.moveToNext()) {
                val type = cursor.getInt(cursor.getColumnIndex("type"))

                if (type != 0) {
                    return true
                }
            }
        }
        return false
    }
}