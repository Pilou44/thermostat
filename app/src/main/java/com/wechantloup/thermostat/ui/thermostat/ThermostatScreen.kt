package com.wechantloup.thermostat.ui.thermostat

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.wechantloup.thermostat.R
import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.ui.compose.Loader
import com.wechantloup.thermostat.ui.compose.TopAppBar
import com.wechantloup.thermostat.ui.theme.Dimens
import com.wechantloup.thermostat.ui.theme.ThermostatTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.roundToInt

@Composable
internal fun ThermostatScreen(
    viewModel: ThermostatViewModel,
    goToSettings: () -> Unit,
) {
    val state by viewModel.stateFlow.collectAsState()

    ThermostatScreen(
        isLoading = state.loading,
        title = state.title,
        currentTemperature = state.currentTemperature,
        currentHumidity = state.currentHumidity,
        currentlyOn = state.currentlyOn,
        lastTimeUpdated = state.lastTimeUpdated,
        poweredOn = state.poweredOn,
        availableModes = state.availableModes,
        selectedMode = state.selectedMode,
        manualTemperature = state.manualTemperature,
        dayTemperature = state.dayTemperature,
        nightTemperature = state.nightTemperature,
        power = viewModel::power,
        selectMode = viewModel::selectMode,
        setTemperature = viewModel::setTemperature,
        setDayTemperature = viewModel::setDayTemperature,
        setNightTemperature = viewModel::setNightTemperature,
        goToSettings = goToSettings,
    )
}

@Composable
private fun ThermostatScreen(
    isLoading: Boolean,
    title: String,
    currentTemperature: Float,
    currentHumidity: Float,
    currentlyOn: Boolean,
    lastTimeUpdated: String,
    poweredOn: Boolean,
    availableModes: ImmutableList<Mode>,
    selectedMode: Mode,
    manualTemperature: Int,
    dayTemperature: Int,
    nightTemperature: Int,
    power: (Boolean) -> Unit,
    selectMode: (Mode) -> Unit,
    setTemperature: (Int) -> Unit,
    setDayTemperature: (Int) -> Unit,
    setNightTemperature: (Int) -> Unit,
    goToSettings: () -> Unit,
) {
    Log.d("TEST", "Recompose ThermostatScreen")
    Scaffold(
        topBar = {
            TopAppBar(
                text = title,
                actions = { Actions(goToSettings) },
            )
        },
    ) {
        ThermostatContent(
            currentTemperature = currentTemperature,
            currentHumidity = currentHumidity,
            currentlyOn = currentlyOn,
            lastTimeUpdated = lastTimeUpdated,
            poweredOn = poweredOn,
            availableModes = availableModes,
            selectedMode = selectedMode,
            manualTemperature = manualTemperature,
            dayTemperature = dayTemperature,
            nightTemperature = nightTemperature,
            power = power,
            selectMode = selectMode,
            setTemperature = setTemperature,
            setDayTemperature = setDayTemperature,
            setNightTemperature = setNightTemperature,
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
private fun Actions(
    goToSettings: () -> Unit,
) {
    IconButton(
        onClick = goToSettings,
        modifier = Modifier.padding(end = Dimens.spacing2w),
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_settings_24),
            contentDescription = stringResource(id = R.string.settings_button_label),
        )
    }
}

@Composable
private fun ThermostatContent(
    currentTemperature: Float,
    currentHumidity: Float,
    currentlyOn: Boolean,
    lastTimeUpdated: String,
    poweredOn: Boolean,
    availableModes: ImmutableList<Mode>,
    selectedMode: Mode,
    manualTemperature: Int,
    dayTemperature: Int,
    nightTemperature: Int,
    power: (Boolean) -> Unit,
    selectMode: (Mode) -> Unit,
    setTemperature: (Int) -> Unit,
    setDayTemperature: (Int) -> Unit,
    setNightTemperature: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
        modifier = modifier.padding(Dimens.spacing2w),
    ) {
        StateModule(
            currentTemperature,
            currentHumidity,
            currentlyOn,
            lastTimeUpdated,
            Modifier.fillMaxWidth(),
        )
        PowerModule(poweredOn, power, Modifier.fillMaxWidth())

        if (!poweredOn) return@Column

        ModeModule(availableModes, selectedMode, selectMode, Modifier.fillMaxWidth())
        SetModule(
            mode = selectedMode,
            manualTemperature = manualTemperature,
            dayTemperature = dayTemperature,
            nightTemperature = nightTemperature,
            setTemperature = setTemperature,
            setDayTemperature = setDayTemperature,
            setNightTemperature = setNightTemperature,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StateModule(
    currentTemperature: Float,
    currentHumidity: Float,
    currentlyOn: Boolean,
    lastUpdated: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
            modifier = Modifier.padding(Dimens.spacing2w),
        ) {
            Text(
                text = stringResource(id = R.string.last_time_updated_label, lastUpdated),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
                modifier = Modifier.height(IntrinsicSize.Min),
            ) {
                TemperatureModule(currentTemperature, Modifier.weight(1f))
                HumidityModule(currentHumidity, Modifier.weight(1f))
                OnModule(currentlyOn)
            }
        }
    }
}

@Composable
private fun OnModule(
    currentlyOn: Boolean,
    modifier: Modifier = Modifier,
) {
    val res = if (currentlyOn) R.drawable.ic_flame_24 else R.drawable.ic_stop_24
    Icon(
        imageVector = ImageVector.vectorResource(id = res),
        contentDescription = null,
        modifier = modifier,
    )
}

@Composable
private fun TemperatureModule(
    temperature: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_thermostat_24),
            contentDescription = stringResource(id = R.string.current_temperature_label),
        )
        Text(
            text = stringResource(id = R.string.float_temperature_label, temperature),
        )
    }
}

@Composable
private fun HumidityModule(
    humidity: Float,
    modifier: Modifier = Modifier,
) {
    if (humidity < 0) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_water_drop_24),
            contentDescription = stringResource(id = R.string.current_humidity_label),
        )
        Text(
            text = stringResource(id = R.string.humidity_label, humidity.roundToInt()),
        )
    }
}

@Composable
private fun PowerModule(
    poweredOn: Boolean,
    power: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = Dimens.spacing2w,
                vertical = Dimens.spacing1w,
            )
        ) {
            Text(
                text = stringResource(id = R.string.power_label),
            )
            Switch(
                checked = poweredOn,
                onCheckedChange = power,
                modifier = Modifier.padding(start = Dimens.spacing2w)
            )
        }
    }
}

@Composable
private fun ModeModule(
    availableModes: ImmutableList<Mode>,
    selectedMode: Mode,
    selectMode: (Mode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.width(IntrinsicSize.Min)
    ) {
        Column {
            availableModes.forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectMode(mode) }
                        .padding(end = Dimens.spacing2w),
                ) {
                    RadioButton(
                        selected = mode == selectedMode,
                        onClick = { selectMode(mode) }
                    )
                    Text(
                        text = stringResource(id = getModeLabelRes(mode)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@StringRes
private fun getModeLabelRes(mode: Mode): Int = when (mode) {
    Mode.MANUAL -> R.string.manual_mode_label
    Mode.AUTO -> R.string.auto_mode_label
}

@Composable
private fun SetModule(
    mode: Mode,
    manualTemperature: Int,
    dayTemperature: Int,
    nightTemperature: Int,
    setTemperature: (Int) -> Unit,
    setDayTemperature: (Int) -> Unit,
    setNightTemperature: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (mode) {
        Mode.MANUAL -> ManualModule(
            manualTemperature = manualTemperature,
            setTemperature = setTemperature,
            modifier = modifier,
        )
        Mode.AUTO -> AutomaticModule(
            dayTemperature = dayTemperature,
            nightTemperature = nightTemperature,
            setDayTemperature = setDayTemperature,
            setNightTemperature = setNightTemperature,
            modifier = modifier,
        )
    }
}

@Composable
private fun ManualModule(
    manualTemperature: Int,
    setTemperature: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(Dimens.spacing2w),
        ) {
            TemperatureSetter(
                temperature = manualTemperature,
                setTemperature = setTemperature,
            )
        }
    }
}

@Composable
private fun TemperatureSetter(
    temperature: Int,
    setTemperature: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(id = R.string.int_temperature_label, temperature),
            fontSize = 64.sp,
        )
        Column(
            modifier = Modifier.padding(start = Dimens.spacing2w),
        ) {
            Button(
                onClick = { setTemperature(temperature + 1) }
            ) {
                Text("+")
            }
            Button(
                onClick = { setTemperature(temperature - 1) }
            ) {
                Text("-")
            }
        }
    }
}

@Composable
private fun AutomaticModule(
    dayTemperature: Int,
    nightTemperature: Int,
    setDayTemperature: (Int) -> Unit,
    setNightTemperature: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            TemperatureSetter(
                temperature = dayTemperature,
                setTemperature = setDayTemperature,
                modifier = Modifier.weight(1f)
            )
            TemperatureSetter(
                temperature = nightTemperature,
                setTemperature = setNightTemperature,
                modifier = Modifier.weight(1f)
            )
        }
//        DayTimeLine(
//            dayTime = dayTime,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp)
//                .background(MaterialTheme.colorScheme.background)
//        )
//        DayTimeLine(
//            dayTime = dayTime,
//            modifier = Modifier
//                .width(200.dp)
//                .height(50.dp)
//                .background(MaterialTheme.colorScheme.background)
//        )
    }
}

@Preview
@Composable
private fun StateModuleOnPreview() {
    ThermostatTheme {
        StateModule(
            currentTemperature = 19f,
            currentHumidity = 60f,
            currentlyOn = true,
            lastUpdated = "Tuesday, 08:30",
        )
    }
}

@Preview
@Composable
private fun StateModuleOffPreview() {
    ThermostatTheme {
        StateModule(
            currentTemperature = 19f,
            currentHumidity = 60f,
            currentlyOn = false,
            lastUpdated = "Tuesday, 08:30",
        )
    }
}

@Preview
@Composable
private fun PowerModulePreview() {
    ThermostatTheme {
        PowerModule(true, {})
    }
}

@Preview
@Composable
private fun ModeModulePreview() {
    val availableModes = Mode.entries.toImmutableList()
    val selectedMode = availableModes.first()
    ThermostatTheme {
        ModeModule(availableModes, selectedMode, {})
    }
}

@Preview
@Composable
private fun ManualModulePreview() {
    ThermostatTheme {
        ManualModule(19, {})
    }
}
