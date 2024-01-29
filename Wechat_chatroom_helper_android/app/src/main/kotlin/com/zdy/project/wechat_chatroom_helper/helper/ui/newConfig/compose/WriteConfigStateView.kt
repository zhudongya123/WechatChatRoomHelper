package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel.ConfigMessage
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel.NewConfigViewModel
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel.Type

@Composable
fun WriteConfigStateView(newConfigViewModel: NewConfigViewModel = viewModel()) {

    val message = newConfigViewModel.messageListFlow.collectAsStateWithLifecycle(initialValue = null).value

    val messageList: MutableList<ConfigMessage> = remember {
        mutableListOf<ConfigMessage>()
    }

    message?.let {
        when (it.type) {
            Type.Top -> {
                messageList.removeAll { it.type == Type.Top }
                messageList.add(0, it)
            }

            Type.Normal -> {
                messageList.add(0, it)
            }

            else -> {}
        }

    }

    Column(Modifier.padding(24.dp)) {
        Text(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            text = messageList.map { it.content }.joinToString("\n") { it ?: "" }
        )
    }
}