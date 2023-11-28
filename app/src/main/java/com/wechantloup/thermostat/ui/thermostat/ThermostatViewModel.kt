package com.wechantloup.thermostat.ui.thermostat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wechantloup.thermostat.model.Device
import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.usecase.HasCommandsUseCase
import com.wechantloup.thermostat.usecase.SubscribeToCommandUseCase
import com.wechantloup.thermostat.usecase.SubscribeToStatusUseCase
import com.wechantloup.thermostat.usecase.ThermostatUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class ThermostatViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(ThermostatSate())
    val stateFlow: StateFlow<ThermostatSate> = _stateFlow

    private val thermostatUseCase = ThermostatUseCase()
    private val hasCommandsUseCase = HasCommandsUseCase()
    private val subscribeToCommandsUseCase = SubscribeToCommandUseCase()
    private val subscribeToStatusUseCase = SubscribeToStatusUseCase()

    private var statusJob: Job? = null
    private var commandJob: Job? = null
    private var roomId: String? = null
    override fun onCleared() {
        super.onCleared()
        statusJob?.cancel()
        commandJob?.cancel()
    }

    fun setRoomId(id: String) {
        showLoader()
        _stateFlow.value = stateFlow.value.copy(
            title = "",
            poweredOn = false,
            selectedMode = Mode.entries.first(),
            manualTemperature = MIN_TEMPERATURE,
        )

        statusJob?.cancel()
        commandJob?.cancel()

        roomId = id

        viewModelScope.launch {
            val device = Device.get(id)
            _stateFlow.value = stateFlow.value.copy(title = device.getLabel())

            if (!hasCommandsUseCase.execute(id)) {
                _stateFlow.value = stateFlow.value.copy(loading = false)
            }
        }

        statusJob = viewModelScope.launch {
            subscribeToStatusUseCase.execute(id)
                .collect { status ->
                    _stateFlow.value = stateFlow.value.copy(
                        currentTemperature = status.temperature,
                        currentlyOn = status.on,
                    )
                }
        }

        commandJob = viewModelScope.launch {
            subscribeToCommandsUseCase.execute(id)
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
        val roomId = roomId ?: return
        showLoader()
        thermostatUseCase.setPowered(roomId, on)
    }

    fun selectMode(mode: Mode) {
        val roomId = roomId ?: return
        showLoader()
        thermostatUseCase.setMode(roomId, mode)
    }

    fun setTemperature(temperature: Int) {
        if (temperature > MAX_TEMPERATURE || temperature < MIN_TEMPERATURE) return
        val roomId = roomId ?: return
        showLoader()
        thermostatUseCase.setManualTemperature(roomId, temperature)
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


