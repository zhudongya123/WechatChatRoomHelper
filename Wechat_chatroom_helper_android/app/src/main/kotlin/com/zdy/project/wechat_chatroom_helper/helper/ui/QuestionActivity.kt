package com.zdy.project.wechat_chatroom_helper.helper.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.R

class QuestionActivity : BaseActivity() {


    private lateinit var listContent: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setTitle(R.string.title_question_string)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_function_setting)
        listContent = findViewById(R.id.list_content)

        initSetting()
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        item?.let {
            when (item.itemId) {
                android.R.id.home -> {
                    finish()
                    return true
                }
                else -> {
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }


    private fun initSetting() {
        listContent.removeAllViews()

        val titles =
                arrayOf(getString(R.string.sub_title_ques_item_1),
                        getString(R.string.sub_title_ques_item_2))

        repeat(titles.size) {

            title = titles[it]

            val itemView = LayoutInflater.from(thisActivity).inflate(R.layout.layout_setting_item, listContent, false)
            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)
            val switch = itemView.findViewById<SwitchCompat>(android.R.id.button1)

            text1.text = title

            itemView.setOnClickListener { switch.performClick() }

            when (title) {
                getString(R.string.sub_title_ques_item_1) -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://t.cn/EP6XMon")))
                    }
                }

                getString(R.string.sub_title_ques_item_2) -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://t.cn/EvMoMNv")))
                    }
                }


            }
            listContent.addView(itemView)
        }
    }
}