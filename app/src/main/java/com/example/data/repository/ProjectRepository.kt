package com.example.data.repository

import com.example.data.local.BuildHistoryDao
import com.example.data.local.ProjectDao
import com.example.data.model.ApkProject
import com.example.data.model.BuildHistoryItem
import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val buildHistoryDao: BuildHistoryDao
) {
    val allProjects: Flow<List<ApkProject>> = projectDao.getAllProjects()
    val allBuilds: Flow<List<BuildHistoryItem>> = buildHistoryDao.getAllBuilds()

    suspend fun getProject(id: Long): ApkProject? = projectDao.getProjectById(id)

    fun observeProject(id: Long): Flow<ApkProject?> = projectDao.observeProjectById(id)

    suspend fun saveProject(project: ApkProject): Long {
        return if (project.id == 0L) {
            projectDao.insertProject(project.copy(createdDate = System.currentTimeMillis(), updatedDate = System.currentTimeMillis()))
        } else {
            projectDao.updateProject(project.copy(updatedDate = System.currentTimeMillis()))
            project.id
        }
    }

    suspend fun deleteProject(id: Long) {
        projectDao.deleteProjectById(id)
    }

    suspend fun duplicateProject(project: ApkProject): Long {
        val duplicated = project.copy(
            id = 0,
            appName = "${project.appName} (Copy)",
            packageName = "${project.packageName}.copy",
            createdDate = System.currentTimeMillis(),
            updatedDate = System.currentTimeMillis(),
            lastBuildDate = null,
            buildStatus = "DRAFT",
            lastApkSizeMb = null,
            lastDownloadUrl = null,
            lastBuildLogs = null
        )
        return projectDao.insertProject(duplicated)
    }

    suspend fun addBuildHistory(item: BuildHistoryItem): Long {
        return buildHistoryDao.insertBuild(item)
    }

    suspend fun deleteBuildHistory(id: Long) {
        buildHistoryDao.deleteBuildById(id)
    }

    fun getBuildsForProject(projectId: Long): Flow<List<BuildHistoryItem>> {
        return buildHistoryDao.getBuildsForProject(projectId)
    }

    suspend fun createDefaultAppGalleryProject(): Long {
        val appGallery = ApkProject(
            appName = "App Gallery",
            packageName = "com.appgallery.games",
            websiteUrl = "https://html5games.com",
            versionName = "1.0.0",
            versionCode = 1,
            iconPreset = "games",
            splashEnabled = true,
            splashBgColor = "#0A0F1D",
            splashDurationSeconds = 2.0f,
            statusBarColor = "#0A0F1D",
            navBarColor = "#0A0F1D",
            appBgColor = "#111827",
            isDarkSystemUi = false,
            isFullScreen = false,
            orientation = "PORTRAIT",
            jsEnabled = true,
            domStorageEnabled = true,
            zoomEnabled = false,
            pullToRefresh = true,
            allowFileUploads = true,
            allowCamera = true,
            allowMicrophone = false,
            allowLocation = false,
            openExternalInBrowser = true,
            downloadSupport = true,
            buildStatus = "DRAFT"
        )
        return projectDao.insertProject(appGallery)
    }
}
