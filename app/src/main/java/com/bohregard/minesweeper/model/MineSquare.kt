package com.bohregard.minesweeper.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class MineSquare(
    val id: Int,
    val isMine: Boolean,
    var nearbyMines: Int = 0
) {

    var isFlagged by mutableStateOf(false)
    var isClicked by mutableStateOf(false)

    fun textColor(): Color {
        if (!isClicked) {
            return Color.Gray
        }
        if (isMine) {
            return Color.Black
        }
        return when (nearbyMines) {
            0 -> Color.White
            1 -> Color(0xFF2A359C)
            2 -> Color(0xFF20572F)
            3 -> Color(0xFF960C0C)
            4 -> Color(0xFF581873)
            5 -> Color(0xFF571D1D)
            6 -> Color(0xFF1A5887)
            7 -> Color.Black
            8 -> Color.DarkGray
            else -> Color.White
        }
    }
}