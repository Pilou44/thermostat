package com.wechantloup.thermostat.ui.authentication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wechantloup.thermostat.usecase.AuthenticationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class AuthenticationViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(AuthenticationSate())
    val stateFlow: StateFlow<AuthenticationSate> = _stateFlow

    private val authenticationUseCase: AuthenticationUseCase = AuthenticationUseCase()

    init {
        relaunch()
    }

    fun relaunch() {
        viewModelScope.launch {
            init()
        }
    }

    private suspend fun init() {
        _stateFlow.emit(stateFlow.value.copy(loading = true))

        val user = FirebaseAuth.getInstance().currentUser
        val isAuthenticated = user != null
        val isAllowed = user?.let { authenticationUseCase.isUserAllowed(user.uid) } ?: false

        _stateFlow.emit(
            stateFlow.value.copy(
                isAuthenticated = isAuthenticated,
                isAllowed = isAllowed,
                loading = false,
            )
        )
    }

    internal data class AuthenticationSate(
        val loading: Boolean = true,
        val isAuthenticated: Boolean = false,
        val isAllowed: Boolean = false,
    )

    companion object {
        const val TAG = "AuthenticationViewModel"
    }
}
