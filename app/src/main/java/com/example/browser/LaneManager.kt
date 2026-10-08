package com.example.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.LanePreferences
import com.example.data.LaneRepository
import com.example.extension.ExtensionBridge
import com.example.extension.ExtensionBridgeListener
import com.example.extension.ExtensionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LaneManager(
    private val context: Context,
    private val repository: LaneRepository,
    private val extensionManager: ExtensionManager,
    private val preferences: LanePreferences,
    private val coroutineScope: CoroutineScope
) : ExtensionBridgeListener {

    private val _laneStates = MutableStateFlow<Map<Int, LaneState>>(emptyMap())
    val laneStates: StateFlow<Map<Int, LaneState>> = _laneStates.asStateFlow()

    private val webViews = mutableMapOf<Int, WebView>()
    private var defaultUserAgent: String = ""

    init {
        // Initialize 4 lanes state
        val initialMap = mutableMapOf<Int, LaneState>()
        for (i in 1..4) {
            initialMap[i] = LaneState(
                laneId = i,
                name = "Lane $i",
                url = preferences.getHomeUrl(i),
                inputUrl = preferences.getHomeUrl(i),
                isDesktopMode = preferences.isDesktopMode(i),
                skippedCount = 19
            )
        }
        _laneStates.value = initialMap
    }

    /**
     * Retrieves or creates the isolated WebView for a given lane.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun getOrCreateWebView(laneId: Int): WebView {
        webViews[laneId]?.let { return it }

        val webView = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Setup isolated settings
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                allowFileAccess = true
                allowContentAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT

                if (defaultUserAgent.isEmpty()) {
                    defaultUserAgent = userAgentString
                }

                val isDesktop = _laneStates.value[laneId]?.isDesktopMode ?: false
                userAgentString = if (isDesktop) {
                    preferences.getDesktopUserAgent()
                } else {
                    preferences.getMobileUserAgent(defaultUserAgent)
                }
            }

            // Add JavaScript Bridge for Chrome Extension polyfill
            val bridge = ExtensionBridge(laneId, this@LaneManager)
            addJavascriptInterface(bridge, "__android_lane_bridge__")

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    _laneStates.update { map ->
                        val current = map[laneId] ?: return@update map
                        map + (laneId to current.copy(
                            progress = newProgress,
                            isLoading = newProgress in 1..99
                        ))
                    }
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    if (!title.isNullOrBlank()) {
                        _laneStates.update { map ->
                            val current = map[laneId] ?: return@update map
                            map + (laneId to current.copy(title = title))
                        }
                        val currentUrl = view?.url ?: ""
                        if (currentUrl.isNotBlank() && currentUrl != "about:blank") {
                            coroutineScope.launch {
                                repository.addHistory(laneId, currentUrl, title)
                            }
                        }
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    // Keep browsing within the Lane WebView
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    val validUrl = url ?: ""
                    _laneStates.update { map ->
                        val current = map[laneId] ?: return@update map
                        map + (laneId to current.copy(
                            url = validUrl,
                            inputUrl = validUrl,
                            isLoading = true,
                            errorMessage = null
                        ))
                    }

                    // Inject document_start extensions (e.g. ad shield, dark reader)
                    coroutineScope.launch {
                        val activeExts = _laneStates.value[laneId]?.activeExtensions ?: emptyList()
                        view?.let {
                            extensionManager.injectExtensions(
                                webView = it,
                                laneId = laneId,
                                extensionIds = activeExts,
                                currentUrl = validUrl,
                                runAtStage = "document_start"
                            )
                        }
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    val validUrl = url ?: ""
                    _laneStates.update { map ->
                        val current = map[laneId] ?: return@update map
                        map + (laneId to current.copy(
                            url = validUrl,
                            inputUrl = validUrl,
                            isLoading = false,
                            canGoBack = view?.canGoBack() ?: false,
                            canGoForward = view?.canGoForward() ?: false
                        ))
                    }

                    // Inject document_end extensions (e.g. captcha solver, 2fa assistant)
                    coroutineScope.launch {
                        val activeExts = _laneStates.value[laneId]?.activeExtensions ?: emptyList()
                        view?.let {
                            extensionManager.injectExtensions(
                                webView = it,
                                laneId = laneId,
                                extensionIds = activeExts,
                                currentUrl = validUrl,
                                runAtStage = "document_end"
                            )
                        }
                        repository.addLog(laneId, "INFO", "Loaded page: $validUrl")
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        val desc = error?.description?.toString() ?: "Connection error"
                        _laneStates.update { map ->
                            val current = map[laneId] ?: return@update map
                            map + (laneId to current.copy(
                                isLoading = false,
                                errorMessage = desc
                            ))
                        }
                        coroutineScope.launch {
                            repository.addLog(laneId, "ERROR", "Page load failed: $desc")
                        }
                    }
                }
            }
        }

        webViews[laneId] = webView

        // Initial load
        val targetUrl = _laneStates.value[laneId]?.url ?: "https://www.google.com"
        webView.loadUrl(targetUrl)

        return webView
    }

    fun loadUrl(laneId: Int, input: String) {
        val formatted = formatUrl(input)
        _laneStates.update { map ->
            val current = map[laneId] ?: return@update map
            map + (laneId to current.copy(url = formatted, inputUrl = formatted))
        }
        val wv = webViews[laneId]
        if (wv != null) {
            wv.loadUrl(formatted)
        } else {
            getOrCreateWebView(laneId).loadUrl(formatted)
        }
    }

    fun updateInputUrl(laneId: Int, input: String) {
        _laneStates.update { map ->
            val current = map[laneId] ?: return@update map
            map + (laneId to current.copy(inputUrl = input))
        }
    }

    fun goBack(laneId: Int) {
        webViews[laneId]?.let { wv ->
            if (wv.canGoBack()) {
                wv.goBack()
            }
        }
    }

    fun goForward(laneId: Int) {
        webViews[laneId]?.let { wv ->
            if (wv.canGoForward()) {
                wv.goForward()
            }
        }
    }

    fun reload(laneId: Int) {
        webViews[laneId]?.reload()
    }

    fun goHome(laneId: Int) {
        val home = preferences.getHomeUrl(laneId)
        loadUrl(laneId, home)
    }

    fun toggleDesktopMode(laneId: Int) {
        val current = _laneStates.value[laneId]?.isDesktopMode ?: false
        val newMode = !current
        preferences.setDesktopMode(laneId, newMode)

        _laneStates.update { map ->
            val state = map[laneId] ?: return@update map
            map + (laneId to state.copy(isDesktopMode = newMode))
        }

        webViews[laneId]?.let { wv ->
            wv.settings.userAgentString = if (newMode) {
                preferences.getDesktopUserAgent()
            } else {
                preferences.getMobileUserAgent(defaultUserAgent)
            }
            wv.settings.useWideViewPort = newMode
            wv.reload()
        }

        coroutineScope.launch {
            repository.updateDesktopMode(laneId, newMode)
            repository.addLog(laneId, "INFO", "Switched to ${if (newMode) "Desktop" else "Mobile"} mode")
        }
    }

    fun setLaneExtensions(laneId: Int, extensionIds: List<String>) {
        _laneStates.update { map ->
            val state = map[laneId] ?: return@update map
            map + (laneId to state.copy(activeExtensions = extensionIds))
        }
        coroutineScope.launch {
            repository.updateLaneExtensions(laneId, extensionIds.joinToString(","))
            repository.addLog(laneId, "INFO", "Updated extensions: ${extensionIds.joinToString()}")
        }
    }

    fun clearLaneData(laneId: Int) {
        webViews[laneId]?.let { wv ->
            wv.clearCache(true)
            wv.clearHistory()
            wv.clearFormData()
        }
        preferences.clearLaneStorage(laneId)
        coroutineScope.launch {
            repository.clearHistoryForLane(laneId)
            repository.addLog(laneId, "WARN", "Cleared cache, history, and storage for Lane $laneId")
        }
    }

    // --- Automation Controls ---
    fun startLane(laneId: Int) {
        _laneStates.update { map ->
            val state = map[laneId] ?: return@update map
            map + (laneId to state.copy(status = "RUNNING"))
        }
        coroutineScope.launch {
            repository.updateStatus(laneId, "RUNNING")
            repository.addLog(laneId, "SUCCESS", "Lane $laneId task started.")
        }
    }

    fun pauseLane(laneId: Int) {
        _laneStates.update { map ->
            val state = map[laneId] ?: return@update map
            map + (laneId to state.copy(status = "PAUSED"))
        }
        coroutineScope.launch {
            repository.updateStatus(laneId, "PAUSED")
            repository.addLog(laneId, "WARN", "Lane $laneId task paused.")
        }
    }

    fun stopLane(laneId: Int) {
        _laneStates.update { map ->
            val state = map[laneId] ?: return@update map
            map + (laneId to state.copy(status = "IDLE"))
        }
        coroutineScope.launch {
            repository.updateStatus(laneId, "IDLE")
            repository.addLog(laneId, "INFO", "Lane $laneId task stopped.")
        }
    }

    fun startAll() {
        for (i in 1..4) startLane(i)
    }

    fun pauseAll() {
        for (i in 1..4) pauseLane(i)
    }

    fun stopAll() {
        for (i in 1..4) stopLane(i)
    }

    fun batchNavigate(urls: List<String>) {
        urls.forEachIndexed { index, url ->
            val laneId = index + 1
            if (laneId <= 4 && url.isNotBlank()) {
                loadUrl(laneId, url)
            }
        }
    }

    // --- Extension Bridge Listener Callbacks ---
    override fun onMetricUpdate(laneId: Int, savedDelta: Int, notFoundDelta: Int, skippedDelta: Int) {
        _laneStates.update { map ->
            val current = map[laneId] ?: return@update map
            val newSaved = (current.savedCount + savedDelta).coerceAtLeast(0)
            val newNotFound = (current.notFoundCount + notFoundDelta).coerceAtLeast(0)
            val newSkipped = (current.skippedCount + skippedDelta).coerceAtLeast(0)
            map + (laneId to current.copy(
                savedCount = newSaved,
                notFoundCount = newNotFound,
                skippedCount = newSkipped
            ))
        }
        coroutineScope.launch {
            val state = _laneStates.value[laneId] ?: return@launch
            repository.updateMetrics(laneId, state.savedCount, state.notFoundCount, state.skippedCount)
        }
    }

    override fun onExtensionLog(laneId: Int, level: String, message: String) {
        coroutineScope.launch {
            repository.addLog(laneId, level, message)
        }
    }

    override fun onExtensionEvent(laneId: Int, extensionId: String, eventType: String, payload: String) {
        coroutineScope.launch {
            repository.addLog(laneId, "INFO", "[$extensionId] Event $eventType: $payload")
        }
    }

    /**
     * Cleans up all WebViews to prevent memory leaks.
     */
    fun destroyAll() {
        webViews.forEach { (_, wv) ->
            try {
                (wv.parent as? ViewGroup)?.removeView(wv)
                wv.stopLoading()
                wv.loadUrl("about:blank")
                wv.clearHistory()
                wv.removeAllViews()
                wv.destroy()
            } catch (e: Exception) {
                Log.e("LaneManager", "Error destroying webview: ${e.message}")
            }
        }
        webViews.clear()
    }

    private fun formatUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return "https://www.google.com"
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://")) {
            return trimmed
        }
        // If domain format like google.com or instagram.com
        if (trimmed.contains(".") && !trimmed.contains(" ")) {
            return "https://$trimmed"
        }
        // Query search
        return "https://www.google.com/search?q=" + android.net.Uri.encode(trimmed)
    }
}
