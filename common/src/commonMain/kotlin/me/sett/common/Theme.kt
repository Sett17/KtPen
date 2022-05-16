package me.sett.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Theme(content: @Composable () -> Unit) {
  MaterialTheme(
    colors = Colors(
      Color(0xffba8ef7),
      Color(0xff594477),
      Color(0xff89ddff),
      Color(0xff436d7e),
      Color(0xff232a2f),
      Color(0xff232a2f),
      Color(0xffec5b5b),
      Color(0xffffffff),
      Color(0xffffffff),
      Color(0xffffffff),
      Color(0xffffffff),
      Color(0xffffffff),
      false
    ),
    shapes = MaterialTheme.shapes.copy(
      small = MaterialTheme.shapes.small.copy(
        CornerSize(8.dp)
      ),
      medium = MaterialTheme.shapes.medium.copy(
        CornerSize(10.dp)
      ),
      large = MaterialTheme.shapes.large.copy(
        CornerSize(14.dp)
      )
    ),
  ) {
    Surface(Modifier.fillMaxSize().background(MaterialTheme.colors.surface), content = content)
  }
}