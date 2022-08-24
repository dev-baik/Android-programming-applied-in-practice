package com.example.geoquiz

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Contacts.SettingsColumns.KEY
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.geoquiz.databinding.ActivityMainBinding

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val KEY_CHEAT = "cheat"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {
    private var messageResId = 0
    private lateinit var question: Question
    private lateinit var binding: ActivityMainBinding

    private var answeredQuestionCount = 0
    private var correctAnswerCount = 0

    private val quizViewModel: QuizViewModel by lazy { ViewModelProvider(this).get(QuizViewModel::class.java) }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val questionBank = quizViewModel.questionBank

        var currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0)?: 0
        quizViewModel.currentIndex = currentIndex

        var currentCheat = savedInstanceState?.getBoolean(KEY_CHEAT, false)?: false
        quizViewModel.isCheater = currentCheat

        val provider: ViewModelProvider = ViewModelProvider(this)
        val quizViewModel = provider.get(QuizViewModel::class.java)
        Log.d(TAG, "Got a QuizViewModel: $quizViewModel")

        binding.trueButton.setOnClickListener {
            var toast = Toast.makeText(this, R.string.correct_toast, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 200)
            toast.show()
            answeredQuestionCount++
            checkAnswer(true)
       }

        binding.falseButton.setOnClickListener { view: View ->
            answeredQuestionCount++
            checkAnswer(false)
        }

        binding.questionTextView.setOnClickListener {
            quizViewModel.currentIndex = (quizViewModel.currentIndex + 1) % questionBank.size
            updateQuestion()
        }

        binding.previousButton.setOnClickListener {
//            currentIndex = (currentIndex - 1) % questionBank.size
            quizViewModel.moveToPrevious()
            if (quizViewModel.currentIndex == -1) quizViewModel.currentIndex = questionBank.lastIndex
            isAnswered(quizViewModel.currentIndex)
//            quizViewModel.isCheater = false
            updateQuestion()
        }

        binding.nextButton.setOnClickListener {
//            currentIndex = (currentIndex + 1) % questionBank.size
            quizViewModel.moveToNext()
            isAnswered(quizViewModel.currentIndex)
//            quizViewModel.isCheater = false
            updateQuestion()
        }

        var cheatCount = 3

        binding.cheatButton.setOnClickListener {
            if (cheatCount <= 3 && cheatCount != 0) {
                cheatCount -= 1
                binding.cheatCount!!.text = cheatCount.toString()

                //            val intent = Intent(this, CheatActivity::class.java)
                val answerIsTrue = quizViewModel.currentQuestionAnswer
                val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val options = ActivityOptions.makeClipRevealAnimation(it, 0, 0, it.width, it.height)
                    startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
                } else {
                    startActivityForResult(intent, REQUEST_CODE_CHEAT)
                }
            }
            if (cheatCount == 0) {
                binding.cheatButton.isEnabled = false
            }
        }

        updateQuestion()

        quizViewModel.initCheatProtection()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
            if (quizViewModel.isCheater) {
                quizViewModel.setCheatedQuestion(quizViewModel.currentIndex)
            }
        }
    }

    private fun updateQuestion() {
//        Log.d(TAG, "Current question index: $currentIndex")

//        try {
//            question = quizViewModel.questionBank[quizViewModel.currentIndex]
//        } catch (ex: ArrayIndexOutOfBoundsException) {
//            Log.e(TAG, "Index was out of bounds", ex)
//        }

        val questionTextResId = quizViewModel.currentQuestionText
        binding.questionTextView.setText(questionTextResId)

    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer

//        if (userAnswer == correctAnswer) {
//            messageResId = R.string.correct_toast
//            quizViewModel.questionBank[quizViewModel.currentIndex].answered = true
//            correctAnswerCount++
//        } else {
//            quizViewModel.questionBank[quizViewModel.currentIndex].answered = true
//            messageResId = R.string.incorrect_toast
//        }

//        val messageResId = when {
//            quizViewModel.isCheater -> {
//                quizViewModel.questionBank[quizViewModel.currentIndex].answered = true
//                R.string.judgment_toast
//            }
//            userAnswer == correctAnswer -> {
//                quizViewModel.questionBank[quizViewModel.currentIndex].answered = true
//                correctAnswerCount++
//                R.string.correct_toast
//            }
//            else -> R.string.incorrect_toast
//        }

        val messageResId = when {
            quizViewModel.cheatedQuestions[quizViewModel.currentIndex] -> {
                quizViewModel.questionBank[quizViewModel.currentIndex].answered = true
                R.string.judgment_toast
            }
            userAnswer == correctAnswer -> {
                quizViewModel.questionBank[quizViewModel.currentIndex].answered = true
                correctAnswerCount++
                R.string.correct_toast
            }
            else -> R.string.incorrect_toast
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()

        binding.trueButton.isEnabled = false
        binding.falseButton.isEnabled = false

        if (answeredQuestionCount == quizViewModel.questionBank.size) {
            Toast.makeText(this, "${correctAnswerCount/quizViewModel.questionBank.size.toDouble() * 100} %", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAnswered(index: Int) {
        if (quizViewModel.questionBank[index].answered) {
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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Log.d(TAG, "onSaveInstanceState")
        savedInstanceState.putInt(KEY_INDEX, quizViewModel.currentIndex)
        savedInstanceState.putBoolean(KEY_CHEAT, quizViewModel.isCheater)
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