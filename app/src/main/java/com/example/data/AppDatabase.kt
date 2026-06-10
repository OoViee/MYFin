package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DailyExpenseEntity::class,
        CreditExpenseEntity::class,
        EmiLoanEntity::class,
        DebtSplitEntity::class,
        IncomePaydayEntity::class,
        SipEntity::class,
        InvestmentEntity::class,
        CreditCardEntity::class,
        TripEventEntity::class,
        TripExpenseEntity::class,
        ParticipantEntity::class,
        BudgetEntity::class,
        CardStatementEntity::class,
        CardEMIEntity::class,
        CardPaymentEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): WealthPulseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wealthpulse_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
