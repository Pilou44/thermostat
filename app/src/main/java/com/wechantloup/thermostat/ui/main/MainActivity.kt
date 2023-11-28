package com.wechantloup.thermostat.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.wechantloup.thermostat.ui.authentication.AuthenticationViewModel
import com.wechantloup.thermostat.ui.roomselection.RoomSelectionViewModel
import com.wechantloup.thermostat.ui.theme.ThermostatTheme
import com.wechantloup.thermostat.ui.thermostat.ThermostatViewModel

class MainActivity : ComponentActivity() {

    // See: https://developer.android.com/training/basics/intents/result
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private val authenticationViewModel by viewModels<AuthenticationViewModel>()
    private val roomSelectionViewModel by viewModels<RoomSelectionViewModel>()
    private val thermostatViewModel by viewModels<ThermostatViewModel>()

    private val navController by lazy {
        NavHostController(this).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThermostatTheme {
                NavigationHost(
                    activity = this,
                    navController = navController,
                )
            }
        }
    }

    fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                authenticationViewModel.relaunch()
            }
    }

    fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
        // [END auth_fui_create_intent]
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Log.i("AUTH", "User id = ${user?.displayName}")
            Log.i("AUTH", "User id = ${user?.uid}")
            authenticationViewModel.relaunch()
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

}
