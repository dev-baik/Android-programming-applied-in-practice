package com.example.geoquiz

import android.util.Log
import androidx.lifecycle.ViewModel

private const val TAG = "QuizViewModel"

class QuizViewModel : ViewModel() {

//    init {
//        Log.d(TAG, "ViewModel instance created")
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        Log.d(TAG, "ViewModel instance about to be destroyed")
//    }

    val questionBank = listOf(
        Question(R.string.question_australia, true, false),
        Question(R.string.question_oceans, true, false),
        Question(R.string.question_mideast, false, false),
        Question(R.string.question_africa, false, false),
        Question(R.string.question_americas, true, false),
        Question(R.string.question_asia, true, false))

    var currentIndex = 0

    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer

    val currentQuestionText: Int
        get() = questionBank[currentIndex].textResId

    fun moveToPrevious() {
        currentIndex = (currentIndex - 1) % questionBank.size
    }

    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionBank.size
    }

    var isCheater = false

    var cheatedQuestions = mutableListOf<Boolean>()

    fun initCheatProtection() {
        for (i in questionBank.indices) {
            cheatedQuestions.add(false)
        }
    }

    fun setCheatedQuestion(questionIndex: Int) {
        cheatedQuestions[questionIndex] = true
    }
}