package com.wechantloup.thermostat.ui.thermostat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wechantloup.thermostat.model.Command
import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.model.Status
import com.wechantloup.thermostat.usecase.CommandListener
import com.wechantloup.thermostat.usecase.StatusListener
import com.wechantloup.thermostat.usecase.ThermostatUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ThermostatViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(ThermostatSate())
    val stateFlow: StateFlow<ThermostatSate> = _stateFlow

    private var thermostatUseCase: ThermostatUseCase? = null

    init {
        relaunch()
    }

    fun relaunch() {
        viewModelScope.launch {
            init()
        }
    }

    private suspend fun init() {
        _stateFlow.emit(stateFlow.value.copy(loading = true))
        initThermostat()
    }

    private suspend fun initThermostat() {
        val commandListener: CommandListener = object : CommandListener {
            override fun onCommandReceived(command: Command) {
                _stateFlow.value = stateFlow.value.copy(
                    loading = false,
                    poweredOn = command.powerOn,
                    selectedMode = command.mode,
                    manualTemperature = command.manualTemperature,
                )
            }
        }
        val statusListener: StatusListener = object : StatusListener {
            override fun onStatusReceived(status: Status) {
                _stateFlow.value = stateFlow.value.copy(
                    currentTemperature = status.temperature,
                    currentlyOn = status.on,
                )
            }
        }

        withContext(Dispatchers.IO) {
            thermostatUseCase = ThermostatUseCase(commandListener, statusListener)
        }
    }

    fun power(on: Boolean) {
        thermostatUseCase?.let {
            showLoader()
            it.setPowered(on)
        }
    }

    fun selectMode(mode: Mode) {
        thermostatUseCase?.let {
            showLoader()
            it.setMode(mode)
        }
    }

    fun setTemperature(temperature: Int) {
        if (temperature > MAX_TEMPERATURE || temperature < MIN_TEMPERATURE) return
        thermostatUseCase?.let {
            showLoader()
            it.setManualTemperature(temperature)
        }
    }

    private fun showLoader() {
        _stateFlow.value = stateFlow.value.copy(loading = true)
    }

    internal data class ThermostatSate(
        val loading: Boolean = true,
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


