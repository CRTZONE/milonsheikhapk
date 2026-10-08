package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.browser.LaneManager
import com.example.browser.LaneState
import com.example.data.ExtensionEntity
import com.example.data.LaneDatabase
import com.example.data.LaneHistoryEntity
import com.example.data.LaneLogEntity
import com.example.data.LanePreferences
import com.example.data.LaneRepository
import com.example.extension.ExtensionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class LaneViewModel(application: Application) : AndroidViewModel(application) {

    private val database = LaneDatabase.getDatabase(application)
    private val repository = LaneRepository(database.laneDao())
    private val preferences = LanePreferences(application)
    private val extensionManager = ExtensionManager(application, repository)

    val laneManager = LaneManager(
        context = application,
        repository = repository,
        extensionManager = extensionManager,
        preferences = preferences,
        coroutineScope = viewModelScope
    )

    val laneStates: StateFlow<Map<Int, LaneState>> = laneManager.laneStates

    val allExtensions: StateFlow<List<ExtensionEntity>> = repository.allExtensions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<LaneLogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Visible Screens layout: 1, 2, 3, or 4
    private val _screenLayout = MutableStateFlow(4) // 4 for 2x2 grid
    val screenLayout: StateFlow<Int> = _screenLayout.asStateFlow()

    // Focused lane when 1 screen is selected
    private val _focusedLaneId = MutableStateFlow(1)
    val focusedLaneId: StateFlow<Int> = _focusedLaneId.asStateFlow()

    // Dialog & Sheet States
    private val _showExtensionsDialog = MutableStateFlow(false)
    val showExtensionsDialog: StateFlow<Boolean> = _showExtensionsDialog.asStateFlow()

    private val _showBatchUrlDialog = MutableStateFlow(false)
    val showBatchUrlDialog: StateFlow<Boolean> = _showBatchUrlDialog.asStateFlow()

    private val _showLogsSheet = MutableStateFlow(false)
    val showLogsSheet: StateFlow<Boolean> = _showLogsSheet.asStateFlow()

    private val _historyLaneId = MutableStateFlow<Int?>(null)
    val historyLaneId: StateFlow<Int?> = _historyLaneId.asStateFlow()

    private val _selectedLogLane = MutableStateFlow(0) // 0 = all
    val selectedLogLane: StateFlow<Int> = _selectedLogLane.asStateFlow()

    private val _notification = MutableStateFlow<UiNotification?>(null)
    val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultLanesIfNeeded()
            extensionManager.initializeExtensions()

            // Initialize default active extensions for each lane
            laneManager.setLaneExtensions(1, listOf("captcha_solver", "dark_reader"))
            laneManager.setLaneExtensions(2, listOf("insta_2fa", "ad_shield"))
            laneManager.setLaneExtensions(3, listOf("captcha_solver", "ad_shield"))
            laneManager.setLaneExtensions(4, listOf("insta_2fa"))
        }
    }

    fun setScreenLayout(count: Int) {
        _screenLayout.value = count.coerceIn(1, 4)
    }

    fun setFocusedLane(laneId: Int) {
        _focusedLaneId.value = laneId.coerceIn(1, 4)
    }

    fun updateInputUrl(laneId: Int, input: String) {
        laneManager.updateInputUrl(laneId, input)
    }

    fun navigateLane(laneId: Int, url: String) {
        laneManager.loadUrl(laneId, url)
    }

    fun goBack(laneId: Int) = laneManager.goBack(laneId)
    fun goForward(laneId: Int) = laneManager.goForward(laneId)
    fun reload(laneId: Int) = laneManager.reload(laneId)
    fun goHome(laneId: Int) = laneManager.goHome(laneId)
    fun toggleDesktopMode(laneId: Int) = laneManager.toggleDesktopMode(laneId)

    fun startAll() = laneManager.startAll()
    fun pauseAll() = laneManager.pauseAll()
    fun stopAll() = laneManager.stopAll()

    fun startLane(laneId: Int) = laneManager.startLane(laneId)
    fun pauseLane(laneId: Int) = laneManager.pauseLane(laneId)
    fun stopLane(laneId: Int) = laneManager.stopLane(laneId)

    fun clearLaneData(laneId: Int) {
        laneManager.clearLaneData(laneId)
        notify("Cleared cache & history for Lane $laneId")
    }

    fun batchNavigate(urls: List<String>) {
        laneManager.batchNavigate(urls)
        notify("Dispatched batch URLs to lanes")
    }

    fun toggleLaneExtension(laneId: Int, extensionId: String) {
        val currentExts = laneStates.value[laneId]?.activeExtensions?.toMutableList() ?: mutableListOf()
        if (currentExts.contains(extensionId)) {
            currentExts.remove(extensionId)
        } else {
            currentExts.add(extensionId)
        }
        laneManager.setLaneExtensions(laneId, currentExts)
    }

    fun uploadExtensionZip(uri: Uri) {
        viewModelScope.launch {
            val result = extensionManager.installExtensionFromZip(uri)
            if (result.isSuccess) {
                val ext = result.getOrNull()
                notify("Extension '${ext?.name}' installed successfully!")
            } else {
                val err = result.exceptionOrNull()?.message ?: "Installation failed"
                notify("Failed to install extension: $err", isError = true)
            }
        }
    }

    fun getHistoryForLane(laneId: Int): StateFlow<List<LaneHistoryEntity>> {
        return repository.getHistoryForLane(laneId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun clearHistoryForLane(laneId: Int) {
        viewModelScope.launch {
            repository.clearHistoryForLane(laneId)
            notify("Cleared history for Lane $laneId")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            notify("Cleared logs")
        }
    }

    fun openExtensionsDialog() { _showExtensionsDialog.value = true }
    fun closeExtensionsDialog() { _showExtensionsDialog.value = false }

    fun openBatchUrlDialog() { _showBatchUrlDialog.value = true }
    fun closeBatchUrlDialog() { _showBatchUrlDialog.value = false }

    fun openLogsSheet() { _showLogsSheet.value = true }
    fun closeLogsSheet() { _showLogsSheet.value = false }

    fun openHistoryDialog(laneId: Int) { _historyLaneId.value = laneId }
    fun closeHistoryDialog() { _historyLaneId.value = null }

    fun setSelectedLogLane(laneId: Int) { _selectedLogLane.value = laneId }

    fun dismissNotification() { _notification.value = null }

    private fun notify(msg: String, isError: Boolean = false) {
        _notification.value = UiNotification(message = msg, isError = isError)
    }

    override fun onCleared() {
        super.onCleared()
        laneManager.destroyAll()
    }
}
