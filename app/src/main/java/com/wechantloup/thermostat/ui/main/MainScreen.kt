package com.wechantloup.thermostat.ui.main

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wechantloup.thermostat.R
import com.wechantloup.thermostat.ui.theme.Dimens
import com.wechantloup.thermostat.ui.theme.ThermostatTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun ThermostatScreen(
    viewModel: MainViewModel,
) {
    val state by viewModel.stateFlow.collectAsState()

    ThermostatScreen(
        currentTemperature = state.currentTemperature,
        currentlyOn = state.currentlyOn,
        poweredOn = state.poweredOn,
        availableModes = state.availableModes,
        selectedMode = state.selectedMode,
        manualTemperature = state.manualTemperature,
        power = viewModel::power,
        selectMode = viewModel::selectMode,
        setTemperature = viewModel::setTemperature,
    )
}

@Composable
private fun ThermostatScreen(
    currentTemperature: Float,
    currentlyOn: Boolean,
    poweredOn: Boolean,
    availableModes: ImmutableList<Mode>,
    selectedMode: Mode,
    manualTemperature: Int,
    power: (Boolean) -> Unit,
    selectMode: (Mode) -> Unit,
    setTemperature: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                text = stringResource(id = R.string.app_name),
            )
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(Dimens.spacing2w),
        ) {
            StateModule(currentTemperature, currentlyOn, Modifier.fillMaxWidth())
            PowerModule(poweredOn, power, Modifier.fillMaxWidth())

            if (!poweredOn) return@Column

            ModeModule(availableModes, selectedMode, selectMode, Modifier.fillMaxWidth())
            SetModule(
                selectedMode,
                manualTemperature,
                setTemperature,
                Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StateModule(
    currentTemperature: Float,
    currentlyOn: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
        modifier = modifier.height(IntrinsicSize.Min),
    ) {
        TemperatureModule(currentTemperature, Modifier.weight(1f))
        OnModule(currentlyOn)
    }
}

@Composable
private fun OnModule(
    currentlyOn: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.aspectRatio(1f),
        ) {
            val res = if (currentlyOn) R.drawable.ic_flame_24 else R.drawable.ic_stop_24
            Icon(
                painter = painterResource(id = res),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.8f),
            )
        }
    }
}

@Composable
private fun TemperatureModule(
    temperature: Float,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Dimens.spacing2w)
        ) {
            Text(
                text = stringResource(id = R.string.current_temperature_label, temperature),
            )
        }
    }
}

@Composable
private fun PowerModule(
    poweredOn: Boolean,
    power: (Boolean) -> Unit,
    modifier: Modifier = Modifier
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
    modifier: Modifier = Modifier
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
                        text = stringResource(id = mode.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SetModule(
    mode: Mode,
    manualTemperature: Int,
    setTemperature: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (mode) {
        Mode.MANUAL -> ManualModule(
            manualTemperature = manualTemperature,
            setTemperature = setTemperature,
            modifier = modifier,
        )
        Mode.AUTO -> AutomaticModule(
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.manual_temperature_label, manualTemperature),
                    fontSize = 64.sp,
                )
                Column(
                    modifier = Modifier.padding(start = Dimens.spacing2w),
                ) {
                    Button(
                        onClick = { setTemperature(manualTemperature + 1) }
                    ) {
                        Text("+")
                    }
                    Button(
                        onClick = { setTemperature(manualTemperature - 1) }
                    ) {
                        Text("-")
                    }
                }
            }
        }
    }
}

@Composable
private fun AutomaticModule(
    modifier: Modifier = Modifier,
) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    text: String,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
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

@Preview
@Composable
private fun StateModuleOnPreview() {
    ThermostatTheme {
        StateModule(currentTemperature = 19f, currentlyOn = true)
    }
}

@Preview
@Composable
private fun StateModuleOffPreview() {
    ThermostatTheme {
        StateModule(currentTemperature = 19f, currentlyOn = false)
    }
}

@Preview
@Composable
private fun TemperatureModulePreview() {
    ThermostatTheme {
        TemperatureModule(19f)
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
