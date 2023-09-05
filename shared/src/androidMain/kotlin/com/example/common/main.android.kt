package com.example.common

import androidx.compose.runtime.Composable
import com.example.common.App

actual fun getPlatformName(): String = "Android"

@Composable fun MainView() = App()
