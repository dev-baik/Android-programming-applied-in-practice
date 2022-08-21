package com.example.geoquiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.geoquiz.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private var messageResId = 0
    private lateinit var question: Question
    private lateinit var binding: ActivityMainBinding

    private val questionBank = listOf(
        Question(R.string.question_australia, true, false),
        Question(R.string.question_oceans, true, false),
        Question(R.string.question_mideast, false, false),
        Question(R.string.question_africa, false, false),
        Question(R.string.question_americas, true, false),
        Question(R.string.question_asia, true, false))

    private var currentIndex = 0
    private var answeredQuestionCount = 0
    private var correctAnswerCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.trueButton.setOnClickListener {
            var toast = Toast.makeText(this, R.string.correct_toast, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 200)
            toast.show()

            questionBank[currentIndex].answered = true
            answeredQuestionCount++
            checkAnswer(true)
       }

        binding.falseButton.setOnClickListener { view: View ->
            answeredQuestionCount++
            checkAnswer(false)
        }

        binding.questionTextView.setOnClickListener {
            currentIndex = (currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        binding.previousButton.setOnClickListener {
            currentIndex = (currentIndex - 1) % questionBank.size
            if (currentIndex == -1) currentIndex = questionBank.lastIndex
            isAnswered(currentIndex)
            updateQuestion()
        }

        binding.nextButton.setOnClickListener {
            currentIndex = (currentIndex + 1) % questionBank.size
            isAnswered(currentIndex)
            updateQuestion()
        }
        updateQuestion()
    }

    private fun updateQuestion() {
        Log.d(TAG, "Current question index: $currentIndex")

        try {
            question = questionBank[currentIndex]
        } catch (ex: ArrayIndexOutOfBoundsException) {
            Log.e(TAG, "Index was out of bounds", ex)
        }

        val questionTextResId = question.textResId
        binding.questionTextView.setText(questionTextResId)

    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = questionBank[currentIndex].answer

        if (userAnswer == correctAnswer) {
            messageResId = R.string.correct_toast
            correctAnswerCount++
        } else {
            messageResId = R.string.incorrect_toast
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()

        binding.trueButton.isEnabled = false
        binding.falseButton.isEnabled = false

        if (answeredQuestionCount == questionBank.size) {
            Toast.makeText(this, "${correctAnswerCount/questionBank.size.toDouble() * 100} %", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAnswered(index: Int) {
        if (questionBank[index].answered) {
            binding.trueButton.isEnabled = false
            binding.falseButton.isEnabled = false
        } else {
            binding.trueButton.isEnabled = true
            binding.falseButton.isEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }
}