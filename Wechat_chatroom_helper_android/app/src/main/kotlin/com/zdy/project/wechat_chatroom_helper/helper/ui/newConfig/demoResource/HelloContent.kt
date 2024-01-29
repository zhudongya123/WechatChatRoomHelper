package com.zdy.project.wechat_chatroom_helper.helper.ui.newConfig.demoResource

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.zdy.project.wechat_chatroom_helper.R

@Preview
@Composable
fun HelloContent() {
    Column(modifier = Modifier.padding(16.dp)) {
        var inputString by remember { mutableStateOf("") }
        if (inputString.isNotEmpty()) {
            Text(
                text = "Hello, $inputString!",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        TextField(
            value = inputString,
            onValueChange = { inputString = it },
            label = { Text("Name") }
        )
    }
}
