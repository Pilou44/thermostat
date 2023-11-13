package com.wechantloup.thermostat.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.wechantloup.thermostat.R
import com.wechantloup.thermostat.ui.theme.Dimens

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.power_label),
                )
                Switch(
                    checked = poweredOn,
                    onCheckedChange = power,
                )
            }
        }
    }
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
