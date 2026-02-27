package com.avidlearner.rem.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes ORDER BY queuePosition ASC")
    fun getAllQuotes(): Flow<List<Quote>>

    @Query("SELECT * FROM quotes ORDER BY queuePosition ASC")
    suspend fun getAllQuotesSync(): List<Quote>

    @Query("SELECT * FROM quotes ORDER BY queuePosition ASC LIMIT 1")
    suspend fun getFirstInQueue(): Quote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote)

    @Update
    suspend fun updateQuote(quote: Quote)

    @Delete
    suspend fun deleteQuote(quote: Quote)

    @Query("UPDATE quotes SET queuePosition = :newPosition WHERE id = :quoteId")
    suspend fun updateQueuePosition(quoteId: Int, newPosition: Long)
}
