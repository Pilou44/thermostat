package com.wechantloup.thermostat.ui.roomselection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wechantloup.thermostat.model.Device
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class RoomSelectionViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(RoomSelectionSate())
    val stateFlow: StateFlow<RoomSelectionSate> = _stateFlow

    init {
        viewModelScope.launch {
            val availableRooms = Device.getAll()
            _stateFlow.emit(stateFlow.value.copy(
                loading = false,
                availableRooms = availableRooms.toImmutableList(),
            ))
        }
    }
    internal data class RoomSelectionSate(
        val loading: Boolean = true,
        val availableRooms: ImmutableList<Device> = persistentListOf(),
    )

    companion object {
        private const val TAG = "RoomSelectionViewModel"
    }
}
