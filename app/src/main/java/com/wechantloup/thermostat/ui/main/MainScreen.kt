package com.wechantloup.thermostat.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.wechantloup.thermostat.ui.authentication.AuthenticationScreen
import com.wechantloup.thermostat.ui.authentication.AuthenticationViewModel
import com.wechantloup.thermostat.ui.roomselection.RoomSelectionScreen
import com.wechantloup.thermostat.ui.roomselection.RoomSelectionViewModel
import com.wechantloup.thermostat.ui.thermostat.ThermostatScreen
import com.wechantloup.thermostat.ui.thermostat.ThermostatViewModel

internal const val ARG_ROOM_ID = "room_id"

// Screens
private const val AUTHENTICATION_SCREEN = "authentication_screen"
private const val ROOM_SELECTION_SCREEN = "room_selection_screen"
private const val THERMOSTAT_SCREEN = "thermostat_screen"
internal const val THERMOSTAT_SCREEN_WITH_ARGS = "$THERMOSTAT_SCREEN/{$ARG_ROOM_ID}"

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
                onRoomSelected = { roomId -> navController.navigate("$THERMOSTAT_SCREEN/$roomId") },
            )
        }

        composable(
            THERMOSTAT_SCREEN_WITH_ARGS,
            arguments = listOf(
                navArgument(ARG_ROOM_ID) { type = NavType.StringType },
            ),
        ) {
            val roomId = requireNotNull(it.arguments?.getString(ARG_ROOM_ID))
            val thermostatViewModel = viewModel<ThermostatViewModel>(
                viewModelStoreOwner = activity,
                key = ThermostatViewModel.TAG,
            )
            thermostatViewModel.setRoomId(roomId)
            ThermostatScreen(
                viewModel = thermostatViewModel,
            )
        }
    }
}
