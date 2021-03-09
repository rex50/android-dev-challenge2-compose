/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.androiddevchallenge.ui.theme.MyTheme
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setContent {
            MyTheme {
                TimerApp(mainViewModel = mainViewModel)
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun TimerApp(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val owner: LifecycleOwner = LocalContext.current as LifecycleOwner
    Surface(color = MaterialTheme.colors.background) {

        val h = remember { mutableStateOf(0L) }
        val m = remember { mutableStateOf(0L) }
        val s = remember { mutableStateOf(0L) }
        val timerRunning = remember { mutableStateOf(false) }

        mainViewModel.hours.observe(owner, { h.value = it ?: 0L })
        mainViewModel.minutes.observe(owner, { m.value = it ?: 0L })
        mainViewModel.seconds.observe(owner, { s.value = it ?: 0L })
        mainViewModel.isTimerRunning.observe(owner, { timerRunning.value = it ?: false })

        Column {

            Text(
                text = "Timer",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            AnimatedVisibility(!timerRunning.value) {
                TimeInputUI(
                    modifier = Modifier.padding(16.dp),
                    hour = h.value,
                    min = m.value,
                    sec = s.value,
                    onHourChanged = {
                        mainViewModel.onHourChanged(it.toLongOrNull() ?: 0L)
                    },
                    onMinChanged = {
                        mainViewModel.onMinChanged(it.toLongOrNull() ?: 0L)
                    },
                    onSecChanged = {
                        mainViewModel.onSecChanged(it.toLongOrNull() ?: 0L)
                    }
                )
            }

            AnimatedVisibility(visible = h.value != 0L || m.value != 0L || s.value != 0L) {
                TimerButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    isTimerRunning = timerRunning.value,
                    onButtonClicked = {
                        mainViewModel.startTimer(!timerRunning.value) {
                            Toast.makeText(context, "Timer completed!!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            CountDownTimerUI(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                hours = h.value,
                minutes = m.value,
                seconds = s.value,
                isTimerRunning = timerRunning.value
            )
        }
    }
}

@Composable
fun TimeInputUI(
    modifier: Modifier = Modifier,
    hour: Long,
    min: Long,
    sec: Long,
    onHourChanged: (String) -> Unit,
    onMinChanged: (String) -> Unit,
    onSecChanged: (String) -> Unit
) {

    Row(
        modifier = modifier
    ) {

        CustomField(
            modifier = Modifier.weight(1f),
            value = hour,
            label = "Hours",
            onTextChanged = { onHourChanged(it) }
        )

        CustomField(
            modifier = Modifier.weight(1f),
            value = min,
            label = "Minutes",
            onTextChanged = { onMinChanged(it) }
        )

        CustomField(
            modifier = Modifier.weight(1f),
            value = sec,
            label = "Seconds",
            onTextChanged = { onSecChanged(it) }
        )
    }
}

@Composable
fun CustomField(
    modifier: Modifier = Modifier,
    value: Long,
    label: String = "",
    onTextChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = if (value == 0L) "" else value.toString(),
        onValueChange = { onTextChanged(it) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .padding(6.dp)
    )
}

@Composable
fun TimerButton(
    modifier: Modifier = Modifier,
    isTimerRunning: Boolean,
    onButtonClicked: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = { onButtonClicked() }
    ) {
        Text(
            modifier = Modifier.padding(4.dp),
            text = if (isTimerRunning) "Stop" else "Start",
            style = MaterialTheme.typography.button
        )
    }
}

@Composable
fun CountDownTimerUI(
    modifier: Modifier = Modifier,
    hours: Long,
    minutes: Long,
    seconds: Long,
    isTimerRunning: Boolean = false
) {
    Text(
        modifier = modifier,
        text = "${formatLong(hours)}:${formatLong(minutes)}:${formatLong(seconds)}",
        style = MaterialTheme.typography.h2.copy(
            color = if (hours == 0L && minutes == 0L && seconds != 0L && seconds < 10 && isTimerRunning) {
                Color.Red
            } else MaterialTheme.colors.primary
        ),
        textAlign = TextAlign.Center
    )
}

private fun formatLong(value: Long): String {
    return /*if(value > 0)*/ DecimalFormat("00").format(value) /*else ""*/
}

@ExperimentalAnimationApi
@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        TimerApp(mainViewModel = MainViewModel())
    }
}

@ExperimentalAnimationApi
@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        TimerApp(mainViewModel = MainViewModel())
    }
}
