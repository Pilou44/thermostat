package com.wechantloup.thermostat.ui.main

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStoreOwner
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
import com.wechantloup.thermostat.ui.thermostat.DaySettingsScreen
import com.wechantloup.thermostat.ui.thermostat.ThermostatScreen
import com.wechantloup.thermostat.ui.thermostat.ThermostatViewModel

internal const val ARG_ROOM_ID = "room_id"
internal const val ARG_DAY = "day"

// Screens
private const val AUTHENTICATION_SCREEN = "authentication_screen"
private const val ROOM_SELECTION_SCREEN = "room_selection_screen"
private const val THERMOSTAT_COMMAND_SCREEN = "thermostat_command_screen"
private const val THERMOSTAT_COMMAND_SCREEN_WITH_ARGS = "$THERMOSTAT_COMMAND_SCREEN/{$ARG_ROOM_ID}"
private const val THERMOSTAT_SETTINGS_SCREEN = "thermostat_settings_screen"
private const val THERMOSTAT_SETTINGS_SCREEN_WITH_ARGS = "$THERMOSTAT_SETTINGS_SCREEN/{$ARG_ROOM_ID}"
private const val THERMOSTAT_DAY_SETTINGS_SCREEN = "thermostat_day_settings_screen"
private const val THERMOSTAT_DAY_SETTINGS_SCREEN_WITH_ARGS = "$THERMOSTAT_DAY_SETTINGS_SCREEN/{$ARG_DAY}"

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
    Log.d("TEST", "Recompose NavigationHost")

    NavHost(navController = navController, startDestination = AUTHENTICATION_SCREEN, modifier) {

        composable(AUTHENTICATION_SCREEN) {
            val authenticationViewModel = getAuthenticationViewModel(owner = activity)
            AuthenticationScreen(
                viewModel = authenticationViewModel,
                signIn = signIn,
                signOut = signOut,
                next = { navController.navigate(ROOM_SELECTION_SCREEN) { popUpTo(0) } },
            )
        }

        composable(ROOM_SELECTION_SCREEN) {
            val roomSelectionViewModel = getRoomSelectionViewModel(owner = activity)
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
            val thermostatViewModel = getThermostatViewModel(owner = activity)
            thermostatViewModel.setRoomId(roomId)
            ThermostatScreen(
                viewModel = thermostatViewModel,
                goToSettings = { navController.navigate("$THERMOSTAT_SETTINGS_SCREEN/$roomId") },
                goToDayTimeSettings = { day -> navController.navigate("$THERMOSTAT_DAY_SETTINGS_SCREEN/$day")  }
            )
        }

        composable(
            THERMOSTAT_SETTINGS_SCREEN_WITH_ARGS,
            arguments = listOf(
                navArgument(ARG_ROOM_ID) { type = NavType.StringType },
            ),
        ) {
            val thermostatViewModel = getThermostatViewModel(owner = activity)
            val settingsViewModel = getSettingsViewModel(owner = activity)
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

        composable(
            THERMOSTAT_DAY_SETTINGS_SCREEN_WITH_ARGS,
            arguments = listOf(
                navArgument(ARG_DAY) { type = NavType.IntType },
            ),
        ) {
            val day = requireNotNull(it.arguments?.getInt(ARG_DAY))
            DaySettingsScreen(
                day = day,
                viewModel = getThermostatViewModel(owner = activity),
            )
        }
    }
}

@Composable
private fun getThermostatViewModel(owner: ViewModelStoreOwner) = viewModel<ThermostatViewModel>(
    viewModelStoreOwner = owner,
)

@Composable
private fun getSettingsViewModel(owner: ViewModelStoreOwner) = viewModel<SettingsViewModel>(
    viewModelStoreOwner = owner,
)

@Composable
private fun getAuthenticationViewModel(owner: ViewModelStoreOwner) = viewModel<AuthenticationViewModel>(
    viewModelStoreOwner = owner,
)

@Composable
private fun getRoomSelectionViewModel(owner: ViewModelStoreOwner) = viewModel<RoomSelectionViewModel>(
    viewModelStoreOwner = owner,
)
