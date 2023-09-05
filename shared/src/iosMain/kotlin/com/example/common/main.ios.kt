package com.example.common

import androidx.compose.ui.window.ComposeUIViewController
import com.example.common.App

actual fun getPlatformName(): String = "iOS"

fun MainViewController() = ComposeUIViewController { App() }