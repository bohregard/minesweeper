package com.bohregard.minesweeper.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bohregard.minesweeper.model.MineSquare

@Composable
fun MinesweeperGridUi(grid: List<MineSquare>) {
    val hapticFeedback = LocalHapticFeedback.current
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        grid.chunked(10).forEach {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (mineSquare in it) {
                    val mine by remember { mutableStateOf(mineSquare) }
                    Log.d("TAG", "Rendering")
                    var bg = if (mine.isClicked.value) Color.White else Color.Gray
                    val border = if (mine.isClicked.value) Color.LightGray else Color.DarkGray
                    val textColor = mine.textColor()
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                            .aspectRatio(1f)
                            .border(width = 1.dp, color = border)
                            .background(bg)
                            .clickable(onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                mine.isClicked.value = true
                                bg = Color.White
                                // We need to look for what we just clicked and if it's zero, find all adjacent zeros
                                if (mine.nearbyMines == 0 && !mine.isMine) {
                                    mine.findNeighbors(grid)
                                }
                            })
                    ) {
                        Text(
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            text = if (mine.isMine) "x" else mine.nearbyMines.toString()
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewMinesweeperGridUi() {

}