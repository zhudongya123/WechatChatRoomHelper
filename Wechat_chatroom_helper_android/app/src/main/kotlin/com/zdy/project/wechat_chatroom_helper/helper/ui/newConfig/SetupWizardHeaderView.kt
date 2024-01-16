package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdy.project.wechat_chatroom_helper.R

@Composable
fun SetupWizardHeaderView(configState: ConfigState) {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val headSize = screenHeightDp / 3f
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .background(color = Color(context.getColor(R.color.colorPrimary)))
            .height(headSize)
            .fillMaxWidth()
    ) {
        Text(
            text = context.getString(
                when (configState) {
                    ConfigState.Welcome -> R.string.config_step1_title
                    is ConfigState.CheckPermission -> R.string.config_step2_title
                    ConfigState.WriteFile -> R.string.config_step3_title
                    else -> throw RuntimeException("Error state -> $configState")
                }
            ),
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.BottomCenter)
        )
    }
}