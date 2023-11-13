package com.wechantloup.thermostat.ui.main

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.wechantloup.thermostat.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val database = Firebase.database("https://thermostat-4211f-default-rtdb.europe-west1.firebasedatabase.app/")

    private val _stateFlow = MutableStateFlow(MainSate())
    val stateFlow: StateFlow<MainSate> = _stateFlow

    fun power(on: Boolean) {
        // ToDo
        testDb()
        viewModelScope.launch {
            _stateFlow.emit(stateFlow.value.copy(poweredOn = on))
        }
    }

    fun selectMode(mode: Mode) {
        // ToDo
        viewModelScope.launch {
            _stateFlow.emit(stateFlow.value.copy(selectedMode = mode))
        }
    }

    fun setTemperature(temperature: Int) {
        if (temperature > MAX_TEMPERATURE || temperature < MIN_TEMPERATURE) return

        // ToDo

        viewModelScope.launch {
            _stateFlow.emit(stateFlow.value.copy(manualTemperature = temperature))
        }
    }

    private fun testDb() {
        val myRef = database.getReference("test")
        Log.i("TEST", "Write to db")
        myRef.setValue("Hello, World!")
    }

    internal data class MainSate(
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

internal enum class Mode(val id: String, @StringRes val labelRes: Int) {
    MANUAL("manual", R.string.manual_mode_label),
    AUTO("auto", R.string.auto_mode_label),
}
