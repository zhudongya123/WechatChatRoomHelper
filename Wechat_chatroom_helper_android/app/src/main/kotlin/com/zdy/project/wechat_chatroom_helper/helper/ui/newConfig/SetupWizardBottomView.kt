package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zdy.project.wechat_chatroom_helper.R

@Composable
fun SetupWizardBottomView(
    previousClick: () -> Unit,
    nextClick: () -> Unit
) {
    val context = LocalContext.current
    val bottomSize = BOTTOM_SIZE.dp
    val newConfigViewModel: NewConfigViewModel = viewModel()

    Box(
        modifier = Modifier
            .background(color = Color(context.getColor(R.color.color_dddddd)))
            .height(bottomSize)
            .fillMaxWidth()
    ) {
        PreviousButton(
            Modifier
                .align(Alignment.CenterStart)
                .clickable(
                    enabled = newConfigViewModel.isPreviousButtonEnable(),
                    onClick = previousClick
                )

        )
        val isNextButtonEnable = newConfigViewModel.isNextButtonEnable()

        NextButton(
            Modifier
                .align(Alignment.CenterEnd)
                .clickable(
                    enabled = isNextButtonEnable,
                    onClick = nextClick
                )
                .alpha(if (isNextButtonEnable) 1f else 0.3f)
        )

    }
}

@Composable
fun NextButton(modifier: Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxHeight()
            .padding(12.dp)
    ) {
        Text(text = context.getString(R.string.next), modifier = Modifier.align(Alignment.CenterVertically))
        Icon(painter = painterResource(R.drawable.baseline_keyboard_arrow_right_24), contentDescription = "", modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
fun PreviousButton(modifier: Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxHeight()
            .padding(12.dp)
    ) {
        Icon(painter = painterResource(R.drawable.baseline_keyboard_arrow_left_24), contentDescription = "", modifier = Modifier.align(Alignment.CenterVertically))
        Text(text = context.getString(R.string.previous), modifier = Modifier.align(Alignment.CenterVertically))
    }
}