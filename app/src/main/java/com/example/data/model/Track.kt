package com.example.data.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "YouTube Music",
    val durationSeconds: Int = 210,
    val thumbnailUrl: String = "",
    val isYoutube: Boolean = true,
    val youtubeVideoId: String = "",
    val isFavorite: Boolean = false,
    val playCount: Int = 0
) {
    val durationFormatted: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return "%d:%02d".format(mins, secs)
        }
}

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val coverUrl: String = "",
    val trackCount: Int = 0,
    val tracks: List<Track> = emptyList()
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: String,
    val coverUrl: String,
    val tracks: List<Track> = emptyList()
)

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String,
    val monthlyListeners: String = "1.2M",
    val topTracks: List<Track> = emptyList()
)

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isPlayerReady: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 0.85f,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val isNowPlayingExpanded: Boolean = false,
    val playbackError: String? = null,
    val restrictedVideoId: String? = null,
    val sleepTimerSecondsRemaining: Long? = null,
    val sleepTimerTotalMinutes: Int? = null
)
