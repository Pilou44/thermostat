package com.wechantloup.thermostat.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wechantloup.thermostat.model.DayTime
import com.wechantloup.thermostat.ui.theme.Dimens
import com.wechantloup.thermostat.ui.theme.ThermostatTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun DayTimeLine(
    dayTime: DayTime,
    modifier: Modifier = Modifier,
) {
    var columnWidth by remember { mutableIntStateOf(0) }
    var increment by remember { mutableIntStateOf(1) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = Dimens.spacing1w),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.spacing1w)
                .fillMaxWidth(),
        ) {
            dayTime.hours.forEach { mode ->
                Hour(
                    mode = mode,
                    modifier = Modifier
                        .weight(1f)
                        .onSizeChanged { columnWidth = it.width },
                )
            }
        }

        val columnWidthDp = with(LocalDensity.current) { columnWidth.toDp() }
        increment = when {
            columnWidthDp.value < 5 -> 6
            columnWidthDp.value < 7 -> 4
            columnWidthDp.value < 10 -> 3
            columnWidthDp.value < 20 -> 2
            else -> 1
        }
        val textWidthDp = with(LocalDensity.current) { (increment * columnWidth).toDp() }
        Row(
            modifier = Modifier.wrapContentSize(unbounded = true)
        ) {
            for (index in 0..24) {
                if (index % increment == 0) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                        modifier = Modifier.width(textWidthDp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Hour(
    mode: DayTime.Mode,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.secondaryContainer)
//            .fillMaxHeight(if (mode == DayTime.Mode.DAY) 1f else 0.5f)
            .background(MaterialTheme.colorScheme.secondaryContainer),
    ) {
//        Box(
//            modifier = modifier
//                .fillMaxSize())
        Box(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(if (mode == DayTime.Mode.DAY) 1f else 0.25f)
//                .fillMaxHeight(0.5f)
                .background(MaterialTheme.colorScheme.onSecondaryContainer)
        ) {}
    }
}

@Preview
@Composable
fun DayTimeLinePreview() {
    val dayTime = DayTime(
        persistentListOf(
            DayTime.Mode.NIGHT,
            DayTime.Mode.NIGHT,
            DayTime.Mode.NIGHT,
            DayTime.Mode.NIGHT,
            DayTime.Mode.NIGHT,
            DayTime.Mode.NIGHT,
            DayTime.Mode.DAY,
            DayTime.Mode.DAY,
            DayTime.Mode.DAY,
            DayTime.Mode.DAY,
            DayTime.Mode.DAY,
            DayTime.Mode.DAY,
            DayTime.Mode.NIGHT,
            DayTime.Mode.NIGHT,
            DayTime.Mode.NIGHT,
            DayTime.Mode.NIGHT,
            DayTime.Mode.NIGHT,
            DayTime.Mode.NIGHT,
            DayTime.Mode.DAY,
            DayTime.Mode.DAY,
            DayTime.Mode.DAY,
            DayTime.Mode.DAY,
            DayTime.Mode.DAY,
            DayTime.Mode.DAY,
        )
    )
    ThermostatTheme {
        Column {
            DayTimeLine(
                dayTime = dayTime,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.background)
            )
            DayTimeLine(
                dayTime = dayTime,
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}
