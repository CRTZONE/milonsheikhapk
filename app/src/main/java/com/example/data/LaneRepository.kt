package com.example.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LaneRepository(
    private val laneDao: LaneDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val allLanes: Flow<List<LaneEntity>> = laneDao.getAllLanes()
    val allExtensions: Flow<List<ExtensionEntity>> = laneDao.getAllExtensions()
    val recentLogs: Flow<List<LaneLogEntity>> = laneDao.getRecentLogs(150)

    suspend fun initializeDefaultLanesIfNeeded() = withContext(ioDispatcher) {
        val existing = laneDao.getLaneById(1)
        if (existing == null) {
            val initialLanes = listOf(
                LaneEntity(
                    id = 1,
                    name = "Lane 1",
                    currentUrl = "https://www.google.com",
                    isDesktopMode = false,
                    savedCount = 0,
                    notFoundCount = 0,
                    skippedCount = 19,
                    status = "IDLE",
                    enabledExtensions = "captcha_solver,dark_reader"
                ),
                LaneEntity(
                    id = 2,
                    name = "Lane 2",
                    currentUrl = "https://www.google.com",
                    isDesktopMode = false,
                    savedCount = 0,
                    notFoundCount = 0,
                    skippedCount = 19,
                    status = "IDLE",
                    enabledExtensions = "insta_2fa,ad_shield"
                ),
                LaneEntity(
                    id = 3,
                    name = "Lane 3",
                    currentUrl = "https://www.google.com",
                    isDesktopMode = false,
                    savedCount = 0,
                    notFoundCount = 0,
                    skippedCount = 19,
                    status = "IDLE",
                    enabledExtensions = "captcha_solver,ad_shield"
                ),
                LaneEntity(
                    id = 4,
                    name = "Lane 4",
                    currentUrl = "https://www.google.com",
                    isDesktopMode = false,
                    savedCount = 0,
                    notFoundCount = 0,
                    skippedCount = 19,
                    status = "IDLE",
                    enabledExtensions = "insta_2fa"
                )
            )
            laneDao.insertLanes(initialLanes)
        }
    }

    suspend fun updateLaneUrl(laneId: Int, url: String) = withContext(ioDispatcher) {
        laneDao.updateLaneUrl(laneId, url)
    }

    suspend fun updateDesktopMode(laneId: Int, isDesktop: Boolean) = withContext(ioDispatcher) {
        laneDao.updateDesktopMode(laneId, isDesktop)
    }

    suspend fun updateStatus(laneId: Int, status: String) = withContext(ioDispatcher) {
        laneDao.updateStatus(laneId, status)
    }

    suspend fun updateAllStatuses(status: String) = withContext(ioDispatcher) {
        laneDao.updateAllStatuses(status)
    }

    suspend fun updateMetrics(laneId: Int, saved: Int, notFound: Int, skipped: Int) = withContext(ioDispatcher) {
        laneDao.updateMetrics(laneId, saved, notFound, skipped)
    }

    suspend fun updateLaneExtensions(laneId: Int, extensionIds: String) = withContext(ioDispatcher) {
        laneDao.updateLaneExtensions(laneId, extensionIds)
    }

    fun getHistoryForLane(laneId: Int): Flow<List<LaneHistoryEntity>> =
        laneDao.getHistoryForLane(laneId)

    suspend fun addHistory(laneId: Int, url: String, title: String) = withContext(ioDispatcher) {
        laneDao.insertHistory(
            LaneHistoryEntity(
                laneId = laneId,
                url = url,
                title = title.ifBlank { url }
            )
        )
    }

    suspend fun clearHistoryForLane(laneId: Int) = withContext(ioDispatcher) {
        laneDao.clearHistoryForLane(laneId)
    }

    suspend fun addLog(laneId: Int, level: String, message: String) = withContext(ioDispatcher) {
        laneDao.insertLog(
            LaneLogEntity(
                laneId = laneId,
                level = level,
                message = message
            )
        )
    }

    suspend fun clearLogs() = withContext(ioDispatcher) {
        laneDao.clearLogs()
    }

    suspend fun insertExtension(extension: ExtensionEntity) = withContext(ioDispatcher) {
        laneDao.insertExtension(extension)
    }

    suspend fun setExtensionEnabled(id: String, enabled: Boolean) = withContext(ioDispatcher) {
        laneDao.setExtensionEnabled(id, enabled)
    }

    suspend fun deleteExtension(id: String) = withContext(ioDispatcher) {
        laneDao.deleteExtension(id)
    }
}
