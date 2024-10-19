package com.ninegag.move.kmp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.ninegag.move.kmp.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun MoveApp(
    viewModel: MainViewModel = viewModel { MainViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    LaunchedEffect("init") {
        viewModel.mayCreateUserDoc()
    }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
    }
}

