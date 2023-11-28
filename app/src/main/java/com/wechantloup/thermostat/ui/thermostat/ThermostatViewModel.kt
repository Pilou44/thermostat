package com.wechantloup.thermostat.ui.thermostat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.usecase.SettingsUseCase
import com.wechantloup.thermostat.usecase.ThermostatUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class ThermostatViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(ThermostatSate())
    val stateFlow: StateFlow<ThermostatSate> = _stateFlow

    private val thermostatUseCase = ThermostatUseCase()
    private val settingsUseCase = SettingsUseCase()

    private var statusJob: Job? = null
    private var commandJob: Job? = null

    fun setRoomId(roomId: String) {
        _stateFlow.value = stateFlow.value.copy(loading = true)

        statusJob?.cancel()
        commandJob?.cancel()

        val deferredRoomId = viewModelScope.async { thermostatUseCase.setRoomId(roomId) }

        statusJob = viewModelScope.launch {
            deferredRoomId.await()
            Log.d("TEST", "subscribeToStatuses")
            thermostatUseCase.subscribeToStatuses()
                .collect { status ->
                    _stateFlow.value = stateFlow.value.copy(
                        currentTemperature = status.temperature,
                        currentlyOn = status.on,
                    )
                }
        }

        commandJob = viewModelScope.launch {
            deferredRoomId.await()
            Log.d("TEST", "subscribeToCommands")
            thermostatUseCase.subscribeToCommands()
                .collect { command ->
                    _stateFlow.value = stateFlow.value.copy(
                        loading = false,
                        poweredOn = command.powerOn,
                        selectedMode = command.mode,
                        manualTemperature = command.manualTemperature,
                    )
                }
        }
    }

    fun power(on: Boolean) {
        showLoader()
        thermostatUseCase.setPowered(on)
    }

    fun selectMode(mode: Mode) {
        showLoader()
        thermostatUseCase.setMode(mode)
    }

    fun setTemperature(temperature: Int) {
        if (temperature > MAX_TEMPERATURE || temperature < MIN_TEMPERATURE) return
        showLoader()
        thermostatUseCase.setManualTemperature(temperature)
    }

    private fun showLoader() {
        _stateFlow.value = stateFlow.value.copy(loading = true)
    }

    internal data class ThermostatSate(
        val loading: Boolean = true,
        val title: String = "",
        val currentTemperature: Float = 0f,
        val currentlyOn: Boolean = false,
        val poweredOn: Boolean = false,
        val availableModes: ImmutableList<Mode> = Mode.entries.toPersistentList(),
        val selectedMode: Mode = Mode.entries.first(),
        val manualTemperature: Int = MIN_TEMPERATURE,
    )

    companion object {
        const val TAG = "ThermostatViewModel"

        private const val MIN_TEMPERATURE = 5
        private const val MAX_TEMPERATURE = 25
    }
}


