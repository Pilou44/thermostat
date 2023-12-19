package com.wechantloup.thermostat.ui.thermostat

import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wechantloup.thermostat.model.Command
import com.wechantloup.thermostat.model.DayTime
import com.wechantloup.thermostat.model.Device
import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.repository.CommandRepository
import com.wechantloup.thermostat.repository.ThermostatRepository
import com.wechantloup.thermostat.usecase.HasCommandsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.GregorianCalendar

internal class ThermostatViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(ThermostatSate())
    val stateFlow: StateFlow<ThermostatSate> = _stateFlow

    private val hasCommandsUseCase = HasCommandsUseCase()

    private var thermostatJob: Job? = null
    private var commandJob: Job? = null
    private var roomId: String? = null
    override fun onCleared() {
        super.onCleared()
        thermostatJob?.cancel()
        commandJob?.cancel()
    }

    fun setRoomId(id: String) {
        showLoader()
        val newCommand = Command()
        _stateFlow.value = stateFlow.value.copy(
            title = "",
            poweredOn = false,
        )

        thermostatJob?.cancel()
        commandJob?.cancel()

        roomId = id

        viewModelScope.launch {
            val device = Device.get(id)
            _stateFlow.value = stateFlow.value.copy(title = device.getLabel())

            if (!hasCommandsUseCase.execute(id)) {
                CommandRepository.setCommand(id, newCommand)
            }
        }

        thermostatJob = viewModelScope.launch {
            ThermostatRepository.subscribe(id)
                .collect { thermostat ->
                    _stateFlow.value = stateFlow.value.copy(
                        currentTemperature = thermostat.temperature,
                        currentHumidity = thermostat.humidity,
                        currentlyOn = thermostat.on,
                        lastTimeUpdated = thermostat.time.toDayTime()
                    )
                }
        }

        commandJob = viewModelScope.launch {
            CommandRepository.subscribe(id)
                .collect { command ->
                    _stateFlow.value = stateFlow.value.copy(
                        loading = false,
                        poweredOn = command.powerOn,
                        selectedMode = command.mode,
                        manualTemperature = command.manualTemperature,
                        automaticTemperatureDay = command.automaticTemperatureDay,
                        automaticTemperatureNight = command.automaticTemperatureNight,
                        automaticTemperatures = command.automaticTemperatures
                            .map {
                                val modes = it.map { isDay -> if (isDay) DayTime.Mode.DAY else DayTime.Mode.NIGHT }
                                DayTime(modes.toImmutableList())
                            }.toImmutableList(),
                    )
                }
        }
    }

    fun power(on: Boolean) {
        val roomId = roomId ?: return
        showLoader()
        viewModelScope.launch {
            CommandRepository.setPowered(roomId, on)
        }
    }

    fun selectMode(mode: Mode) {
        val roomId = roomId ?: return
        showLoader()
        viewModelScope.launch {
            CommandRepository.setMode(roomId, mode)
        }
    }

    fun setTemperature(temperature: Int) {
        if (temperature > MAX_TEMPERATURE || temperature < MIN_TEMPERATURE) return
        val roomId = roomId ?: return
        showLoader()
        viewModelScope.launch {
            CommandRepository.setManualTemperature(roomId, temperature)
        }
    }

    fun setDayTemperature(temperature: Int) {
        if (temperature > MAX_TEMPERATURE || temperature < MIN_TEMPERATURE) return
        val roomId = roomId ?: return
        showLoader()
        viewModelScope.launch {
            CommandRepository.setDayTemperature(roomId, temperature)
        }
    }

    fun setNightTemperature(temperature: Int) {
        if (temperature > MAX_TEMPERATURE || temperature < MIN_TEMPERATURE) return
        val roomId = roomId ?: return
        showLoader()
        viewModelScope.launch {
            CommandRepository.setNightTemperature(roomId, temperature)
        }
    }

    private fun showLoader() {
        _stateFlow.value = stateFlow.value.copy(loading = true)
    }

    private fun String.toDayTime(): String {
        var startIndex = 0
        var endIndex = indexOf('-')
        val day = substring(startIndex, endIndex).toInt()
        startIndex = endIndex + 1
        endIndex = indexOf('-', startIndex)
        val hours = substring(startIndex, endIndex).toInt()
        startIndex = endIndex + 1
        val minutes = substring(startIndex).toInt()
        val calendar = GregorianCalendar()
        calendar.set(GregorianCalendar.DAY_OF_WEEK, ((day + 1) % 7) + 1)
        calendar.set(GregorianCalendar.HOUR_OF_DAY, hours)
        calendar.set(GregorianCalendar.MINUTE, minutes)
        return DateFormat.format("EEEE, HH:mm", calendar.time).toString()
    }

    fun setDay(day: Int, hour: Int, isDay: Boolean) {
        val roomId = roomId ?: return
        showLoader()
        viewModelScope.launch {
            CommandRepository.setIsDay(roomId, day, hour, isDay)
        }
    }

    internal data class ThermostatSate(
        val loading: Boolean = true,
        val title: String = "",
        val currentTemperature: Float = 0f,
        val currentHumidity: Float = 0f,
        val currentlyOn: Boolean = false,
        val lastTimeUpdated: String = "",
        val poweredOn: Boolean = false,
        val availableModes: ImmutableList<Mode> = Mode.entries.toImmutableList(),
        val selectedMode: Mode = Mode.values().first(),
        val manualTemperature: Int = 19,
        val automaticTemperatureDay: Int = 19,
        val automaticTemperatureNight: Int = 16,
        val automaticTemperatures: ImmutableList<DayTime> = persistentListOf(),
    )

    companion object {
        private const val TAG = "ThermostatViewModel"

        private const val MIN_TEMPERATURE = 5
        private const val MAX_TEMPERATURE = 25
    }
}
