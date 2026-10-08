package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LaneDao {

    // --- Lanes ---
    @Query("SELECT * FROM lanes ORDER BY id ASC")
    fun getAllLanes(): Flow<List<LaneEntity>>

    @Query("SELECT * FROM lanes WHERE id = :laneId LIMIT 1")
    suspend fun getLaneById(laneId: Int): LaneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLane(lane: LaneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanes(lanes: List<LaneEntity>)

    @Update
    suspend fun updateLane(lane: LaneEntity)

    @Query("UPDATE lanes SET currentUrl = :url WHERE id = :laneId")
    suspend fun updateLaneUrl(laneId: Int, url: String)

    @Query("UPDATE lanes SET isDesktopMode = :desktopMode WHERE id = :laneId")
    suspend fun updateDesktopMode(laneId: Int, desktopMode: Boolean)

    @Query("UPDATE lanes SET status = :status WHERE id = :laneId")
    suspend fun updateStatus(laneId: Int, status: String)

    @Query("UPDATE lanes SET status = :status")
    suspend fun updateAllStatuses(status: String)

    @Query("UPDATE lanes SET savedCount = :saved, notFoundCount = :notFound, skippedCount = :skipped WHERE id = :laneId")
    suspend fun updateMetrics(laneId: Int, saved: Int, notFound: Int, skipped: Int)

    @Query("UPDATE lanes SET enabledExtensions = :extensionIds WHERE id = :laneId")
    suspend fun updateLaneExtensions(laneId: Int, extensionIds: String)

    // --- Isolated History per Lane ---
    @Query("SELECT * FROM lane_history WHERE laneId = :laneId ORDER BY timestamp DESC")
    fun getHistoryForLane(laneId: Int): Flow<List<LaneHistoryEntity>>

    @Query("SELECT * FROM lane_history ORDER BY timestamp DESC LIMIT 200")
    fun getAllHistory(): Flow<List<LaneHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: LaneHistoryEntity)

    @Query("DELETE FROM lane_history WHERE laneId = :laneId")
    suspend fun clearHistoryForLane(laneId: Int)

    @Query("DELETE FROM lane_history")
    suspend fun clearAllHistory()

    // --- Logs ---
    @Query("SELECT * FROM lane_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<LaneLogEntity>>

    @Query("SELECT * FROM lane_logs WHERE laneId = :laneId ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsForLane(laneId: Int, limit: Int = 100): Flow<List<LaneLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LaneLogEntity)

    @Query("DELETE FROM lane_logs")
    suspend fun clearLogs()

    // --- Extensions ---
    @Query("SELECT * FROM extensions ORDER BY installedTimestamp ASC")
    fun getAllExtensions(): Flow<List<ExtensionEntity>>

    @Query("SELECT * FROM extensions WHERE id = :id LIMIT 1")
    suspend fun getExtensionById(id: String): ExtensionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtension(extension: ExtensionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtensions(extensions: List<ExtensionEntity>)

    @Query("UPDATE extensions SET enabledGlobal = :enabled WHERE id = :id")
    suspend fun setExtensionEnabled(id: String, enabled: Boolean)

    @Query("DELETE FROM extensions WHERE id = :id")
    suspend fun deleteExtension(id: String)
}
