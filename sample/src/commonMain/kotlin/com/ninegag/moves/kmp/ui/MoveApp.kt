package com.ninegag.moves.kmp.ui

import MainPage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.ninegag.moves.kmp.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveApp(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    var currentPage by remember { mutableStateOf("landingPage") }

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
                MainPage(viewModel, paddingValues, onNavigateToLandingPage = { currentPage = "landingPage" })
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
