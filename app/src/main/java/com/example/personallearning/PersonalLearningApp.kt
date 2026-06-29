package com.example.personallearning

import android.app.Application
import com.example.personallearning.data.local.AppDatabase

class PersonalLearningApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
