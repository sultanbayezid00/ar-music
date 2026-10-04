package com.example.player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.PlayerState
import com.example.data.model.RepeatMode
import com.example.data.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class YouTubeBridge(
    private val playerManager: PlayerManager,
    private val mainHandler: Handler = Handler(Looper.getMainLooper())
) {
    @JavascriptInterface
    fun onPlayerReady() {
        mainHandler.post {
            playerManager.onYouTubePlayerReady()
        }
    }

    @JavascriptInterface
    fun onPlayerStateChange(state: Int) {
        mainHandler.post {
            playerManager.onYouTubePlayerStateChange(state)
        }
    }

    @JavascriptInterface
    fun onTimeUpdate(currentTime: Float, duration: Float) {
        mainHandler.post {
            playerManager.onYouTubeTimeUpdate(currentTime, duration)
        }
    }

    @JavascriptInterface
    fun onPlayerError(errorCode: Int) {
        mainHandler.post {
            playerManager.onYouTubePlayerError(errorCode)
        }
    }
}

class PlayerManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var webView: WebView? = null
    private var isPlayerReady = false
    private var pendingVideoId: String? = null

    private var sleepTimerJob: Job? = null
    private var autoNextJob: Job? = null

    var onTrackChangedListener: ((Track) -> Unit)? = null

    init {
        // Initialize WebView immediately on main thread so player is ready upon app launch
        ensureWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun ensureWebView(): WebView {
        if (webView == null) {
            val wv = WebView(context.applicationContext)
            wv.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            wv.setBackgroundColor(android.graphics.Color.BLACK)
            wv.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            wv.settings.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                loadWithOverviewMode = true
                useWideViewPort = true
                allowFileAccess = true
                allowContentAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
            }

            wv.addJavascriptInterface(YouTubeBridge(this), "AndroidBridge")
            wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
            wv.webChromeClient = WebChromeClient()
            wv.loadDataWithBaseURL("https://localhost", getPlayerHtml(), "text/html", "UTF-8", null)
            webView = wv
        }
        return webView!!
    }

    fun playTrack(track: Track, newQueue: List<Track>? = null) {
        autoNextJob?.cancel()

        val currentQueue = newQueue ?: _playerState.value.queue.ifEmpty { listOf(track) }
        val index = currentQueue.indexOfFirst { it.id == track.id }.let { if (it == -1) 0 else it }

        _playerState.update {
            it.copy(
                currentTrack = track,
                isPlaying = true,
                isBuffering = true,
                currentPositionMs = 0L,
                durationMs = track.durationSeconds * 1000L,
                queue = currentQueue,
                currentIndex = index,
                playbackError = null,
                restrictedVideoId = null
            )
        }

        onTrackChangedListener?.invoke(track)

        val videoId = track.youtubeVideoId.ifEmpty { "dQw4w9WgXcQ" }
        if (isPlayerReady) {
            executeJs("loadVideo('$videoId')")
        } else {
            pendingVideoId = videoId
            ensureWebView()
        }
    }

    fun togglePlayPause() {
        val current = _playerState.value
        if (current.currentTrack == null) {
            if (current.queue.isNotEmpty()) {
                playTrack(current.queue.first(), current.queue)
            }
            return
        }

        if (current.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _playerState.update { it.copy(isPlaying = false, isBuffering = false) }
        executeJs("pauseVideo()")
    }

    fun resume() {
        _playerState.update { it.copy(isPlaying = true) }
        executeJs("playVideo()")
    }

    fun next() {
        autoNextJob?.cancel()
        val state = _playerState.value
        if (state.queue.isEmpty()) return

        var nextIndex = state.currentIndex + 1
        if (state.isShuffle && state.queue.size > 1) {
            val possible = state.queue.indices.filter { it != state.currentIndex }
            nextIndex = possible.random()
        } else if (nextIndex >= state.queue.size) {
            if (state.repeatMode == RepeatMode.ALL) {
                nextIndex = 0
            } else {
                pause()
                return
            }
        }

        val nextTrack = state.queue[nextIndex]
        playTrack(nextTrack, state.queue)
    }

    fun previous() {
        autoNextJob?.cancel()
        val state = _playerState.value
        if (state.queue.isEmpty()) return

        // If played more than 3 seconds, restart current track
        if (state.currentPositionMs > 3000L) {
            seekTo(0L)
            return
        }

        var prevIndex = state.currentIndex - 1
        if (prevIndex < 0) {
            prevIndex = if (state.repeatMode == RepeatMode.ALL) state.queue.size - 1 else 0
        }
        val prevTrack = state.queue[prevIndex]
        playTrack(prevTrack, state.queue)
    }

    fun seekTo(positionMs: Long) {
        val safePosition = positionMs.coerceIn(0L, _playerState.value.durationMs.coerceAtLeast(1000L))
        _playerState.update { it.copy(currentPositionMs = safePosition) }
        val seconds = (safePosition / 1000).toInt()
        executeJs("seekTo($seconds)")
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _playerState.update { it.copy(volume = clamped) }
        val ytVolume = (clamped * 100).toInt()
        executeJs("setVolume($ytVolume)")
    }

    fun toggleShuffle() {
        _playerState.update { it.copy(isShuffle = !it.isShuffle) }
    }

    fun toggleRepeat() {
        _playerState.update {
            val nextRepeat = when (it.repeatMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            it.copy(repeatMode = nextRepeat)
        }
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _playerState.update { it.copy(isNowPlayingExpanded = expanded) }
    }

    fun dismissError() {
        _playerState.update { it.copy(playbackError = null, restrictedVideoId = null) }
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSeconds = minutes * 60L
        _playerState.update {
            it.copy(
                sleepTimerSecondsRemaining = totalSeconds,
                sleepTimerTotalMinutes = minutes
            )
        }

        sleepTimerJob = scope.launch(Dispatchers.Main) {
            var remaining = totalSeconds
            while (isActive && remaining > 0) {
                delay(1000L)
                remaining -= 1
                _playerState.update { it.copy(sleepTimerSecondsRemaining = remaining) }
            }
            if (isActive) {
                pause()
                _playerState.update {
                    it.copy(
                        sleepTimerSecondsRemaining = null,
                        sleepTimerTotalMinutes = null
                    )
                }
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _playerState.update {
            it.copy(
                sleepTimerSecondsRemaining = null,
                sleepTimerTotalMinutes = null
            )
        }
    }

    // JavaScript Bridge callbacks
    fun onYouTubePlayerReady() {
        isPlayerReady = true
        _playerState.update { it.copy(isPlayerReady = true) }

        // Set initial volume
        val initialVolume = (_playerState.value.volume * 100).toInt()
        executeJs("setVolume($initialVolume)")

        val pending = pendingVideoId
        if (pending != null) {
            pendingVideoId = null
            executeJs("loadVideo('$pending')")
        }
    }

    fun onYouTubePlayerStateChange(stateCode: Int) {
        when (stateCode) {
            1 -> { // PLAYING
                _playerState.update { it.copy(isPlaying = true, isBuffering = false, playbackError = null) }
            }
            2 -> { // PAUSED
                _playerState.update { it.copy(isPlaying = false, isBuffering = false) }
            }
            3 -> { // BUFFERING
                _playerState.update { it.copy(isBuffering = true) }
            }
            0 -> { // ENDED
                handleTrackCompletion()
            }
        }
    }

    fun onYouTubeTimeUpdate(currentTimeSeconds: Float, durationSeconds: Float) {
        _playerState.update {
            it.copy(
                currentPositionMs = (currentTimeSeconds * 1000).toLong(),
                durationMs = if (durationSeconds > 0) (durationSeconds * 1000).toLong() else it.durationMs
            )
        }
    }

    fun onYouTubePlayerError(errorCode: Int) {
        val currentTrack = _playerState.value.currentTrack
        val videoId = currentTrack?.youtubeVideoId ?: ""
        val errorMsg = when (errorCode) {
            101, 150 -> "The owner has disabled embedding for this video. You can open and watch it directly on YouTube."
            100 -> "This video was removed or marked private on YouTube."
            2 -> "Invalid video ID parameter."
            else -> "YouTube playback error occurred (Code $errorCode)."
        }

        _playerState.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                playbackError = errorMsg,
                restrictedVideoId = videoId
            )
        }

        // If queue has more items, automatically skip to next song after 4 seconds
        if (_playerState.value.queue.size > 1) {
            autoNextJob?.cancel()
            autoNextJob = scope.launch(Dispatchers.Main) {
                delay(4000L)
                if (isActive && _playerState.value.playbackError != null) {
                    next()
                }
            }
        }
    }

    private fun handleTrackCompletion() {
        val state = _playerState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0L)
                resume()
            }
            RepeatMode.ALL -> next()
            RepeatMode.OFF -> {
                if (state.currentIndex < state.queue.size - 1) {
                    next()
                } else {
                    pause()
                    seekTo(0L)
                }
            }
        }
    }

    private fun executeJs(script: String) {
        val wv = webView ?: return
        wv.post {
            try {
                wv.evaluateJavascript(script, null)
            } catch (e: Exception) {
                // Ignore evaluate errors
            }
        }
    }

    fun release() {
        sleepTimerJob?.cancel()
        autoNextJob?.cancel()
        webView?.post {
            try {
                webView?.destroy()
            } catch (e: Exception) {
                // Ignore
            }
            webView = null
        }
    }

    private fun getPlayerHtml(): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body, html { width: 100%; height: 100%; background: #000; overflow: hidden; }
                #player { width: 100%; height: 100%; position: absolute; top: 0; left: 0; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var player = null;
                var timeInterval = null;

                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        height: '100%',
                        width: '100%',
                        host: 'https://www.youtube-nocookie.com',
                        playerVars: {
                            'autoplay': 1,
                            'playsinline': 1,
                            'controls': 1,
                            'fs': 1,
                            'rel': 0,
                            'modestbranding': 1,
                            'enablejsapi': 1,
                            'origin': 'https://localhost'
                        },
                        events: {
                            'onReady': onPlayerReady,
                            'onStateChange': onPlayerStateChange,
                            'onError': onPlayerError
                        }
                    });
                }

                function onPlayerReady(event) {
                    try {
                        event.target.unMute();
                    } catch(e){}
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onPlayerReady();
                    }
                    startTimeTracking();
                }

                function onPlayerStateChange(event) {
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onPlayerStateChange(event.data);
                    }
                }

                function onPlayerError(event) {
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onPlayerError(event.data);
                    }
                }

                function startTimeTracking() {
                    if (timeInterval) clearInterval(timeInterval);
                    timeInterval = setInterval(function() {
                        if (player && typeof player.getCurrentTime === 'function' && window.AndroidBridge) {
                            var curr = player.getCurrentTime() || 0;
                            var dur = player.getDuration() || 0;
                            window.AndroidBridge.onTimeUpdate(curr, dur);
                        }
                    }, 500);
                }

                function loadVideo(videoId) {
                    if (player && typeof player.loadVideoById === 'function') {
                        try { player.unMute(); } catch(e){}
                        player.loadVideoById({
                            videoId: videoId,
                            suggestedQuality: 'large'
                        });
                        try { player.playVideo(); } catch(e){}
                    }
                }

                function playVideo() {
                    if (player && typeof player.playVideo === 'function') {
                        try { player.unMute(); } catch(e){}
                        player.playVideo();
                    }
                }

                function pauseVideo() {
                    if (player && typeof player.pauseVideo === 'function') {
                        player.pauseVideo();
                    }
                }

                function seekTo(sec) {
                    if (player && typeof player.seekTo === 'function') {
                        player.seekTo(sec, true);
                    }
                }

                function setVolume(vol) {
                    if (player && typeof player.setVolume === 'function') {
                        try { player.unMute(); } catch(e){}
                        player.setVolume(vol);
                    }
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}
