package me.sett.android

import android.os.Bundle
import android.view.MotionEvent
import android.view.MotionEvent.BUTTON_STYLUS_PRIMARY
import android.view.MotionEvent.TOOL_TYPE_STYLUS
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.sett.common.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

lateinit var output: Output

class MainActivity : AppCompatActivity() {
  @OptIn(ExperimentalComposeUiApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Preferences.setup(this)
    setContent {
      Theme {
        val systemUiController = rememberSystemUiController()
        systemUiController.setSystemBarsColor(MaterialTheme.colors.surface, false)

        val listState = rememberLazyListState()
        val list = remember { mutableStateListOf<String>() }
        val scope = rememberCoroutineScope()
        output = Output(list, listState, scope)

        val pos = remember { mutableStateOf(Pair(0f, 0f)) }
        val inRange = remember { mutableStateOf(false) }

        val settingsScreen = remember { mutableStateOf(false) }

        val canvasSize = remember { mutableStateOf(IntSize.Zero) }
        Column(Modifier.fillMaxSize(1f).padding(15.dp), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
          Row(Modifier.fillMaxWidth().padding(26.dp, 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.width(320.dp)) {
              Icon(
                if (settingsScreen.value) Icons.Filled.ArrowBack else Icons.Filled.Settings,
                "Settings",
                Modifier.size(30.dp).clickable {
                  settingsScreen.value = !settingsScreen.value
                },
              )
            }
            Box(Modifier.width(320.dp), contentAlignment = Alignment.Center) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("KtPen", fontSize = 24.sp)
                Text("UDP to ${Preferences.address}:${Preferences.port}", fontSize = 10.sp)
              }
            }
            output.Output(9.sp, Modifier.height(75.dp).width(320.dp))
          }
          if (settingsScreen.value) {
            val address = remember { mutableStateOf(Preferences.address) }
            val port = remember { mutableStateOf("${Preferences.port}") }
            Box(Modifier.fillMaxHeight(1f).fillMaxWidth(.9f), contentAlignment = Alignment.Center) {
              Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceEvenly) {
                TextField(
                  address.value,
                  onValueChange = {
                    address.value = it
                    Preferences.address = it
                  },
                  label = { Text("Address") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                TextField(
                  port.value,
                  onValueChange = {
                    port.value = it
                    Preferences.port = it.toIntOrNull() ?: 0
                  },
                  label = { Text("Port") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Spacer(Modifier.height(50.dp))
              }
            }
          } else {
            Box(
              modifier = Modifier.fillMaxHeight(1f)
                .aspectRatio(16 / 9f)
                .border(2.dp, MaterialTheme.colors.secondary, MaterialTheme.shapes.large)
                .onGloballyPositioned {
                  canvasSize.value = it.size
                }
                .pointerInteropFilter(onTouchEvent = {
                  when (it.action) {
                    MotionEvent.ACTION_HOVER_MOVE -> {
                      pos.value = Pair(it.x, it.y)
                      PenPacket(
                        PenEvent.HOVER_MOVE,
                        it.x / canvasSize.value.width,
                        it.y / canvasSize.value.height,
                        buttonPressed = it.buttonState == BUTTON_STYLUS_PRIMARY
                      ).send(Preferences.address, Preferences.port)
                    }
                    MotionEvent.ACTION_MOVE, 213       -> {
                      if (it.getToolType(0) == TOOL_TYPE_STYLUS) {
                        pos.value = Pair(it.x, it.y)
                        PenPacket(
                          PenEvent.CONTACT_MOVE,
                          it.x / canvasSize.value.width,
                          it.y / canvasSize.value.height,
                          it.pressure,
                          it.buttonState == BUTTON_STYLUS_PRIMARY
                        ).send(Preferences.address, Preferences.port)
                      }
                    }
                    MotionEvent.ACTION_HOVER_EXIT -> {
                      inRange.value = false
                      PenPacket(PenEvent.HOVER_EXIT).send(Preferences.address, Preferences.port)
                    }
                    MotionEvent.ACTION_DOWN       -> {
                      if (it.getToolType(0) == TOOL_TYPE_STYLUS) {
                        PenPacket(PenEvent.CONTACT_DOWN).send(Preferences.address, Preferences.port)
                      }
                    }
                    MotionEvent.ACTION_UP         -> {
                      if (it.getToolType(0) == TOOL_TYPE_STYLUS) {
                        PenPacket(PenEvent.CONTACT_UP).send(Preferences.address, Preferences.port)
                      }
                    }
                  }
                  true
                })
            ) {}
          }
        }
      }
    }
  }
}

fun PenPacket.send(address: String, port: Int) {
  CoroutineScope(Dispatchers.IO).launch {
    withContext(Dispatchers.IO) {
      DatagramSocket().use { socket ->
        socket.broadcast = true
        val data = this@send.encode()
        socket.send(DatagramPacket(data, data.size, InetAddress.getByName(address), port))
        output + "${this@send}"
      }
    }
  }
}
