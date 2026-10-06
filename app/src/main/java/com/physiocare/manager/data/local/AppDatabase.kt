package com.physiocare.manager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.physiocare.manager.data.local.dao.PatientDao
import com.physiocare.manager.data.local.dao.PaymentDao
import com.physiocare.manager.data.local.dao.SessionDao
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.local.entity.PaymentEntity
import com.physiocare.manager.data.local.entity.SessionEntity

@Database(
    entities = [PatientEntity::class, SessionEntity::class, PaymentEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun sessionDao(): SessionDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "physiocare_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
