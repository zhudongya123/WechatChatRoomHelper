package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.utils.collectInScope
import org.koin.androidx.viewmodel.ext.android.viewModel

const val BOTTOM_SIZE = 56

class NewConfigActivity : ComponentActivity() {

    private val newConfigViewModel by viewModel<NewConfigViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        newConfigViewModel.state.collectInScope(lifecycleScope) {
            if (it is ConfigState.Finish) {
                finish()
            } else {
                setContent { SetupWizardLayout(it) }
            }
        }
    }
}

@Preview
@Composable
fun SetupWizardLayout(state: ConfigState = ConfigState.Welcome) {
    val newConfigViewModel: NewConfigViewModel = viewModel()

    Column(modifier = Modifier.fillMaxSize()) {
        SetupWizardHeaderView(state)
        SetupWizardContentView(state)
        SetupWizardBottomView({
            newConfigViewModel.handlePreviousButton()
        }, {
            newConfigViewModel.handleNextButton()
        })
    }
}

@Composable
private fun SetupWizardContentView(state: ConfigState) {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val headSize = screenHeightDp / 3f
    val contentSize = headSize * 2 - BOTTOM_SIZE.dp

    Box(
        modifier = Modifier
            .background(color = Color.White)
            .height(contentSize)
            .fillMaxWidth()
    ) {
        Shadow(alpha = 0.3f)

        Text(text = state.toString(), modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun SetupWizardBottomView(previousClick: () -> Unit, nextClick: () -> Unit) {
    val bottomSize = BOTTOM_SIZE.dp
    Box(
        modifier = Modifier
            .background(color = Color.LightGray)
            .height(bottomSize)
            .fillMaxWidth()
    ) {
        PreviousButton(
            Modifier
                .align(Alignment.CenterStart)
                .clickable(onClick = previousClick)
        )
        NextButton(
            Modifier
                .align(Alignment.CenterEnd)
                .clickable(onClick = nextClick)
        )
    }
}

@Composable
private fun SetupWizardHeaderView(configState: ConfigState) {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val headSize = screenHeightDp / 3f
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .background(color = Color(0xFF0080FF))
            .height(headSize)
            .fillMaxWidth()
    ) {
        Text(
            text = context.getString(
                when (configState) {
                    ConfigState.Welcome -> R.string.config_step1_title
                    ConfigState.CheckPermission -> R.string.config_step2_title
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

@Composable
fun NextButton(modifier: Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxHeight()
            .padding(12.dp)
    ) {
        Text(text = context.getString(R.string.next), modifier = Modifier.align(CenterVertically))
        Icon(painter = painterResource(R.drawable.baseline_keyboard_arrow_right_24), contentDescription = "", modifier = Modifier.align(CenterVertically))
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
        Icon(painter = painterResource(R.drawable.baseline_keyboard_arrow_left_24), contentDescription = "", modifier = Modifier.align(CenterVertically))
        Text(text = context.getString(R.string.previous), modifier = Modifier.align(CenterVertically))
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