package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.HistoryEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistTrackCrossRef
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.TrackEntity
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.data.remote.YouTubeApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class MusicRepository(
    private val database: AppDatabase,
    private val youTubeApi: YouTubeApiService = YouTubeApiService.create()
) {
    private val musicDao = database.musicDao()

    val allTracks: Flow<List<Track>> = musicDao.getAllTracks()
        .map { list -> list.map { it.toDomain() } }
        .flowOn(Dispatchers.IO)

    val favoriteTracks: Flow<List<Track>> = musicDao.getFavoriteTracks()
        .map { list -> list.map { it.toDomain() } }
        .flowOn(Dispatchers.IO)

    val recentHistoryTracks: Flow<List<Track>> = musicDao.getRecentHistoryTracks()
        .map { list -> list.map { it.toDomain() } }
        .flowOn(Dispatchers.IO)

    val playlists: Flow<List<Playlist>> = musicDao.getAllPlaylists()
        .map { list ->
            list.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    coverUrl = entity.coverUrl
                )
            }
        }
        .flowOn(Dispatchers.IO)

    val recentSearchQueries: Flow<List<String>> = musicDao.getRecentSearchQueries()
        .flowOn(Dispatchers.IO)

    suspend fun initDefaultDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = musicDao.getAllTracks().first()
        if (existing.isEmpty()) {
            val initialTracks = getCuratedTracks()
            musicDao.insertTracks(initialTracks.map { TrackEntity.fromDomain(it) })

            // Default Playlists
            val p1 = PlaylistEntity(
                id = "pl_favorites",
                name = "AR Favorites (প্রিয় গান)",
                description = "Your favorite trending YouTube hits",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80"
            )
            val p2 = PlaylistEntity(
                id = "pl_bengali_vibes",
                name = "Bengali Melodies (বাংলা সুর)",
                description = "Soulful Bengali songs & trending hits",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80"
            )
            val p3 = PlaylistEntity(
                id = "pl_lofi",
                name = "Lo-Fi Midnight Chill",
                description = "Relaxing beats to study and relax",
                coverUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&q=80"
            )
            musicDao.insertPlaylist(p1)
            musicDao.insertPlaylist(p2)
            musicDao.insertPlaylist(p3)

            initialTracks.take(4).forEach { track ->
                musicDao.addTrackToPlaylist(PlaylistTrackCrossRef(p1.id, track.id))
            }
            initialTracks.filter { it.artist.contains("Arijit") || it.artist.contains("Anupam") || it.artist.contains("James") }
                .forEach { track ->
                    musicDao.addTrackToPlaylist(PlaylistTrackCrossRef(p2.id, track.id))
                }
        }
    }

    suspend fun toggleFavorite(trackId: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        musicDao.setFavorite(trackId, !currentFavorite)
    }

    suspend fun saveTrack(track: Track) = withContext(Dispatchers.IO) {
        musicDao.insertTrack(TrackEntity.fromDomain(track))
    }

    suspend fun recordHistory(trackId: String) = withContext(Dispatchers.IO) {
        musicDao.incrementPlayCount(trackId)
        musicDao.insertHistory(HistoryEntity(trackId = trackId))
    }

    suspend fun saveSearchQuery(query: String) = withContext(Dispatchers.IO) {
        if (query.isNotBlank()) {
            musicDao.insertSearchQuery(SearchHistoryEntity(query = query.trim()))
        }
    }

    suspend fun deleteSearchQuery(query: String) = withContext(Dispatchers.IO) {
        musicDao.deleteSearchQuery(query)
    }

    suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        musicDao.clearSearchHistory()
    }

    suspend fun createPlaylist(name: String, description: String = "", coverUrl: String = "") = withContext(Dispatchers.IO) {
        val id = "pl_${UUID.randomUUID()}"
        val playlist = PlaylistEntity(
            id = id,
            name = name,
            description = description.ifEmpty { "Created in AR Music" },
            coverUrl = coverUrl.ifEmpty { "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&q=80" }
        )
        musicDao.insertPlaylist(playlist)
        id
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        musicDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, trackId))
    }

    fun getPlaylistTracks(playlistId: String): Flow<List<Track>> {
        return musicDao.getTracksForPlaylist(playlistId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    suspend fun searchYouTubeOrLocal(query: String, apiKey: String): List<Track> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        if (apiKey.isNotBlank()) {
            try {
                val response = youTubeApi.searchMusic(query = trimmed, apiKey = apiKey)
                val onlineResults = response.items.mapNotNull { item ->
                    val videoId = item.id.videoId ?: return@mapNotNull null
                    val snippet = item.snippet
                    val thumb = snippet.thumbnails?.high?.url
                        ?: snippet.thumbnails?.medium?.url
                        ?: snippet.thumbnails?.defaultThumb?.url
                        ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

                    Track(
                        id = "yt_$videoId",
                        title = cleanHtmlEntities(snippet.title),
                        artist = snippet.channelTitle,
                        album = "YouTube Music",
                        durationSeconds = 240,
                        thumbnailUrl = thumb,
                        isYoutube = true,
                        youtubeVideoId = videoId
                    )
                }
                if (onlineResults.isNotEmpty()) {
                    musicDao.insertTracks(onlineResults.map { TrackEntity.fromDomain(it) })
                    return@withContext onlineResults
                }
            } catch (e: Exception) {
                // If API quota or network error, fallback to curated library
            }
        }

        // Fallback: search curated tracks + local DB
        val curated = getCuratedTracks()
        curated.filter {
            it.title.contains(trimmed, ignoreCase = true) ||
            it.artist.contains(trimmed, ignoreCase = true) ||
            it.album.contains(trimmed, ignoreCase = true)
        }
    }

    private fun cleanHtmlEntities(text: String): String {
        return text
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

    fun getCuratedAlbums(): List<Album> {
        val tracks = getCuratedTracks()
        return listOf(
            Album(
                id = "alb_bengali_timeless",
                title = "Bengali Melodies & Acoustic",
                artist = "Various Artists",
                year = "2025",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                tracks = tracks.filter { it.album.contains("Bengali") || it.artist.contains("Arijit") || it.artist.contains("Anupam") }
            ),
            Album(
                id = "alb_global_hits",
                title = "AR Global Chartbusters",
                artist = "Top World Artists",
                year = "2026",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80",
                tracks = tracks.filter { !it.artist.contains("Rabindra") }
            ),
            Album(
                id = "alb_synthwave",
                title = "Neon Waves & Midnight Chill",
                artist = "AR Electronic Lab",
                year = "2026",
                coverUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80",
                tracks = tracks.takeLast(4)
            )
        )
    }

    fun getCuratedArtists(): List<Artist> {
        val tracks = getCuratedTracks()
        return listOf(
            Artist(
                id = "art_arijit",
                name = "Arijit Singh (অরিজিৎ সিং)",
                imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&q=80",
                monthlyListeners = "42.5M",
                topTracks = tracks.filter { it.artist.contains("Arijit") }
            ),
            Artist(
                id = "art_anupam",
                name = "Anupam Roy (অনুপম রায়)",
                imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&q=80",
                monthlyListeners = "8.3M",
                topTracks = tracks.filter { it.artist.contains("Anupam") }
            ),
            Artist(
                id = "art_james",
                name = "James (নগর বাউল জেমস)",
                imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&q=80",
                monthlyListeners = "6.1M",
                topTracks = tracks.filter { it.artist.contains("James") }
            ),
            Artist(
                id = "art_weeknd",
                name = "The Weeknd",
                imageUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500&q=80",
                monthlyListeners = "108M",
                topTracks = tracks.filter { it.artist.contains("Weeknd") }
            ),
            Artist(
                id = "art_dua",
                name = "Dua Lipa",
                imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&q=80",
                monthlyListeners = "78M",
                topTracks = tracks.filter { it.artist.contains("Dua") }
            ),
            Artist(
                id = "art_alan",
                name = "Alan Walker",
                imageUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=500&q=80",
                monthlyListeners = "54M",
                topTracks = tracks.filter { it.artist.contains("Walker") }
            )
        )
    }

    fun getCuratedTracks(): List<Track> {
        return listOf(
            Track(
                id = "yt_1",
                title = "Tumi Robe Nirobe (তুমি রবে নীরবে)",
                artist = "Rabindrasangeet Melodies",
                album = "Bengali Melodies",
                durationSeconds = 245,
                thumbnailUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                isYoutube = true,
                youtubeVideoId = "dQw4w9WgXcQ",
                isFavorite = true
            ),
            Track(
                id = "yt_2",
                title = "Amake Amar Moto Thakte Dao",
                artist = "Anupam Roy",
                album = "Autograph",
                durationSeconds = 280,
                thumbnailUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80",
                isYoutube = true,
                youtubeVideoId = "kJQP7kiw5Fk",
                isFavorite = true
            ),
            Track(
                id = "yt_3",
                title = "Tum Hi Ho (আশিকী ২)",
                artist = "Arijit Singh",
                album = "Aashiqui 2",
                durationSeconds = 262,
                thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                isYoutube = true,
                youtubeVideoId = "IJq0yyWug1k",
                isFavorite = true
            ),
            Track(
                id = "yt_4",
                title = "Blinding Lights",
                artist = "The Weeknd",
                album = "After Hours",
                durationSeconds = 200,
                thumbnailUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80",
                isYoutube = true,
                youtubeVideoId = "4NRXx6U8ABQ",
                isFavorite = true
            ),
            Track(
                id = "yt_5",
                title = "Faded",
                artist = "Alan Walker",
                album = "Different World",
                durationSeconds = 212,
                thumbnailUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&q=80",
                isYoutube = true,
                youtubeVideoId = "60ItHLz5WEA",
                isFavorite = false
            ),
            Track(
                id = "yt_6",
                title = "Levitating",
                artist = "Dua Lipa",
                album = "Future Nostalgia",
                durationSeconds = 203,
                thumbnailUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&q=80",
                isYoutube = true,
                youtubeVideoId = "TUVcZfQe-Kw",
                isFavorite = false
            ),
            Track(
                id = "yt_7",
                title = "Believer",
                artist = "Imagine Dragons",
                album = "Evolve",
                durationSeconds = 204,
                thumbnailUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80",
                isYoutube = true,
                youtubeVideoId = "7wtfhZwyrcc",
                isFavorite = true
            ),
            Track(
                id = "yt_8",
                title = "Shape of You",
                artist = "Ed Sheeran",
                album = "Divide",
                durationSeconds = 233,
                thumbnailUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                isYoutube = true,
                youtubeVideoId = "JGwWNGJdvx8",
                isFavorite = false
            )
        )
    }
}
