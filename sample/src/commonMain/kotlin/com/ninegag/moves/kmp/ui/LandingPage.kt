package com.ninegag.moves.kmp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.ninegag.moves.kmp.Constants
import com.ninegag.moves.kmp.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun LandingPage(
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = Constants.APP_IMAGE,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
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