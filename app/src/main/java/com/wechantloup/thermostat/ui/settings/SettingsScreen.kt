package com.wechantloup.thermostat.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.wechantloup.thermostat.R
import com.wechantloup.thermostat.model.KnownSwitch
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
        knownSwitches = state.knownSwitches,
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
    knownSwitches: ImmutableList<KnownSwitch>,
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
            knownSwitches = knownSwitches,
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
    Button(
        onClick = validate,
        modifier = Modifier.padding(end = Dimens.spacing2w),
    ) {
        Text(stringResource(id = R.string.validate_button_label))
    }
}

@Composable
private fun SettingsContent(
    id: String,
    name: String?,
    switches: ImmutableList<Switch>,
    knownSwitches: ImmutableList<KnownSwitch>,
    setName: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isAddingSwitch by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(Dimens.spacing2w),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        item(
            key = "id",
            contentType = "id",
        ) {
            Text(stringResource(id = R.string.device_id_label, id))
        }
        item(
            key = "name",
            contentType = "name",
        ) {
            Name(name, setName)
        }
        items(
            count = switches.size,
            key = { index -> "switch$index" },
            contentType = { "switch" },
        ) { index ->
            Switch(switches[index])
        }
        if (isAddingSwitch) {
            item(
                key = "adding_switch",
                contentType = "adding_switch",
            ) {
                AddingSwitch(
                    knownSwitches = knownSwitches,
                    validate = {},
                    cancel = { isAddingSwitch = false},
                )
            }
        }
        item(
            key = "add_switch",
            contentType = "add_switch",
        ) {
            Button(
                onClick = { isAddingSwitch = true},
                enabled = !isAddingSwitch,
            ) {
                Text(stringResource(id = R.string.add_switch_button_label))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddingSwitch(
    knownSwitches: ImmutableList<KnownSwitch>,
//    createNewSwitch: () -> Unit,
    validate: () -> Unit,
    cancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        var expanded by remember { mutableStateOf(false) }
        var selectedValue by remember { mutableStateOf(" ") }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            },
            modifier = Modifier.weight(1f),
        ) {
            TextField(
                modifier = Modifier
                    .menuAnchor(), // menuAnchor modifier must be passed to the text field for correctness.
                readOnly = true,
                value = selectedValue,
                onValueChange = {},
                label = { Text(stringResource(id = R.string.switches_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
            ) {
                knownSwitches.forEach { selectionOption ->
                    val context = LocalContext.current
                    DropdownMenuItem(
                        text = { Text(selectionOption.getLabel(context)) },
                        onClick = {
                            selectedValue = selectionOption.getLabel(context)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
                val createNewSwitchLabel = stringResource(id = R.string.create_new_switch_label)
                DropdownMenuItem(
                    text = { Text(createNewSwitchLabel) },
                    onClick = {
                        selectedValue = createNewSwitchLabel
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
        IconButton(onClick = validate) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_check_16),
                contentDescription = stringResource(id = R.string.validate_button_label),
            )
        }
        IconButton(onClick = cancel) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete_16),
                contentDescription = stringResource(id = R.string.delete_button_label),
            )
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
