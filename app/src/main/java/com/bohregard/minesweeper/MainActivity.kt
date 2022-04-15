package com.bohregard.minesweeper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bohregard.minesweeper.model.MineSquare
import com.bohregard.minesweeper.ui.HeaderUi
import com.bohregard.minesweeper.ui.MinesweeperGridUi
import com.bohregard.minesweeper.ui.theme.MinesweeperTheme
import kotlin.random.Random

operator fun Boolean.plus(bool: Boolean): Int = when {
    this && bool -> 2
    !this && !bool -> 0
    else -> 1
}

operator fun Int.plus(bool: Boolean): Int = if (bool) this + 1 else this

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var grid by rememberSaveable { mutableStateOf(buildGrid()) }

            MinesweeperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        HeaderUi {
                            grid = buildGrid()
                        }
                        MinesweeperGridUi(grid = grid) {
                            // Lost the game :( Needs a reset
                            grid.filter { it.isMine }.forEach { it.isClicked = true }
                        }
                    }
                }
            }
        }
    }

    private fun buildGrid(): List<MineSquare> {
        val mines by lazy {
            val ran = Random
            IntArray(15) { ran.nextInt(1, 101) }
        }

        val grid = (1..100).map { MineSquare(it, mines.contains(it)) }
        grid.forEach {
            if (it.isMine) {
                return@forEach
            }

            // Rows and Columns
            // If we think about the [] as a grid, we can figure out what squares are "around" it
            // and calculate the mines in the area.
            // eg for 1, we'd look at 2, 11, and 12. Count the mines and set the text.
            if (it.id == 1) {
                // look at 2, 11, and 12
                it.nearbyMines = grid[1].isMine + grid[10].isMine + grid[11].isMine
                return@forEach
            }

            val curId = it.id

            var topStart = curId - 12
            var middleStart = curId - 2
            var bottomStart = curId + 8
            if (it.id % 10 == 1) {
                topStart = curId - 11
                middleStart = curId - 1
                bottomStart = curId + 9
            }

            var topEnd = curId - 9
            var middleEnd = curId + 1
            var bottomEnd = curId + 11
            if (it.id % 10 == 0) {
                topEnd = curId - 10
                middleEnd = curId
                bottomEnd = curId + 10
            }

            if (it.id > 10) {
                grid.subList(topStart, topEnd).forEach { top ->
                    if (top.isMine) {
                        it.nearbyMines++
                    }
                }
            }

            grid.subList(middleStart, middleEnd).forEach { middle ->
                if (middle.id != curId && middle.isMine) {
                    it.nearbyMines++
                }
            }

            // Bottom Row
            if (it.id < 90) {
                grid.subList(bottomStart, bottomEnd).forEach { bottom ->
                    if (bottom.isMine) {
                        it.nearbyMines++
                    }
                }
            }
        }
        return grid
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MinesweeperTheme {
    }
}