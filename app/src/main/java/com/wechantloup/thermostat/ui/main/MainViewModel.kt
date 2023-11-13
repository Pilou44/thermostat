package com.wechantloup.thermostat.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(MainSate())
    val stateFlow: StateFlow<MainSate> = _stateFlow

    fun power(on: Boolean) {
        // ToDo
        viewModelScope.launch {
            _stateFlow.emit(stateFlow.value.copy(poweredOn = on))
        }
    }
}

internal data class MainSate(
    val poweredOn: Boolean = false,
)
