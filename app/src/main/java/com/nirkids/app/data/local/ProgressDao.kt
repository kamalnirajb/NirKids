package com.nirkids.app.data.local

import androidx.room.*
import com.nirkids.app.data.model.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress ORDER BY letter ASC")
    fun getAllProgress(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress WHERE letter = :letter")
    suspend fun getProgressForLetter(letter: String): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Query("UPDATE progress SET learned = :learned, attempts = attempts + 1, masteryLevel = masteryLevel + CASE WHEN :learned = 1 THEN 1 ELSE 0 END WHERE letter = :letter")
    suspend fun updateProgress(letter: String, learned: Boolean)

    @Query("UPDATE progress SET masteryLevel = :level WHERE letter = :letter")
    suspend fun updateMasteryLevel(letter: String, level: Int)

    @Query("DELETE FROM progress WHERE letter = :letter")
    suspend fun deleteProgress(letter: String)
}
