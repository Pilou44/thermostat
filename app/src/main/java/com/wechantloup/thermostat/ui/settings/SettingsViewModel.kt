package com.wechantloup.thermostat.ui.settings

import android.app.Application
import android.net.InetAddresses
import android.os.Build
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wechantloup.thermostat.model.Device
import com.wechantloup.thermostat.model.KnownSwitch
import com.wechantloup.thermostat.model.Switch
import com.wechantloup.thermostat.repository.SwitchRepository
import com.wechantloup.thermostat.usecase.CreateNewSwitchUseCase
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
    private val createNewSwitchUseCase = CreateNewSwitchUseCase()

    private var deviceId: String? = null

    fun reload(deviceId: String) {
        this.deviceId = deviceId

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
        val device = Device(
            id = stateFlow.value.id,
            name = stateFlow.value.name,
        )
        viewModelScope.launch {
            settingsUseCase.saveDevice(device, cb)
        }
    }

    fun createNewSwitch(newSwitch: Switch) {
        viewModelScope.launch {
            _stateFlow.emit(stateFlow.value.copy(loading = true))

            val isIpValid = if (Build.VERSION.SDK_INT >= 29) {
                InetAddresses.isNumericAddress(newSwitch.address)
            } else {
                @Suppress("DEPRECATION")
                Patterns.IP_ADDRESS.matcher(newSwitch.address).matches()
            }

            if (!isIpValid) {
                _stateFlow.emit(
                    stateFlow.value.copy(
                        loading = false,
                        createSwitchStatus = CreateSwitchStatus.BAD_ADDRESS,
                    )
                )
                return@launch
            }

            try {
                if (createNewSwitchUseCase.execute(newSwitch)) {
                    _stateFlow.emit(
                        stateFlow.value.copy(
                            loading = false,
                            createSwitchStatus = CreateSwitchStatus.SUCCESS,
                        )
                    )
                    reload(requireNotNull(newSwitch.pairedDeviceId))
                } else {
                    _stateFlow.emit(
                        stateFlow.value.copy(
                            loading = false,
                            createSwitchStatus = CreateSwitchStatus.USED_ADDRESS,
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving switch", e)
                _stateFlow.emit(
                    stateFlow.value.copy(
                        loading = false,
                        createSwitchStatus = CreateSwitchStatus.ERROR,
                    )
                )
            }
        }
    }

    fun startCreateNewSwitch() {
        _stateFlow.value = stateFlow.value.copy(createSwitchStatus = null)
    }

    fun unpairSwitch(switch: Switch) {
        val deviceId = deviceId ?: return

        viewModelScope.launch {
            _stateFlow.emit(stateFlow.value.copy(loading = true))
            SwitchRepository.unPair(switch)
            val switches = settingsUseCase.getPairedSwitches(deviceId)
            val knownSwitches = getKnownSwitchesUseCase.execute(deviceId)
            _stateFlow.emit(
                stateFlow.value.copy(
                    loading = false,
                    switches = switches.toImmutableList(),
                    knownSwitches = knownSwitches.toImmutableList(),
                )
            )
        }
    }

    fun removeSwitch(switch: Switch) {
        TODO("Not yet implemented")
    }

    internal data class SettingsState(
        val loading: Boolean = true,
        val title: String = "",
        val id: String = "",
        val name: String = "",
        val switches: ImmutableList<Switch> = persistentListOf(),
        val knownSwitches: ImmutableList<KnownSwitch> = persistentListOf(),
        val createSwitchStatus: CreateSwitchStatus? = null,
    )

    companion object {
        const val TAG = "SettingsViewModel"
    }
}
