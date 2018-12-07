package com.zdy.project.wechat_chatroom_helper.plugins.addition.hook

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.Handler
import android.view.MenuItem
import android.widget.EditText
import android.widget.ListView
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.plugins.addition.Clazz
import com.zdy.project.wechat_chatroom_helper.plugins.addition.DataModel
import com.zdy.project.wechat_chatroom_helper.plugins.addition.MyListAdapter
import com.zdy.project.wechat_chatroom_helper.plugins.addition.MyListAdapter.Companion.getDialogView
import com.zdy.project.wechat_chatroom_helper.plugins.addition.SpecialPluginEntry
import com.zdy.project.wechat_chatroom_helper.plugins.addition.SpecialPluginEntry.Companion.dbf
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import org.xml.sax.InputSource
import java.io.StringReader

object FConversationHook {


    lateinit var C: Clazz

    fun hook(classLoader: ClassLoader) {

        val FMessageConversationUI = XposedHelpers.findClass("com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI", classLoader)

        XposedHelpers.findAndHookMethod(XposedHelpers.findClass(
                "com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI", classLoader),
                "initView", object : XC_MethodHook() {


            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)

                val thisObject = param.thisObject as Activity

                XposedHelpers.callMethod(thisObject, "addTextOptionMenu", 700, "清除`已", object : MenuItem.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {

                        try {
                            val cursor: Cursor = XposedHelpers.callMethod(DataBaseHook.msgDataBase, "rawQueryWithFactory",
                                    DataBaseHook.msgDataBaseFactory, "SELECT * FROM fmessage_conversation where state = 1", null, null) as Cursor

                            while (cursor.moveToNext()) {
                                val talker = cursor.getString(cursor.getColumnIndex("talker"))

                                LogUtils.log("fmessage_conversation, talker = $talker")

                                try {
                                    XposedHelpers.callMethod(DataBaseHook.msgDataBase, "delete", "fmessage_conversation", "talker=?", arrayOf(talker))
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                }

                                try {
                                    XposedHelpers.callMethod(DataBaseHook.msgDataBase, "delete", "fmessage_msginfo", "talker=?", arrayOf(talker))
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }

                        thisObject.finish()
                        thisObject.startActivity(Intent(thisObject, FMessageConversationUI))

                        return true
                    }
                })

                XposedHelpers.callMethod(thisObject, "addTextOptionMenu", 701, "清除`未", object : MenuItem.OnMenuItemClickListener {
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
                                            val cursor: Cursor = XposedHelpers.callMethod(DataBaseHook.msgDataBase, "rawQueryWithFactory",
                                                    DataBaseHook.msgDataBaseFactory, "SELECT * FROM fmessage_conversation where state = 0", null, null) as Cursor

                                            while (cursor.moveToNext()) {
                                                val talker = cursor.getString(cursor.getColumnIndex("talker"))

                                                LogUtils.log("fmessage_conversation, talker = $talker")

                                                try {
                                                    XposedHelpers.callMethod(DataBaseHook.msgDataBase, "delete", "fmessage_conversation", "talker=?", arrayOf(talker))
                                                } catch (e: Throwable) {
                                                    e.printStackTrace()
                                                }

                                                try {
                                                    XposedHelpers.callMethod(DataBaseHook.msgDataBase, "delete", "fmessage_msginfo", "talker=?", arrayOf(talker))
                                                } catch (e: Throwable) {
                                                    e.printStackTrace()
                                                }
                                            }
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

//                XposedHelpers.callMethod(thisObject, "addTextOptionMenu", 702, "加扫", object : MenuItem.OnMenuItemClickListener {
//                    override fun onMenuItemClick(item: MenuItem?): Boolean {
//
//                        val cursor: Cursor = XposedHelpers.callMethod(DataBaseHook.msgDataBase, "rawQueryWithFactory",
//                                DataBaseHook.msgDataBaseFactory, "SELECT * FROM fmessage_conversation where addScene = 30 and state = 0", null, null) as Cursor
//
//                        val data = mutableListOf<DataModel>()
//
//                        while (cursor.moveToNext()) {
//                            val fmsgContent = cursor.getString(cursor.getColumnIndex("fmsgContent"))
//                            val talker = cursor.getString(cursor.getColumnIndex("talker"))
//                            val displayName = cursor.getString(cursor.getColumnIndex("displayName"))
//
//
//
//                            LogUtils.log("fmessage_conversation, fmsgContent = $fmsgContent")
//
//
//                            val db = dbf.newDocumentBuilder()
//                            val sr = StringReader(fmsgContent)
//                            val doc = db.parse(InputSource(sr))
//
//
//                            data.add(DataModel().apply {
//                                ticket = doc.getElementsByTagName("msg").item(0).attributes.getNamedItem("ticket").nodeValue
//                                sayhiuser = talker
//                                isAdd = 0
//                                scene = 30
//                                username = displayName
//                            })
//
//                        }
//
//                        val myListAdapter = MyListAdapter(thisObject as Activity, data, C)
//
//                        val dialogView = getDialogView(thisObject as Activity)
//                        val listView = dialogView.findViewById<ListView>(android.R.id.list) as ListView
//                        val editText = dialogView.findViewById<EditText>(android.R.id.edit) as EditText
//
//                        listView.adapter = myListAdapter
//
//                        AlertDialog.Builder(thisObject as Activity).setTitle("自动添加好友").setView(dialogView)
//                                .setPositiveButton("一键添加好友", object : DialogInterface.OnClickListener {
//                                    override fun onClick(dialog: DialogInterface, which: Int) {
//                                        SpecialPluginEntry.time = editText.text.toString().toInt().toLong()
//                                        myListAdapter.addAllContact()
//                                        dialog.dismiss()
//                                    }
//                                }).show()
//
//
//
//                        return true
//                    }
//                })

            }
        })


    }
}