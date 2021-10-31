package com.dubblej.simon

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.collections.lastIndex

class Game : ViewModel() {

    val pattern = mutableListOf<Int>()

    private var userIndex = 0

    private val _difficulty = MutableLiveData(2)
    val difficulty: LiveData<Int> = _difficulty

    private val _userScore = MutableLiveData<Int>()
    val userScore: LiveData<Int> = _userScore

    private val _acceptingUserInput = MutableLiveData(false)
    val acceptingUserInput : LiveData<Boolean> = _acceptingUserInput

    private val _gameOver = MutableLiveData(false)
    val gameOver : LiveData<Boolean> = _gameOver

    private val _patternComplete = MutableLiveData(false)
    val patternComplete : LiveData<Boolean> = _patternComplete

    private val _secondsTilGameStart = MutableLiveData<Int>()
    val secondsTilGameStart : LiveData<Int> = _secondsTilGameStart

    private val _gameStarted = MutableLiveData(false)
    val gameStarted: LiveData<Boolean> = _gameStarted

    private val timer = object : CountDownTimer(5100, 1000) {

        override fun onTick(millisUntilFinished: Long) {
            _secondsTilGameStart.value = (millisUntilFinished / 1000).toInt() % 60
        }

        override fun onFinish() {
            _gameStarted.value = true
        }
    }

    init {
        startNewGame()
    }

    private fun addToPattern() {
        pattern.add((0..3).random())
        userIndex = 0
        _patternComplete.value = false
    }

    fun checkUserInput(userInput: Int)   {

        if (userInput != pattern[userIndex]) {
            _gameOver.value = true
            return
        }

        if(_gameOver.value == false && userIndex == pattern.lastIndex){
            _userScore.value = _userScore.value?.plus(1)
            _patternComplete.value = true
            _acceptingUserInput.value = false
            addToPattern()
        }

        else
            userIndex++

    }

    fun isPatternCompleteShowing (index: Int) {
        if(index == pattern.lastIndex)
            _acceptingUserInput.postValue(true)
    }

    fun startNewGame() {

        timer.cancel()
        _acceptingUserInput.value = false
        _userScore.value = 0
        _gameOver.value = false
        _gameStarted.value = false
        pattern.clear()
        addToPattern()
        timer.start()

    }

    fun setDifficulty(difficulty: Int) {
        _difficulty.value = difficulty
    }

    fun setGameStarted() {
        _gameStarted.value = false
    }
}