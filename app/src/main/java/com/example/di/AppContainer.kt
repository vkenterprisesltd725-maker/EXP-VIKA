package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.ExpVikaRepository

interface AppContainer {
    val expVikaRepository: ExpVikaRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "expvika_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    override val expVikaRepository: ExpVikaRepository by lazy {
        ExpVikaRepository(database.accountDao(), database.transactionDao(), database.budgetDao())
    }
}
