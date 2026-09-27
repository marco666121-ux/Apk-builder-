package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BuildHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildHistoryDao {
    @Query("SELECT * FROM build_history ORDER BY buildDate DESC")
    fun getAllBuilds(): Flow<List<BuildHistoryItem>>

    @Query("SELECT * FROM build_history WHERE projectId = :projectId ORDER BY buildDate DESC")
    fun getBuildsForProject(projectId: Long): Flow<List<BuildHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(item: BuildHistoryItem): Long

    @Delete
    suspend fun deleteBuild(item: BuildHistoryItem)

    @Query("DELETE FROM build_history WHERE id = :id")
    suspend fun deleteBuildById(id: Long)
}
