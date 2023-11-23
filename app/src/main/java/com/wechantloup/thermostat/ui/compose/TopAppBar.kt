package com.wechantloup.thermostat.ui.compose

import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopAppBar(
    text: String,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.TopAppBar(
        title = { TopAppBarTitle(text) },
        modifier = modifier.background(MaterialTheme.colorScheme.background),
    )
}

@Composable
private fun TopAppBarTitle(title: String) {
    Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
