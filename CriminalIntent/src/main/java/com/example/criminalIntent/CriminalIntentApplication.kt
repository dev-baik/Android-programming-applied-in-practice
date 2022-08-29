package com.example.criminalIntent

import android.app.Application
import com.example.criminalIntent.database.CrimeRepository

class CriminalIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}