package com.example.crimefragment

import androidx.lifecycle.ViewModel

class CrimeListViewModel : ViewModel(){
    val crimes = mutableListOf<Crime>()

    init {
        for (i in 0 until 100) {
            val crime = Crime()
            crime.title = "Crime #$i"
            crime.isSolved = i % 2 == 0
            crime.requiresPolice = when ((0..1).shuffled().first()) {
                0 -> false
                else -> true
            }
            crime.isSolved = when (i % 2 == 1) {
                true -> true
                else -> false
            }
            crimes += crime
        }
    }
}