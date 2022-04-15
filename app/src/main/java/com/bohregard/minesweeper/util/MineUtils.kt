package com.bohregard.minesweeper.util

import com.bohregard.minesweeper.model.MineSquare

object MineUtils {
    fun findNeighbors(mine: MineSquare, grid: List<MineSquare>) {
        with(mine) {
            isClicked = true

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
                if (item.nearbyMines == 0 && !item.isMine && !item.isClicked) {
                    findNeighbors(item, grid)
                }
            }
        }
    }
}