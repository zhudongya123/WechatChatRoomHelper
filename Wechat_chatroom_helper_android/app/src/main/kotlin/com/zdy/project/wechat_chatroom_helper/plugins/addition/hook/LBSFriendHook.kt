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
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object LBSFriendHook {


    lateinit var C: Clazz

    fun hook(classLoader: ClassLoader) {
        XposedHelpers.findAndHookMethod(C.NearbySayHiListUI, "initView", object : XC_MethodHook() {


            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)

                val thisObject = param.thisObject

                XposedHelpers.callMethod(thisObject, "addTextOptionMenu", 700, "鸡", object : MenuItem.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {

                        val myListAdapter = MyListAdapter(thisObject as Activity, getLBSListData(), C)

                        val dialogView = MyListAdapter.getDialogView(thisObject as Activity)
                        val listView = dialogView.findViewById<ListView>(android.R.id.list) as ListView
                        val editText = dialogView.findViewById<EditText>(android.R.id.edit) as EditText

                        editText.hint = "默认3秒"
                        editText.setText("3000")
                        listView.adapter = myListAdapter

                        AlertDialog.Builder(thisObject as Activity).setTitle("自动添加好友").setView(dialogView)
                                .setPositiveButton("一键添加好友", object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                        SpecialPluginEntry.time = editText.text.toString().toInt().toLong()
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


        })

        XposedHelpers.findAndHookConstructor(C.m, String::class.java, String::class.java,
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
    }


    private fun getLBSListData(): MutableList<DataModel> {

        val data = mutableListOf<DataModel>()

        val cursor: Cursor = XposedHelpers.callMethod(DataBaseHook.msgDataBase, "rawQueryWithFactory",
                DataBaseHook.msgDataBaseFactory, "SELECT * FROM LBSVerifyMessage where isSend = 0 ORDER BY createtime desc", null, null) as Cursor

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

            val rcontactResult = XposedHelpers.callMethod(DataBaseHook.msgDataBase, "rawQueryWithFactory",
                    DataBaseHook.msgDataBaseFactory, "SELECT * FROM rcontact where username = '$sayhiuser'", null, null) as Cursor


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
        return data
    }
}