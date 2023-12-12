package com.wechantloup.thermostat.ui.thermostat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wechantloup.thermostat.R
import com.wechantloup.thermostat.model.DayTime
import com.wechantloup.thermostat.ui.compose.Loader
import com.wechantloup.thermostat.ui.compose.TopAppBar
import com.wechantloup.thermostat.ui.theme.Dimens
import com.wechantloup.thermostat.utils.toDayOfWeek

@Composable
internal fun DaySettingsScreen(
    day: Int,
    viewModel: ThermostatViewModel,
) {
    val state by viewModel.stateFlow.collectAsState()

    DaySettingsScreen(
        isLoading = state.loading,
        title = state.title,
        day = day,
        dayTime = state.automaticTemperatures[day],
        setDay = viewModel::setDay,
    )
}

@Composable
private fun DaySettingsScreen(
    isLoading: Boolean,
    title: String,
    day: Int,
    dayTime: DayTime,
    setDay: (day: Int, hour: Int, isDay: Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                text = title,
            )
        },
    ) {
        DaySettingsContent(
            day = day,
            dayTime = dayTime,
            setDay = setDay,
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        )
        Loader(
            isVisible = isLoading,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun DaySettingsContent(
    day: Int,
    dayTime: DayTime,
    setDay: (day: Int, hour: Int, isDay: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Dimens.spacing2w),
    ) {
        Text(text = day.toDayOfWeek())
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
            modifier = Modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Max),
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
            ) {
                dayTime.hours.forEachIndexed { index, _ ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(id = R.string.from_time_to_time, index, index + 1),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxHeight(),
            ) {
                dayTime.hours.forEachIndexed { index, mode ->
                    HourLine(
                        mode = mode,
                        setDay = { isDay: Boolean -> setDay(day, index, isDay)},
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

}

@Composable
private fun HourLine(
    mode: DayTime.Mode,
    setDay: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
        modifier = modifier,
    ) {
        Text(text = stringResource(id = R.string.night_label))
        Switch(
            checked = mode == DayTime.Mode.DAY,
            onCheckedChange = setDay,
        )
        Text(text = stringResource(id = R.string.day_label))
    }
}
