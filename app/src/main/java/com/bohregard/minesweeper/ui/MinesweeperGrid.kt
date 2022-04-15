package com.bohregard.minesweeper.ui

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bohregard.minesweeper.model.MineSquare
import com.bohregard.minesweeper.util.MineUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinesweeperGridUi(
    grid: List<MineSquare>,
    mineClicked: () -> Unit
) {
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
                for (mine in it) {
                    var bg = if (mine.isClicked) Color.White else Color.LightGray
                    val border = if (mine.isClicked) Color.LightGray else Color.DarkGray
                    val textColor = mine.textColor()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(width = if (mine.isClicked) 0.5.dp else 0.dp, color = border)
                            .background(bg)
                            .combinedClickable(
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    mine.isClicked = true
                                    bg = Color.White
                                    // We need to look for what we just clicked and if it's zero, find all adjacent zeros
                                    if (mine.nearbyMines == 0 && !mine.isMine) {
                                        MineUtils.findNeighbors(mine, grid)
                                    }

                                    if (mine.isMine) {
                                        mineClicked()
                                    }
                                },
                                onLongClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )
                    ) {
                        if (mine.isClicked) {
                            Text(
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                text = if (mine.isMine) "x" else mine.nearbyMines.toString()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .align(Alignment.TopStart)
                                    .border(1.dp, Color.White)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                                    .align(Alignment.CenterStart)
                                    .border(1.dp, Color.White)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .align(Alignment.BottomStart)
                                    .border(1.dp, Color.Gray)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                                    .align(Alignment.CenterEnd)
                                    .border(1.dp, Color.Gray)
                            )
                        }
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