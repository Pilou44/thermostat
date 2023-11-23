package com.wechantloup.thermostat.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wechantloup.thermostat.ui.authentication.AuthenticationScreen
import com.wechantloup.thermostat.ui.authentication.AuthenticationViewModel
import com.wechantloup.thermostat.ui.roomselection.RoomSelectionScreen
import com.wechantloup.thermostat.ui.roomselection.RoomSelectionViewModel
import com.wechantloup.thermostat.ui.thermostat.ThermostatScreen
import com.wechantloup.thermostat.ui.thermostat.ThermostatViewModel

// Screens
private const val AUTHENTICATION_SCREEN = "authentication_screen"
private const val ROOM_SELECTION_SCREEN = "room_selection_screen"
private const val THERMOSTAT_SCREEN = "thermostat_screen"

@Composable
internal fun NavigationHost(
    activity: MainActivity,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavigationHost(
        activity = activity,
        signIn = activity::createSignInIntent,
        signOut = activity::signOut,
        navController = navController,
        modifier = modifier,
    )
}

@Composable
internal fun NavigationHost(
    activity: ComponentActivity,
    signIn: () -> Unit,
    signOut: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = AUTHENTICATION_SCREEN, modifier) {

        composable(AUTHENTICATION_SCREEN) {
            val authenticationViewModel = viewModel<AuthenticationViewModel>(
                viewModelStoreOwner = activity,
                key = AuthenticationViewModel.TAG,
            )
            AuthenticationScreen(
                viewModel = authenticationViewModel,
                signIn = signIn,
                signOut = signOut,
                next = { navController.navigate(ROOM_SELECTION_SCREEN) },
            )
        }

        composable(ROOM_SELECTION_SCREEN) {
            val roomSelectionViewModel = viewModel<RoomSelectionViewModel>(
                viewModelStoreOwner = activity,
                key = RoomSelectionViewModel.TAG,
            )
            RoomSelectionScreen(
                viewModel = roomSelectionViewModel,
                onRoomSelected = { navController.navigate(THERMOSTAT_SCREEN) },
            )
        }

        composable(THERMOSTAT_SCREEN) {
            val thermostatViewModel = viewModel<ThermostatViewModel>(
                viewModelStoreOwner = activity,
                key = ThermostatViewModel.TAG,
            )
            ThermostatScreen(
                viewModel = thermostatViewModel,
            )
        }
    }
}
