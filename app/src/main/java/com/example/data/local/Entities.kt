package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val thumbnailUrl: String,
    val isYoutube: Boolean,
    val youtubeVideoId: String,
    val isFavorite: Boolean,
    val playCount: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationSeconds = durationSeconds,
        thumbnailUrl = thumbnailUrl,
        isYoutube = isYoutube,
        youtubeVideoId = youtubeVideoId,
        isFavorite = isFavorite,
        playCount = playCount
    )

    companion object {
        fun fromDomain(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationSeconds = track.durationSeconds,
            thumbnailUrl = track.thumbnailUrl,
            isYoutube = track.isYoutube,
            youtubeVideoId = track.youtubeVideoId,
            isFavorite = track.isFavorite,
            playCount = track.playCount
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val coverUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
