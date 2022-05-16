import androidx.compose.ui.unit.IntOffset
import java.util.prefs.Preferences as JavaUtilPrefs

object Preferences {
  private val prefs = JavaUtilPrefs.userRoot().node("ktpen")

  private const val PREF_PORT_KEY = "port"
  var port: Int
    get() = prefs.getInt(PREF_PORT_KEY, 17420)
    set(value) {
      prefs.putInt(PREF_PORT_KEY, value)
      prefs.flush()
    }

  private const val PREF_OFFSET_X_KEY = "offset_x"
  private const val PREF_OFFSET_Y_KEY = "offset_y"

  var offset: IntOffset
    get() = IntOffset(prefs.getInt(PREF_OFFSET_X_KEY, 0), prefs.getInt(PREF_OFFSET_Y_KEY, 0))
    set(value) {
      prefs.putInt(PREF_OFFSET_X_KEY, value.x)
      prefs.putInt(PREF_OFFSET_Y_KEY, value.y)
      prefs.flush()
    }
}