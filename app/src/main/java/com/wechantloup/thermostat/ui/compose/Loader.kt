package com.wechantloup.thermostat.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.wechantloup.thermostat.ui.theme.ThermostatTheme

@Composable
fun Loader(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
) {
    AnimatedVisibility(
        enter = fadeIn(initialAlpha = 0f),
        exit = fadeOut(targetAlpha = 1.0f),
        visible = isVisible,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clickable {},
        ) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
private fun LoaderPreview() {
    ThermostatTheme {
        Loader(isVisible = true)
    }
}
