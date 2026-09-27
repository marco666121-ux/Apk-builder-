package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApkProject
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM apk_projects ORDER BY updatedDate DESC")
    fun getAllProjects(): Flow<List<ApkProject>>

    @Query("SELECT * FROM apk_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ApkProject?

    @Query("SELECT * FROM apk_projects WHERE id = :id")
    fun observeProjectById(id: Long): Flow<ApkProject?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ApkProject): Long

    @Update
    suspend fun updateProject(project: ApkProject)

    @Delete
    suspend fun deleteProject(project: ApkProject)

    @Query("DELETE FROM apk_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)
}
