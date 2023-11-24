package com.wechantloup.thermostat.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wechantloup.thermostat.model.CommandDevice
import com.wechantloup.thermostat.model.KnownSwitch
import com.wechantloup.thermostat.model.Switch
import com.wechantloup.thermostat.usecase.GetKnownSwitchesUseCase
import com.wechantloup.thermostat.usecase.SettingsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(SettingsState())
    val stateFlow: StateFlow<SettingsState> = _stateFlow

    private val settingsUseCase = SettingsUseCase()
    private val getKnownSwitchesUseCase = GetKnownSwitchesUseCase()

    fun reload(deviceId: String) {
        viewModelScope.launch {
            _stateFlow.emit(stateFlow.value.copy(loading = true))

            val device = settingsUseCase.getDevice(deviceId)
            val switches = settingsUseCase.getPairedSwitches(deviceId)
            val knownSwitches = getKnownSwitchesUseCase.execute(deviceId)

            _stateFlow.emit(
                stateFlow.value.copy(
                    loading = false,
                    title = device.getLabel(),
                    id = deviceId,
                    name = device.name,
                    switches = switches.toImmutableList(),
                    knownSwitches = knownSwitches.toImmutableList(),
                )
            )
        }
    }

    fun setName(name: String) {
        _stateFlow.value = stateFlow.value.copy(name = name)
    }

    fun save(cb: () -> Unit) {
        val device = CommandDevice(
            id = stateFlow.value.id,
            name = stateFlow.value.name,
        )
        viewModelScope.launch {
            settingsUseCase.saveDevice(device, cb)
        }
    }

    internal data class SettingsState(
        val loading: Boolean = true,
        val title: String = "",
        val id: String = "",
        val name: String = "",
        val switches: ImmutableList<Switch> = persistentListOf(),
        val knownSwitches: ImmutableList<KnownSwitch> = persistentListOf(),
    )

    companion object {
        const val TAG = "SettingsViewModel"
    }
}
