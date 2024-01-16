package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.manager.PermissionHelper

@Composable
fun PermissionManagerView(state: ConfigState.CheckPermission) {
    val context = LocalContext.current

    val result = state.result

    val pair: Pair<String, Int> =
        when (result) {
            PermissionResult.Ask -> {
                val descriptionText = context.getString(R.string.config_step2_text1)
                val descriptionTextColor = context.getColor(R.color.warm_color)
                Pair<String, Int>(descriptionText, descriptionTextColor)
            }

            PermissionResult.Deny -> {
                val descriptionText = context.getString(R.string.config_permission_fail)
                val descriptionTextColor = context.getColor(R.color.error_color)
                Pair<String, Int>(descriptionText, descriptionTextColor)
            }

            PermissionResult.Pass -> {
                val descriptionText = context.getString(R.string.config_permission_success)
                val descriptionTextColor = context.getColor(R.color.right_color)
                Pair<String, Int>(descriptionText, descriptionTextColor)
            }

            PermissionResult.Wait -> {
                val descriptionText = context.getString(R.string.config_permission_wait)
                val descriptionTextColor = context.getColor(R.color.warm_color)
                Pair<String, Int>(descriptionText, descriptionTextColor)
            }

        }



    Column(modifier = Modifier.padding(24.dp)) {
        Text(text = pair.first, color = Color(pair.second))
        Spacer(modifier = Modifier.height(10.dp))
        if (result is PermissionResult.Pass || result is PermissionResult.Wait) return@Column
        Button(
            onClick = {
                when (result) {
                    is PermissionResult.Deny -> {
                        PermissionHelper.gotoPermissionPage(context as Activity)
                    }

                    is PermissionResult.Ask -> {
                        PermissionHelper.requestPermission(context as Activity)
                    }

                    else -> {
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(context.getColor(R.color.colorPrimary))),
            elevation = ButtonDefaults.buttonElevation(8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "检查")
        }

    }
}