package com.wechantloup.thermostat.ui.roomselection

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wechantloup.thermostat.R
import com.wechantloup.thermostat.ui.compose.Loader
import com.wechantloup.thermostat.ui.compose.TopAppBar

@Composable
internal fun RoomSelectionScreen(
    viewModel: RoomSelectionViewModel,
    onRoomSelected: (String) -> Unit,
) {
    val state by viewModel.stateFlow.collectAsState()

    RoomSelectionScreen(
        isLoading = state.loading,
        availableRooms = state.availableRooms,
        onRoomSelected = onRoomSelected,
    )
}

@Composable
private fun RoomSelectionScreen(
    isLoading: Boolean,
    availableRooms: List<String>,
    onRoomSelected: (String) -> Unit,
) {

    Scaffold(
        topBar = {
            TopAppBar(text = stringResource(id = R.string.app_name))
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        ) {
            items(
                count = availableRooms.size,
            ) { index ->
                Room(availableRooms[index], onRoomSelected)
            }
        }
        Loader(
            isVisible = isLoading,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Room(
    room: String,
    onRoomSelected: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onRoomSelected(room) },
    ) {
        Text(room)
    }
}
