package com.wechantloup.thermostat.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wechantloup.thermostat.model.Command
import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.model.Status
import com.wechantloup.thermostat.usecase.AuthenticationUseCase
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

internal class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(MainSate())
    val stateFlow: StateFlow<MainSate> = _stateFlow

    private var thermostatUseCase: ThermostatUseCase? = null
    private val authenticationUseCase: AuthenticationUseCase = AuthenticationUseCase()

    init {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val isAuthenticated = user != null
            val isAllowed = user?.let { authenticationUseCase.isUserAllowed(user.uid) } ?: false

            _stateFlow.emit(stateFlow.value.copy(isAuthenticated = isAuthenticated, isAllowed = isAllowed))

            if (!isAllowed) {
                _stateFlow.emit(stateFlow.value.copy(loading = false))
                return@launch
            }

            initThermostat()
        }
    }

    fun relaunch() {
        viewModelScope.launch {
            init()
        }
    }

    private suspend fun init() {
        _stateFlow.emit(stateFlow.value.copy(loading = true))
        val user = FirebaseAuth.getInstance().currentUser
        val isAuthenticated = user != null
        val isAllowed = user?.let { authenticationUseCase.isUserAllowed(user.uid) } ?: false

        _stateFlow.emit(stateFlow.value.copy(isAuthenticated = isAuthenticated, isAllowed = isAllowed))

        if (!isAllowed) {
            _stateFlow.emit(stateFlow.value.copy(loading = false))
            return
        }

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

    internal data class MainSate(
        val loading: Boolean = true,
        val isAuthenticated: Boolean = false,
        val isAllowed: Boolean = false,
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


