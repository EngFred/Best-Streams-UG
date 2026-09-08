package com.engineerfred.beststreamsug.mobile.core.cast

import android.content.Context
import android.net.Uri
import com.google.android.gms.cast.MediaError
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var castContext: CastContext? = null
    private var castSession: CastSession? = null

    private val _isCasting = MutableStateFlow(false)
    val isCasting: StateFlow<Boolean> = _isCasting.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _isCastPlaying = MutableStateFlow(false)
    val isCastPlaying: StateFlow<Boolean> = _isCastPlaying.asStateFlow()

    private val _isCastBuffering = MutableStateFlow(false)
    val isCastBuffering: StateFlow<Boolean> = _isCastBuffering.asStateFlow()

    private val _castError = MutableStateFlow<String?>(null)
    val castError: StateFlow<String?> = _castError.asStateFlow()

    private val _currentCastStreamUrl = MutableStateFlow("")
    val currentCastStreamUrl: StateFlow<String> = _currentCastStreamUrl.asStateFlow()

    private val _currentCastTitle = MutableStateFlow("")
    val currentCastTitle: StateFlow<String> = _currentCastTitle.asStateFlow()

    private val _currentCastPosterUrl = MutableStateFlow("")
    val currentCastPosterUrl: StateFlow<String> = _currentCastPosterUrl.asStateFlow()

    private val _currentCastMeta = MutableStateFlow("")
    val currentCastMeta: StateFlow<String> = _currentCastMeta.asStateFlow()

    private var lastStreamUrl: String = ""
    private var lastTitle: String = ""
    private var lastPosterUrl: String = ""
    private var lastOverview: String = ""

    private val mediaCallback = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            val client = remoteMediaClient ?: return
            val status = client.playerState
            val isPlaying = client.isPlaying
            val isBuffering = status == MediaStatus.PLAYER_STATE_BUFFERING || status == MediaStatus.PLAYER_STATE_LOADING

            _isCastPlaying.value = isPlaying
            _isCastBuffering.value = isBuffering

            val currentMedia = client.mediaInfo
            if (currentMedia != null && currentMedia.contentId.isNotBlank()) {
                _currentCastStreamUrl.value = currentMedia.contentId
                _currentCastTitle.value = currentMedia.metadata?.getString(MediaMetadata.KEY_TITLE).orEmpty()
                val images = currentMedia.metadata?.images
                if (!images.isNullOrEmpty()) {
                    _currentCastPosterUrl.value = images[0].url.toString()
                }
                val subtitle = currentMedia.metadata?.getString(MediaMetadata.KEY_SUBTITLE).orEmpty()
                if (subtitle.isNotBlank()) {
                    _currentCastMeta.value = subtitle
                }
            }

            if (isPlaying) {
                _castError.value = null
            } else if (status == MediaStatus.PLAYER_STATE_IDLE && client.idleReason == MediaStatus.IDLE_REASON_ERROR) {
                _castError.value = "Playback error on Cast receiver"
            }
        }

        override fun onMetadataUpdated() {
            val client = remoteMediaClient ?: return
            _isCastPlaying.value = client.isPlaying
            val currentMedia = client.mediaInfo
            if (currentMedia != null && currentMedia.contentId.isNotBlank()) {
                _currentCastStreamUrl.value = currentMedia.contentId
                _currentCastTitle.value = currentMedia.metadata?.getString(MediaMetadata.KEY_TITLE).orEmpty()
                val images = currentMedia.metadata?.images
                if (!images.isNullOrEmpty()) {
                    _currentCastPosterUrl.value = images[0].url.toString()
                }
                val subtitle = currentMedia.metadata?.getString(MediaMetadata.KEY_SUBTITLE).orEmpty()
                if (subtitle.isNotBlank()) {
                    _currentCastMeta.value = subtitle
                }
            }
        }

        override fun onMediaError(error: MediaError) {
            _isCastBuffering.value = false
        }
    }

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            attachSession(session)
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            attachSession(session)
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            detachSession()
        }

        override fun onSessionSuspended(session: CastSession, reason: Int) {
            _isCasting.value = false
            _connectedDeviceName.value = null
        }

        override fun onSessionStarting(session: CastSession) {}
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionStartFailed(session: CastSession, error: Int) {
            detachSession()
        }
        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            detachSession()
        }
    }

    init {
        try {
            castContext = CastContext.getSharedInstance(context)
            castContext?.sessionManager?.addSessionManagerListener(sessionListener, CastSession::class.java)
            val initialSession = castContext?.sessionManager?.currentCastSession
            if (initialSession != null && initialSession.isConnected) {
                attachSession(initialSession)
            }
        } catch (_: Exception) {
            // Cast framework may not be available on some devices
        }
    }

    private fun attachSession(session: CastSession) {
        castSession = session
        _isCasting.value = true
        _connectedDeviceName.value = session.castDevice?.friendlyName
        _castError.value = null

        val client = session.remoteMediaClient
        client?.registerCallback(mediaCallback)

        val mediaInfo = client?.mediaInfo
        if (mediaInfo != null && mediaInfo.contentId.isNotBlank()) {
            _currentCastStreamUrl.value = mediaInfo.contentId
            _currentCastTitle.value = mediaInfo.metadata?.getString(MediaMetadata.KEY_TITLE).orEmpty()
            val images = mediaInfo.metadata?.images
            if (!images.isNullOrEmpty()) {
                _currentCastPosterUrl.value = images[0].url.toString()
            }
            val subtitle = mediaInfo.metadata?.getString(MediaMetadata.KEY_SUBTITLE).orEmpty()
            if (subtitle.isNotBlank()) {
                _currentCastMeta.value = subtitle
            }
        }
        _isCastPlaying.value = client?.isPlaying == true
        _isCastBuffering.value = client?.playerState == MediaStatus.PLAYER_STATE_BUFFERING
    }

    private fun detachSession() {
        castSession?.remoteMediaClient?.unregisterCallback(mediaCallback)
        castSession = null
        _isCasting.value = false
        _connectedDeviceName.value = null
        _isCastPlaying.value = false
        _isCastBuffering.value = false
        _currentCastStreamUrl.value = ""
        _currentCastTitle.value = ""
        _currentCastPosterUrl.value = ""
        _currentCastMeta.value = ""
        _castError.value = null
    }

    val remoteMediaClient: RemoteMediaClient?
        get() = castSession?.remoteMediaClient

    fun getCastPosition(): Long {
        return remoteMediaClient?.approximateStreamPosition ?: 0L
    }

    fun getCastDuration(): Long {
        return remoteMediaClient?.streamDuration ?: 0L
    }

    fun isCurrentlyCasting(streamUrl: String): Boolean {
        if (!_isCasting.value || streamUrl.isBlank()) return false
        val activeUrl = _currentCastStreamUrl.value
        val remoteUrl = remoteMediaClient?.mediaInfo?.contentId.orEmpty()
        return activeUrl == streamUrl || remoteUrl == streamUrl
    }

    fun loadMedia(
        streamUrl: String,
        title: String,
        posterUrl: String = "",
        overview: String = "",
        startPositionMs: Long = 0L,
        autoPlay: Boolean = true
    ) {
        val client = remoteMediaClient ?: return
        if (streamUrl.isBlank()) return

        // Prevent restarting the video from 0 if it's already the active media playing on the TV
        if (isCurrentlyCasting(streamUrl) && (client.isPlaying || client.playerState == MediaStatus.PLAYER_STATE_BUFFERING)) {
            return
        }

        lastStreamUrl = streamUrl
        lastTitle = title
        lastPosterUrl = posterUrl
        lastOverview = overview

        _currentCastStreamUrl.value = streamUrl
        _currentCastTitle.value = title
        _currentCastPosterUrl.value = posterUrl
        _currentCastMeta.value = overview
        _isCastBuffering.value = true
        _castError.value = null

        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            putString(MediaMetadata.KEY_TITLE, title)
            if (overview.isNotBlank()) {
                putString(MediaMetadata.KEY_SUBTITLE, overview)
            }
            if (posterUrl.isNotBlank()) {
                addImage(WebImage(Uri.parse(posterUrl)))
            }
        }

        val mediaInfo = MediaInfo.Builder(streamUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("video/mp4")
            .setMetadata(metadata)
            .build()

        val request = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo)
            .setAutoplay(autoPlay)
            .setCurrentTime(startPositionMs)
            .build()

        client.load(request)
    }

    fun retryLastMedia() {
        if (lastStreamUrl.isNotBlank()) {
            loadMedia(
                streamUrl = lastStreamUrl,
                title = lastTitle,
                posterUrl = lastPosterUrl,
                overview = lastOverview,
                startPositionMs = 0L,
                autoPlay = true
            )
        }
    }

    fun play() {
        remoteMediaClient?.play()
        _isCastPlaying.value = true
    }

    fun pause() {
        remoteMediaClient?.pause()
        _isCastPlaying.value = false
    }

    fun seekTo(positionMs: Long) {
        remoteMediaClient?.seek(positionMs)
    }

    fun disconnect() {
        try {
            castContext?.sessionManager?.endCurrentSession(true)
        } catch (_: Exception) {}
    }
}