package com.ninegag.move.kmp.ui

import MainPage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.ninegag.move.kmp.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun MoveApp(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    LaunchedEffect("init") {
        viewModel.loadUser()
        viewModel.mayCreateUserDoc()
        viewModel.loadStepCount()
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("9GAG Moves!")
                    }
                )
            }
        ) { paddingValues ->
            if (user != null) {
                MainPage(viewModel, paddingValues)
            } else {
                LandingPage(viewModel)
            }
        }
    }
}

@Composable
fun Header(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user != null) {
            Text("Hello, ${user.name}")
            if (!uiState.isHealthManagerAvailable) {
                Text("Sorry, this application is not supported on your device")
            }
            if (!uiState.isAuthorized) {
                Button(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            viewModel.requestAuthorization()
                        }
                    }
                ) {
                    Text("Authorize Health's access")
                }
            }

        } else {
            Button(
                onClick = {
                    viewModel.viewModelScope.launch {
                        viewModel.signIn()
                    }
                }
            ) {
                Text("Sign in")
            }
        }
    }
}
