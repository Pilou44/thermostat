package com.wechantloup.thermostat.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
internal fun ThermostatScreen(
    viewModel: MainViewModel,
) {
    val state by viewModel.stateFlow.collectAsState()

    ThermostatScreen(
        poweredOn = state.poweredOn,
        power = viewModel::power,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThermostatScreen(
    poweredOn: Boolean,
    power: (Boolean) -> Unit,
) {
    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        ) {
            Switch(
                checked = poweredOn,
                onCheckedChange = power,
            )
        }
    }
}
