package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zdy.project.wechat_chatroom_helper.helper.manager.PermissionHelper
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.compose.PermissionManagerView
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.compose.SetupWizardBottomView
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.compose.SetupWizardHeaderView
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.compose.WelcomeStateView
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.compose.WriteConfigStateView
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.demoResource.HelloContent
import com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.viewmodel.NewConfigViewModel
import com.zdy.project.wechat_chatroom_helper.helper.utils.collectInScope
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import org.koin.androidx.viewmodel.ext.android.viewModel

const val BOTTOM_SIZE = 56

class NewConfigActivity : ComponentActivity() {

    private val newConfigViewModel by viewModel<NewConfigViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WechatJsonUtils.init(this)

        newConfigViewModel.stateFlow.collectInScope(lifecycleScope) { state ->
            Log.v("stateFlow.collectInScope", "state = $state")
            when (state) {
                is ConfigPageState.CheckPermissionPage -> {
                    Log.v("stateFlow.collectInScope", "result = ${state.result}")
                    if (state.result is PermissionResult.Wait) {
                        val checkResult = PermissionHelper.check(this@NewConfigActivity)
                        newConfigViewModel.handlePermissionResult(checkResult)
                    }
                }

                is ConfigPageState.WriteFilePage -> {
                    if (state.state is WriteConfigState.Wait) {
                        newConfigViewModel.handleWriteConfig()
                    }
                }

                else -> {
                }
            }
        }
        setContent {
            val state by newConfigViewModel.stateFlow.collectAsStateWithLifecycle()
            SetupWizardLayout(state)
            HelloContent()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val checkResult = PermissionHelper.check(this@NewConfigActivity)
        newConfigViewModel.handlePermissionResult(checkResult)
    }
}

@Preview
@Composable
fun SetupWizardLayout(state: ConfigPageState = ConfigPageState.CheckPermissionPage(PermissionResult.Ask)) {
    val context = LocalContext.current

    val newConfigViewModel: NewConfigViewModel = viewModel()

    Column(modifier = Modifier.fillMaxSize()) {
        SetupWizardHeaderView(state)
        SetupWizardContentView(state)
        SetupWizardBottomView({
            if (state == ConfigPageState.WelcomePage) {
                (context as Activity).finish()
            } else {
                newConfigViewModel.handlePreviousButton()
            }
        }, {
            newConfigViewModel.handleNextButton()
        })
    }
}

@Composable
private fun SetupWizardContentView(state: ConfigPageState) {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val headSize = screenHeightDp / 3f
    val contentSize = headSize * 2 - BOTTOM_SIZE.dp

    Column(
        modifier = Modifier
            .background(color = Color.White)
            .height(contentSize)
            .fillMaxWidth()
    ) {
        Shadow(alpha = 0.3f)
        when (state) {
            is ConfigPageState.CheckPermissionPage -> {
                PermissionManagerView(state)
            }

            is ConfigPageState.WelcomePage -> {
                WelcomeStateView()
            }

            is ConfigPageState.WriteFilePage -> {
                WriteConfigStateView()
            }
        }
    }
}

@Composable
fun Shadow(alpha: Float = 0.1f, height: Dp = 8.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = alpha),
                        Color.Transparent,
                    )
                )
            )
    )
}