package com.bohregard.minesweeper.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

data class MineSquare(
    val id: Int,
    val isMine: Boolean,
    var nearbyMines: Int = 0
) {

    var isClicked = mutableStateOf(false)

    fun textColor(): Color {
        if (!isClicked.value) {
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

    fun findNeighbors(grid: List<MineSquare>) {
        isClicked.value = true

        val ids = mutableListOf<Int>()

        val middleStart = id - 2
        val topMiddle = id - 11
        val bottomMiddle = id + 9
        val middleEnd = id

        when {
            id % 10 == 1 -> {
                ids.add(topMiddle)
                ids.add(bottomMiddle)
                ids.add(middleEnd)
            }
            id % 10 == 0 -> {
                ids.add(middleStart)
                ids.add(topMiddle)
                ids.add(bottomMiddle)
            }
            else -> {
                ids.add(middleStart)
                ids.add(topMiddle)
                ids.add(bottomMiddle)
                ids.add(middleEnd)
            }
        }

        ids.filter { it in 0..99 }.forEach {
            val item = grid[it]
            if (item.nearbyMines == 0 && !item.isMine && !item.isClicked.value) {
                item.findNeighbors(grid)
            }
        }
    }
}