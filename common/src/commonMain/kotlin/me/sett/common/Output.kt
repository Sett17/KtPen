package me.sett.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class Output(private val list: SnapshotStateList<String>, private val state: LazyListState, private val scope: CoroutineScope) {

  operator fun plus(s: Any) {
    MainScope().launch {
      list.add(s.toString())
      scope.launch {
        state.scrollToItem(list.lastIndex)
      }
    }
  }

  @Composable
  fun Output(fontSize: TextUnit = 14.sp, modifier: Modifier = Modifier) {
    Box {
      LazyColumn(
        modifier,
        state = state,
      ) {
        items(list) {
          Text(it, fontSize = fontSize)
        }
      }
    }
  }
}