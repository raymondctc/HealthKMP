package com.ninegag.move.kmp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.ninegag.move.kmp.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun MoveApp(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect("init") {
        viewModel.loadUser()
        viewModel.mayCreateUserDoc()
        viewModel.loadStepCount()
    }

    MaterialTheme {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            item {
                Header(viewModel)
            }
            uiState.stepsRecord.forEach { (date, count) ->
                item {
                    Text(text = "Date=$date, steps count= $count")
                }
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
