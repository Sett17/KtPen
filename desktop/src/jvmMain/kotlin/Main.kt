import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import me.sett.common.Output
import me.sett.common.PenEvent.*
import me.sett.common.PenPacket
import me.sett.common.Theme
import me.sett.common.decode
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.NetworkInterface
import kotlin.math.roundToInt

lateinit var output: Output

lateinit var receiveJob: Job
lateinit var socket: DatagramSocket

fun main() = application {
  val offsetX = remember { mutableStateOf(Preferences.offset.x) }
  val offsetY = remember { mutableStateOf(Preferences.offset.y) }
  val port = remember { mutableStateOf(Preferences.port) }

  SynthPointer
  Window(onCloseRequest = ::exitApplication) {
    Theme {
      val listState = rememberLazyListState()
      val list = remember { mutableStateListOf<String>() }
      val scope = rememberCoroutineScope()
      output = Output(list, listState, scope)

      val startEnabled = remember { mutableStateOf(true) }

      Column {
        Box(Modifier.fillMaxHeight(.5f).padding(10.dp)) {
          Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.fillMaxWidth(.5f), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
              TextField(
                value = "${offsetX.value}",
                onValueChange = {
                  offsetX.value = it.toIntOrNull() ?: 0
                  Preferences.offset = IntOffset(offsetX.value, offsetY.value)
                },
                label = {
                  Text("Offset X")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = startEnabled.value
              )
              Spacer(Modifier.height(10.dp))
              TextField(
                value = "${offsetY.value}",
                onValueChange = {
                  offsetY.value = it.toIntOrNull() ?: 0
                  Preferences.offset = IntOffset(offsetX.value, offsetY.value)
                },
                label = {
                  Text("Offset Y")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = startEnabled.value
              )
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
              TextField(
                value = "${port.value}",
                onValueChange = {
                  port.value = it.toIntOrNull() ?: 0
                  Preferences.port = port.value
                },
                label = {
                  Text("Port")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = startEnabled.value
              )
              Spacer(Modifier.height(10.dp))
              var prevPressure = -1f
              if (startEnabled.value) {
                Button(
                  onClick = {
                    output + "Server started on ${
                      NetworkInterface.networkInterfaces().map { it.inetAddresses().map { it.hostAddress }.filter { it.startsWith("192") }.toArray().firstOrNull() }.filter { it != null }.toArray()
                        .joinToString()
                    }:${port.value}"
                    startEnabled.value = false
                    PenPacket.receive(port.value) {
                      output + "$it"
                      when (it.event) {
                        HOVER_MOVE -> {
                          SynthPointer.penHoverMove(it.arg1, it.arg2, offsetX.value.toInt(), offsetY.value.toInt(), it.buttonPressed)
                        }
                        HOVER_EXIT -> {
                          SynthPointer.penHoverExit()
                        }
                        CONTACT_MOVE -> {
                          SynthPointer.penContactMove(it.arg1, it.arg2, offsetX.value.toInt(), offsetY.value.toInt(), ((it.arg3 * 1024).roundToInt()), it.buttonPressed)
                          prevPressure = it.arg3
                        }
                        CONTACT_DOWN -> {
                          SynthPointer.penDown()
                        }
                        CONTACT_UP -> {
                          SynthPointer.penUp()
                        }
                        DUMMY -> {}
                      }
                    }
                  }
                ) {
                  Text("Start")
                }
              } else {
                Button(
                  onClick = {
                    startEnabled.value = true
                    runBlocking {
                      if (::socket.isInitialized) {
                        socket.close()
                      }
                      receiveJob.cancelAndJoin()
                      output + "Server stopped"
                    }
                  }
                ) {
                  Text("Stop")
                }
              }
            }
          }
        }
        Box(Modifier.fillMaxHeight(1f).padding(10.dp)) {
          output.Output(fontSize = 12.sp, modifier = Modifier.border(1.dp, MaterialTheme.colors.secondary, MaterialTheme.shapes.large).padding(3.dp).fillMaxHeight().fillMaxWidth(.5f))
        }
      }
    }
  }
}

fun PenPacket.Companion.receive(port: Int, process: (PenPacket) -> Unit) {
  receiveJob = CoroutineScope(Dispatchers.IO).launch {
    withContext(Dispatchers.IO) {
      val buffer = ByteArray(PenPacket.size)
      try {
        socket = DatagramSocket(port)
        socket.broadcast = true
        while (true) {
          if (receiveJob.isCancelled) {
            break
          }
          val packet = DatagramPacket(buffer, buffer.size)
          socket.receive(packet)
          process.invoke(PenPacket.decode(packet.data))
        }
      } catch (e: Exception) {
        if (e.message != "Socket closed") {
          e.printStackTrace()
        }
      } finally {
        socket.close()
      }
    }
  }
}
