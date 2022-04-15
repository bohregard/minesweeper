package com.bohregard.minesweeper.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HeaderUi(reset: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(20.dp)
                .clickable{ reset() },
            text = "Reset"
        )
    }
}

@Preview
@Composable
private fun PreviewHeaderUi() {
    HeaderUi {

    }
}