package me.sett.android

import android.content.Context
import android.content.SharedPreferences

object Preferences {
  private lateinit var prefs: SharedPreferences

  fun setup(context: Context) {
    prefs = context.getSharedPreferences("sett.ktpen", Context.MODE_PRIVATE)
  }

  private const val PREF_ADDRESS_KEY = "sett.ktpen.address"
  var address: String
    get() = prefs.getString(PREF_ADDRESS_KEY, "192.168.0.x") ?: "192.168.0.x"
    set(value) = prefs.edit().putString(PREF_ADDRESS_KEY, value).apply()

  private const val PREF_PORT_KEY = "sett.ktpen.port"
  var port: Int
    get() = prefs.getInt(PREF_PORT_KEY, 17420)
    set(value) = prefs.edit().putInt(PREF_PORT_KEY, value).apply()
}