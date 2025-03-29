package com.example.usageinsight.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.usageinsight.data.AppDatabase
import com.example.usageinsight.data.dao.UnlockDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建新表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `fitness_data` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `steps` INTEGER NOT NULL,
                    `distance` REAL NOT NULL,
                    `heartRate` REAL,
                    `calories` REAL,
                    `timestamp` INTEGER NOT NULL
                )
            """)
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }

    @Provides
    @Singleton
    fun provideUnlockDao(database: AppDatabase): UnlockDao {
        return database.unlockDao()
    }
} 