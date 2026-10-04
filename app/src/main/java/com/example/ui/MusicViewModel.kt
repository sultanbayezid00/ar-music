package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.PlayerState
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.data.repository.MusicRepository
import com.example.player.PlayerManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = MusicRepository(database)

    val playerManager = PlayerManager(application, viewModelScope)
    val playerState: StateFlow<PlayerState> = playerManager.playerState

    val allTracks: StateFlow<List<Track>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<Track>> = repository.favoriteTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyTracks: StateFlow<List<Track>> = repository.recentHistoryTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearchQueries: StateFlow<List<String>> = repository.recentSearchQueries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: List<Album> = repository.getCuratedAlbums()
    val artists: List<Artist> = repository.getCuratedArtists()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _apiKey = MutableStateFlow(getInjectedApiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPlaylistTracks: StateFlow<List<Track>> = _selectedPlaylist
        .flatMapLatest { pl ->
            if (pl != null) repository.getPlaylistTracks(pl.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultDataIfEmpty()
        }

        playerManager.onTrackChangedListener = { track ->
            viewModelScope.launch {
                repository.recordHistory(track.id)
            }
        }
    }

    private fun getInjectedApiKey(): String {
        return try {
            val field = BuildConfig::class.java.getField("YOUTUBE_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun setApiKey(newKey: String) {
        _apiKey.value = newKey
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun performSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            repository.saveSearchQuery(trimmed)
            _isSearching.value = true
            try {
                val results = repository.searchYouTubeOrLocal(trimmed, _apiKey.value)
                _searchResults.value = results
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun deleteSearchQuery(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun playTrack(track: Track, queue: List<Track>? = null) {
        viewModelScope.launch {
            repository.saveTrack(track)
            playerManager.playTrack(track, queue)
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id, track.isFavorite)
        }
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        viewModelScope.launch {
            repository.saveTrack(track)
            repository.addTrackToPlaylist(playlist.id, track.id)
        }
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
    }

    fun selectArtist(artist: Artist?) {
        _selectedArtist.value = artist
    }

    fun startSleepTimer(minutes: Int) {
        playerManager.startSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
