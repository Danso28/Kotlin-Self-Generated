package com.example.cisco.kotlincrudlab.controller

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class ThisApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}