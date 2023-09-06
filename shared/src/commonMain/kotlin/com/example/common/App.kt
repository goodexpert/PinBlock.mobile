package com.example.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    MaterialTheme {
        val kc = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val panFocus = remember { FocusRequester() }
        val pinFocus = remember { FocusRequester() }
        var panNumber by remember { mutableStateOf("43219876543210987") }
        var pinNumber by remember { mutableStateOf("1234") }
        var decodedNumber by remember { mutableStateOf("") }
        var encodedNumber by remember { mutableStateOf("") }

        val generate: () -> Unit = {
            if (panNumber.isBlank()) {
                panFocus.requestFocus()
            }

            if (pinNumber.isBlank()) {
                pinFocus.requestFocus()
            }

            if (panNumber.isNotBlank() && pinNumber.isNotBlank()) {
                encodedNumber = PinBlock.encode(PinBlock.Format.ISO_3, panNumber, pinNumber).uppercase()
                decodedNumber = PinBlock.decode(PinBlock.Format.ISO_3, panNumber, encodedNumber).uppercase()
                kc?.hide()
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ISO-3 Format 3", modifier = Modifier.padding(vertical = 32.dp), style = MaterialTheme.typography.h4)

                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PAN", modifier = Modifier.padding(8.dp))

                    TextField(
                        panNumber,
                        onValueChange = {
                            panNumber = it
                        },
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        ),
                        modifier = Modifier.focusRequester(panFocus)
                    )
                }

                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PIN", modifier = Modifier.padding(8.dp))

                    TextField(
                        pinNumber,
                        onValueChange = {
                            pinNumber = it
                        },
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                generate()
                            }
                        ),
                        modifier = Modifier.focusRequester(pinFocus)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ENCODED", modifier = Modifier.padding(8.dp))

                    Text("$encodedNumber", modifier = Modifier.padding(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DECODED", modifier = Modifier.padding(8.dp))

                    Text("$decodedNumber", modifier = Modifier.padding(8.dp))
                }

                Button(
                    onClick = {
                        generate()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Generate PIN Block")
                }
            }
        }
    }
}

expect fun getPlatformName(): String