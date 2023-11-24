package com.wechantloup.thermostat.ui.authentication

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wechantloup.thermostat.R
import com.wechantloup.thermostat.ui.compose.Loader
import com.wechantloup.thermostat.ui.compose.TopAppBar

@Composable
internal fun AuthenticationScreen(
    viewModel: AuthenticationViewModel,
    signIn: () -> Unit,
    signOut: () -> Unit,
    next: () -> Unit,
) {
    val state by viewModel.stateFlow.collectAsState()

    AuthenticationScreen(
        isLoading = state.loading,
        isAuthenticated = state.isAuthenticated,
        isAllowed = state.isAllowed,
        signIn = signIn,
        signOut = signOut,
        next = next,
    )
}

@Composable
private fun AuthenticationScreen(
    isLoading: Boolean,
    isAuthenticated: Boolean,
    isAllowed: Boolean,
    signIn: () -> Unit,
    signOut: () -> Unit,
    next: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(text = stringResource(id = R.string.app_name))
        },
    ) {
        val contentModifie = Modifier
            .padding(it)
            .fillMaxSize()
        when {
            !isAuthenticated -> AuthenticateContent(signIn, contentModifie)
            !isAllowed -> NotAllowedContent(signOut, contentModifie)
            else -> next()
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

@Composable
private fun AuthenticateContent(
    signIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Button(onClick = signIn) {
            Text(text = stringResource(id = R.string.sign_in_button_label))
        }
    }
}

@Composable
private fun NotAllowedContent(
    signOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(id = R.string.not_allowed_message))
            Button(onClick = signOut) {
                Text(text = stringResource(id = R.string.sign_out_button_label))
            }
        }
    }
}
