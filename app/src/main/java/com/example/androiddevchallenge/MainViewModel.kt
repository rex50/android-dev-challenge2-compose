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

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _hours = MutableLiveData(0L)
    private val _minutes = MutableLiveData(0L)
    private val _seconds = MutableLiveData(0L)

    private val _isTimerRunning = MutableLiveData(false)

    private var timer: CountDownTimer? = null

    private var lastHour: Long? = 0L
    private var lastMin: Long? = 0L
    private var lastSec: Long? = 0L

    val hours: LiveData<Long> = _hours
    val minutes: LiveData<Long> = _minutes
    val seconds: LiveData<Long> = _seconds

    val isTimerRunning: LiveData<Boolean> = _isTimerRunning

    fun onHourChanged(hours: Long) {
        _hours.value = if (hours > 24) 24 else hours
    }

    fun onMinChanged(min: Long) {
        _minutes.value = if (min > 59) 59 else min
    }

    fun onSecChanged(sec: Long) {
        _seconds.value = if (sec > 59) 59 else sec
    }

    private fun startTimer(sec: Long, onTimerFinished: () -> Unit) {
        _isTimerRunning.value = true
        timer = object : CountDownTimer(sec * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _hours.value = (millisUntilFinished / 3600000) % 24
                _minutes.value = (millisUntilFinished / 60000) % 60
                _seconds.value = (millisUntilFinished / 1000) % 60
            }

            override fun onFinish() {
                _isTimerRunning.value = false
                restoreLastTime()
                onTimerFinished()
            }
        }

        timer?.start()
    }

    private fun saveCurrentTime() {
        lastHour = hours.value
        lastMin = minutes.value
        lastSec = seconds.value
    }

    private fun restoreLastTime() {
        _hours.value = lastHour
        _minutes.value = lastMin
        _seconds.value = lastSec
    }

    fun startTimer(start: Boolean, onTimerFinished: () -> Unit) {
        if (start)
            startTimer { onTimerFinished() }
        else
            stopTimer()
    }

    private fun startTimer(onTimerFinished: () -> Unit) {
        saveCurrentTime()
        val finalSeconds = (seconds.value ?: 0L) + ((minutes.value ?: 0L) * 60) + ((hours.value ?: 0L) * 60 * 60)
        startTimer(finalSeconds) {
            onTimerFinished()
        }
    }

    private fun stopTimer() {
        _isTimerRunning.value = false
        timer?.cancel()
        timer = null
        restoreLastTime()
    }
}
