package com.wechantloup.thermostat.ui.settings

import android.net.InetAddresses
import android.os.Build
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.wechantloup.thermostat.R
import com.wechantloup.thermostat.model.KnownSwitch
import com.wechantloup.thermostat.model.Switch
import com.wechantloup.thermostat.model.SwitchType
import com.wechantloup.thermostat.ui.compose.Loader
import com.wechantloup.thermostat.ui.compose.TopAppBar
import com.wechantloup.thermostat.ui.theme.Dimens
import com.wechantloup.thermostat.ui.theme.ThermostatTheme
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
        startCreateNewSwitch = viewModel::startCreateNewSwitch,
        createNewSwitch = viewModel::createNewSwitch,
        createNewSwitchStatus = state.createSwitchStatus,
        addSwitch = viewModel::addSwitch,
        unpair = viewModel::unpairSwitch,
        remove = viewModel::removeSwitch,
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
    startCreateNewSwitch: () -> Unit,
    createNewSwitch: (Switch) -> Unit,
    createNewSwitchStatus: CreateSwitchStatus?,
    addSwitch: (Switch) -> Unit,
    unpair: (Switch) -> Unit,
    remove: (Switch) -> Unit,
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
            isLoading = isLoading,
            id = id,
            name = name,
            switches = switches,
            knownSwitches = knownSwitches,
            setName = setName,
            startCreateNewSwitch = startCreateNewSwitch,
            createNewSwitch = createNewSwitch,
            createNewSwitchStatus = createNewSwitchStatus,
            addSwitch = addSwitch,
            unpair = unpair,
            remove = remove,
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
    isLoading: Boolean,
    id: String,
    name: String?,
    switches: ImmutableList<Switch>,
    knownSwitches: ImmutableList<KnownSwitch>,
    setName: (String) -> Unit,
    startCreateNewSwitch: () -> Unit,
    createNewSwitch: (Switch) -> Unit,
    createNewSwitchStatus: CreateSwitchStatus?,
    addSwitch: (Switch) -> Unit,
    unpair: (Switch) -> Unit,
    remove: (Switch) -> Unit,
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
            Switch(
                isLoading = isLoading,
                switch = switches[index],
                unpair = unpair,
                remove = remove,
            )
        }
        if (isAddingSwitch) {
            item(
                key = "adding_switch",
                contentType = "adding_switch",
            ) {
                AddingSwitch(
                    isLoading = isLoading,
                    id = id,
                    knownSwitches = knownSwitches,
                    startCreateNewSwitch = startCreateNewSwitch,
                    createNewSwitch = createNewSwitch,
                    createNewSwitchStatus = createNewSwitchStatus,
                    addSwitch = addSwitch,
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
    isLoading: Boolean,
    id: String,
    knownSwitches: ImmutableList<KnownSwitch>,
    startCreateNewSwitch: () -> Unit,
    createNewSwitch: (Switch) -> Unit,
    createNewSwitchStatus: CreateSwitchStatus?,
    addSwitch: (Switch) -> Unit,
    cancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateSwitchDialog(
            isLoading = isLoading,
            id = id,
            createNewSwitch = createNewSwitch,
            createNewSwitchStatus = createNewSwitchStatus,
            dismiss = {
                showCreateDialog = false
                cancel()
            },
        )
    }

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        var expanded by remember { mutableStateOf(false) }
        var selectedValue by remember { mutableStateOf<Any?>(null) }
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
                value = when (val value = selectedValue) {
                    is String -> value
                    is KnownSwitch -> value.getLabel(LocalContext.current)
                    else -> ""
                },
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
                            selectedValue = selectionOption
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
                val createNewSwitchLabel = stringResource(id = R.string.create_new_switch_label)
                DropdownMenuItem(
                    text = { Text(createNewSwitchLabel) },
                    onClick = {
                        startCreateNewSwitch()
                        selectedValue = createNewSwitchLabel
                        expanded = false
                        showCreateDialog = true
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
        IconButton(
            enabled = selectedValue is KnownSwitch,
            onClick = {
                addSwitch((selectedValue as KnownSwitch).switch)
                cancel()
            },
        ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSwitchDialog(
    isLoading: Boolean,
    id: String,
    createNewSwitch: (Switch) -> Unit,
    createNewSwitchStatus: CreateSwitchStatus?,
    dismiss: () -> Unit,
) {
    var ip by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val values = SwitchType.values()
    var selectedValue by remember { mutableStateOf(values.first()) }
    var errorMessageId by remember { mutableIntStateOf(0) }

    when (createNewSwitchStatus) {
        CreateSwitchStatus.SUCCESS -> {
            errorMessageId = 0
            dismiss()
        }
        CreateSwitchStatus.BAD_ADDRESS -> errorMessageId = R.string.bad_ip_error_message
        CreateSwitchStatus.USED_ADDRESS -> errorMessageId = R.string.used_ip_error_message
        CreateSwitchStatus.ERROR -> errorMessageId = R.string.unknown_error_message
        null -> errorMessageId = 0
    }

    Dialog(onDismissRequest = dismiss) {
        Card {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
                modifier = Modifier.padding(Dimens.spacing2w)
            ) {
                if (errorMessageId > 0) {
                    Text(
                        text = stringResource(id = errorMessageId),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = stringResource(id = R.string.create_switch_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text(stringResource(id = R.string.ip_label)) },
                    enabled = !isLoading,
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    },
                ) {
                    TextField(
                        readOnly = true,
                        value = selectedValue.getName(),
                        onValueChange = {},
                        label = { Text(stringResource(id = R.string.switches_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        enabled = !isLoading,
                        modifier = Modifier.menuAnchor(),
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        },
                    ) {
                        values.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.getName()) },
                                onClick = {
                                    selectedValue = selectionOption
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        val newSwitch = Switch(
                            address = ip,
                            type = selectedValue,
                            pairedDeviceId = id,
                        )
                        createNewSwitch(newSwitch)
                    },
                    enabled = ip.trim().isValidIp() && !isLoading,
                ) {
                    Text(text = stringResource(id = R.string.validate_button_label))
                }
            }
        }
    }
}

private fun String.isValidIp(): Boolean = if (Build.VERSION.SDK_INT >= 29) {
    InetAddresses.isNumericAddress(this)
} else {
    @Suppress("DEPRECATION")
    Patterns.IP_ADDRESS.matcher(this).matches()
}

@Composable
private fun SwitchType.getName(): String {
    val context = LocalContext.current
    return when (this) {
        SwitchType.SHELLY_PLUS_1 -> context.getString(R.string.shelly_plus_1_type)
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
private fun Switch(
    isLoading: Boolean,
    switch: Switch,
    unpair: (Switch) -> Unit,
    remove: (Switch) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    if (showRemoveDialog) {
        RemoveSwitchDialog(
            isLoading = isLoading,
            switch = switch,
            unpair = unpair,
            remove = remove,
            dismiss = { showRemoveDialog = false },
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "${switch.address} / ${switch.type.getName()}",
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { showRemoveDialog = true }) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete_16),
                contentDescription = stringResource(id = R.string.delete_button_label),
            )
        }
    }
}

@Composable
fun RemoveSwitchDialog(
    isLoading: Boolean,
    switch: Switch,
    unpair: (Switch) -> Unit,
    remove: (Switch) -> Unit,
    dismiss: () -> Unit,
) {
    Dialog(onDismissRequest = dismiss) {
        Card {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
                modifier = Modifier.padding(Dimens.spacing2w)
            ) {
                Text(
                    text = stringResource(id = R.string.remove_switch_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing2w),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = {
                            unpair(switch)
                            dismiss()
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(id = R.string.unpair_button_label))
                    }
                    Button(
                        onClick = {
                            remove(switch)
                            dismiss()
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(id = R.string.delete_button_label))
                    }
                }
            }
        }
    }
}

internal enum class CreateSwitchStatus {
    SUCCESS,
    BAD_ADDRESS,
    USED_ADDRESS,
    ERROR,
}

@Preview
@Composable
private fun CreateSwitchDialogPreview() {
    ThermostatTheme {
        CreateSwitchDialog(
            isLoading = false,
            id = "toto",
            createNewSwitch = {},
            createNewSwitchStatus = null,
            dismiss = {},
        )
    }
}

@Preview
@Composable
private fun RemoveSwitchDialogPreview() {
    ThermostatTheme {
        RemoveSwitchDialog(
            isLoading = false,
            switch = Switch("", SwitchType.SHELLY_PLUS_1, ""),
            unpair = {},
            remove = {},
            dismiss = {},
        )
    }
}
