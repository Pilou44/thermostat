package com.wechantloup.thermostat.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.wechantloup.thermostat.model.Command
import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.model.Status
import com.wechantloup.thermostat.usecase.CommandListener
import com.wechantloup.thermostat.usecase.StatusListener
import com.wechantloup.thermostat.usecase.ThermostatUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(MainSate())
    val stateFlow: StateFlow<MainSate> = _stateFlow

    private val useCase: ThermostatUseCase

    init {
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
        useCase = ThermostatUseCase(commandListener, statusListener)
    }

    fun power(on: Boolean) {
        showLoader()
        useCase.setPowered(on)
    }

    fun selectMode(mode: Mode) {
        showLoader()
        useCase.setMode(mode)
    }

    fun setTemperature(temperature: Int) {
        showLoader()
        if (temperature > MAX_TEMPERATURE || temperature < MIN_TEMPERATURE) return
        useCase.setManualTemperature(temperature)
    }

    private fun showLoader() {
        _stateFlow.value = stateFlow.value.copy(loading = true)
    }

    internal data class MainSate(
        val loading: Boolean = true, // ToDo
        val currentTemperature: Float = 0f,
        val currentlyOn: Boolean = false,
        val poweredOn: Boolean = false,
        val availableModes: ImmutableList<Mode> = Mode.entries.toPersistentList(),
        val selectedMode: Mode = Mode.entries.first(),
        val manualTemperature: Int = MIN_TEMPERATURE,
    )

    companion object {
        private const val MIN_TEMPERATURE = 5
        private const val MAX_TEMPERATURE = 25
    }
}


