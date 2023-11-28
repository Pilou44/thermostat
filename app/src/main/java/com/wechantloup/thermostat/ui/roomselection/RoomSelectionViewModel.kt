package com.wechantloup.thermostat.ui.roomselection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wechantloup.thermostat.usecase.GetAllRoomsUseCase
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

    private val getAllRoomsUseCase = GetAllRoomsUseCase()

    init {
        viewModelScope.launch {
            val availableRooms = getAllRoomsUseCase.execute()
            _stateFlow.emit(stateFlow.value.copy(
                loading = false,
                availableRooms = availableRooms.map { it.id }.toImmutableList(),
            ))
        }
    }
    internal data class RoomSelectionSate(
        val loading: Boolean = true,
        val availableRooms: ImmutableList<String> = persistentListOf(),
    )

    companion object {
        const val TAG = "RoomSelectionViewModel"
    }
}
