package com.wechantloup.thermostat.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wechantloup.thermostat.R
import com.wechantloup.thermostat.model.Switch
import com.wechantloup.thermostat.ui.compose.Loader
import com.wechantloup.thermostat.ui.compose.TopAppBar
import com.wechantloup.thermostat.ui.theme.Dimens
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
    goBack: () -> Unit,
) {
    val state by viewModel.stateFlow.collectAsState()
    val saveAndGoBack = {
        viewModel.save(goBack)
    }

    SettingsScreen(
        isLoading = state.loading,
        title = state.title,
        id = state.id,
        name = state.name,
        switches = state.switches,
        setName = viewModel::setName,
        validate = saveAndGoBack,
    )
}

@Composable
private fun SettingsScreen(
    isLoading: Boolean,
    title: String,
    id: String,
    name: String?,
    switches: ImmutableList<Switch>,
    setName: (String) -> Unit,
    validate: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                text = title,
                actions = { Actions(validate) },
            )
        },
    ) {
        SettingsContent(
            id = id,
            name = name,
            switches = switches,
            setName = setName,
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
    validate: () -> Unit,
) {
    Button(onClick = validate) {
        Text(stringResource(id = R.string.validate_button_label))
    }
}

@Composable
private fun SettingsContent(
    id: String,
    name: String?,
    switches: ImmutableList<Switch>,
    setName: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(Dimens.spacing2w),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
        modifier = modifier,
    ) {
        item(
            key = "id",
            contentType = "id",
        ) {
            Text(stringResource(id = R.string.device_id_label, id))
        }
        item {
            Name(name, setName)
        }
        items(
            count = switches.size,
        ) { index ->
            Switch(switches[index])
        }
    }
}

@Composable
private fun Name(
    name: String?,
    setName: (String) -> Unit,
) {
    TextField(
        value = name.orEmpty(),
        onValueChange = setName,
        label = { Text(stringResource(id = R.string.name_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
private fun Switch(switch: Switch) {

}
