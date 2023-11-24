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
import com.wechantloup.thermostat.ui.settings.SettingsScreen
import com.wechantloup.thermostat.ui.settings.SettingsViewModel
import com.wechantloup.thermostat.ui.thermostat.ThermostatScreen
import com.wechantloup.thermostat.ui.thermostat.ThermostatViewModel

internal const val ARG_ROOM_ID = "room_id"

// Screens
private const val AUTHENTICATION_SCREEN = "authentication_screen"
private const val ROOM_SELECTION_SCREEN = "room_selection_screen"
private const val THERMOSTAT_COMMAND_SCREEN = "thermostat_command_screen"
internal const val THERMOSTAT_COMMAND_SCREEN_WITH_ARGS = "$THERMOSTAT_COMMAND_SCREEN/{$ARG_ROOM_ID}"
private const val THERMOSTAT_SETTINGS_SCREEN = "thermostat_settings_screen"
internal const val THERMOSTAT_SETTINGS_SCREEN_WITH_ARGS = "$THERMOSTAT_SETTINGS_SCREEN/{$ARG_ROOM_ID}"

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
    val thermostatViewModel = viewModel<ThermostatViewModel>(
        viewModelStoreOwner = activity,
        key = ThermostatViewModel.TAG,
    )
    val settingsViewModel = viewModel<SettingsViewModel>(
        viewModelStoreOwner = activity,
        key = SettingsViewModel.TAG,
    )
    val authenticationViewModel = viewModel<AuthenticationViewModel>(
        viewModelStoreOwner = activity,
        key = AuthenticationViewModel.TAG,
    )
    val roomSelectionViewModel = viewModel<RoomSelectionViewModel>(
        viewModelStoreOwner = activity,
        key = RoomSelectionViewModel.TAG,
    )

    NavHost(navController = navController, startDestination = AUTHENTICATION_SCREEN, modifier) {

        composable(AUTHENTICATION_SCREEN) {
            AuthenticationScreen(
                viewModel = authenticationViewModel,
                signIn = signIn,
                signOut = signOut,
                next = { navController.navigate(ROOM_SELECTION_SCREEN) { popUpTo(0) } },
            )
        }

        composable(ROOM_SELECTION_SCREEN) {
            RoomSelectionScreen(
                viewModel = roomSelectionViewModel,
                onRoomSelected = { roomId -> navController.navigate("$THERMOSTAT_COMMAND_SCREEN/$roomId") },
            )
        }

        composable(
            THERMOSTAT_COMMAND_SCREEN_WITH_ARGS,
            arguments = listOf(
                navArgument(ARG_ROOM_ID) { type = NavType.StringType },
            ),
        ) {
            val roomId = requireNotNull(it.arguments?.getString(ARG_ROOM_ID))
            thermostatViewModel.setRoomId(roomId)
            ThermostatScreen(
                viewModel = thermostatViewModel,
                goToSettings = { navController.navigate("$THERMOSTAT_SETTINGS_SCREEN/$roomId") },
            )
        }

        composable(
            THERMOSTAT_SETTINGS_SCREEN_WITH_ARGS,
            arguments = listOf(
                navArgument(ARG_ROOM_ID) { type = NavType.StringType },
            ),
        ) {
            val roomId = requireNotNull(it.arguments?.getString(ARG_ROOM_ID))
            settingsViewModel.reload(roomId)
            SettingsScreen(
                viewModel = settingsViewModel,
                goBack = {
                    thermostatViewModel.setRoomId(roomId)
                    navController.popBackStack()
                },
            )
        }
    }
}
