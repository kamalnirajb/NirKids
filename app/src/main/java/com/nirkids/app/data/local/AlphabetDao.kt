package com.nirkids.app.data.local

import androidx.room.*
import com.nirkids.app.data.model.AlphabetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlphabetDao {
    @Query("SELECT * FROM alphabet ORDER BY letter ASC")
    fun getAllAlphabets(): Flow<List<AlphabetEntity>>

    @Query("SELECT * FROM alphabet WHERE letter = :letter")
    suspend fun getAlphabetByLetter(letter: String): AlphabetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlphabet(entity: AlphabetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlphabets(alphabets: List<AlphabetEntity>)

    @Query("DELETE FROM alphabet")
    suspend fun deleteAllAlphabets()
}
