package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Track
import com.example.ui.MusicViewModel
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.ARTopBar
import com.example.ui.components.MiniPlayer
import com.example.ui.navigation.ARNavScreen
import com.example.ui.theme.ARPrimary
import com.example.ui.theme.ARSurface

@Composable
fun MainAppScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val historyTracks by viewModel.historyTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val selectedPlaylistTracks by viewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()
    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val selectedArtist by viewModel.selectedArtist.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val recentSearchQueries by viewModel.recentSearchQueries.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()

    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(ARNavScreen.HOME) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var trackForPlaylist by remember { mutableStateOf<Track?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Tablet / Desktop Canonical Layout: NavigationRail on left
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = ARSurface,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ar_music_logo),
                                contentDescription = "AR Music",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "AR Music",
                                fontSize = 11.sp,
                                color = ARPrimary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    ARNavScreen.entries.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = {
                                currentScreen = screen
                                if (screen == ARNavScreen.PLAYLISTS) viewModel.selectPlaylist(null)
                                if (screen == ARNavScreen.ALBUMS) viewModel.selectAlbum(null)
                                if (screen == ARNavScreen.ARTISTS) viewModel.selectArtist(null)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title, fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = ARPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_rail_${screen.name.lowercase()}")
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { showApiKeyDialog = true },
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "API Key", tint = ARPrimary)
                    }
                }

                // Main Content Area for Wide Screens
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ARTopBar(
                            onSearchClick = { currentScreen = ARNavScreen.SEARCH },
                            onApiKeyClick = { showApiKeyDialog = true }
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            when (currentScreen) {
                                ARNavScreen.HOME -> HomeScreen(
                                    tracks = allTracks,
                                    playlists = playlists,
                                    albums = viewModel.albums,
                                    artists = viewModel.artists,
                                    currentTrack = playerState.currentTrack,
                                    isPlaying = playerState.isPlaying,
                                    onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                    onPlaylistClick = {
                                        viewModel.selectPlaylist(it)
                                        currentScreen = ARNavScreen.PLAYLISTS
                                    },
                                    onAlbumClick = {
                                        viewModel.selectAlbum(it)
                                        currentScreen = ARNavScreen.ALBUMS
                                    },
                                    onArtistClick = {
                                        viewModel.selectArtist(it)
                                        currentScreen = ARNavScreen.ARTISTS
                                    },
                                    onNavigateToSearch = { currentScreen = ARNavScreen.SEARCH },
                                    onNavigateToLibrary = { currentScreen = ARNavScreen.LIBRARY },
                                    onAddToPlaylist = { trackForPlaylist = it }
                                )
                                ARNavScreen.SEARCH -> SearchScreen(
                                    searchQuery = searchQuery,
                                    searchResults = searchResults,
                                    recentSearchQueries = recentSearchQueries,
                                    isSearching = isSearching,
                                    apiKey = apiKey,
                                    currentTrack = playerState.currentTrack,
                                    isPlaying = playerState.isPlaying,
                                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                                    onPerformSearch = { viewModel.performSearch(it) },
                                    onDeleteSearchQuery = { viewModel.deleteSearchQuery(it) },
                                    onClearSearchHistory = { viewModel.clearSearchHistory() },
                                    onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                    onAddToPlaylist = { trackForPlaylist = it },
                                    onOpenApiKeyDialog = { showApiKeyDialog = true }
                                )
                                ARNavScreen.LIBRARY -> LibraryScreen(
                                    favoriteTracks = favoriteTracks,
                                    historyTracks = historyTracks,
                                    playlists = playlists,
                                    currentTrack = playerState.currentTrack,
                                    isPlaying = playerState.isPlaying,
                                    onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                    onAddToPlaylist = { trackForPlaylist = it },
                                    onNavigateToPlaylists = { currentScreen = ARNavScreen.PLAYLISTS }
                                )
                                ARNavScreen.PLAYLISTS -> PlaylistsScreen(
                                    playlists = playlists,
                                    selectedPlaylist = selectedPlaylist,
                                    playlistTracks = selectedPlaylistTracks,
                                    currentTrack = playerState.currentTrack,
                                    isPlaying = playerState.isPlaying,
                                    onPlaylistSelect = { viewModel.selectPlaylist(it) },
                                    onCreatePlaylist = { n, d -> viewModel.createPlaylist(n, d) },
                                    onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                    onAddToPlaylist = { trackForPlaylist = it }
                                )
                                ARNavScreen.ALBUMS -> AlbumsScreen(
                                    albums = viewModel.albums,
                                    selectedAlbum = selectedAlbum,
                                    currentTrack = playerState.currentTrack,
                                    isPlaying = playerState.isPlaying,
                                    onAlbumSelect = { viewModel.selectAlbum(it) },
                                    onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                    onAddToPlaylist = { trackForPlaylist = it }
                                )
                                ARNavScreen.ARTISTS -> ArtistsScreen(
                                    artists = viewModel.artists,
                                    selectedArtist = selectedArtist,
                                    currentTrack = playerState.currentTrack,
                                    isPlaying = playerState.isPlaying,
                                    onArtistSelect = { viewModel.selectArtist(it) },
                                    onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                    onAddToPlaylist = { trackForPlaylist = it }
                                )
                            }
                        }

                        // Docked MiniPlayer for Wide Screens
                        if (playerState.currentTrack != null) {
                            MiniPlayer(
                                playerState = playerState,
                                playerManager = viewModel.playerManager,
                                onExpandClick = { viewModel.playerManager.setNowPlayingExpanded(true) },
                                onPlayPauseClick = { viewModel.playerManager.togglePlayPause() },
                                onNextClick = { viewModel.playerManager.next() },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Handheld / Mobile Layout: Scaffold with TopBar and Bottom NavigationBar
            Scaffold(
                topBar = {
                    ARTopBar(
                        onSearchClick = { currentScreen = ARNavScreen.SEARCH },
                        onApiKeyClick = { showApiKeyDialog = true }
                    )
                },
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .background(ARSurface)
                            .navigationBarsPadding()
                    ) {
                        // Floating MiniPlayer above NavigationBar
                        if (playerState.currentTrack != null) {
                            MiniPlayer(
                                playerState = playerState,
                                playerManager = viewModel.playerManager,
                                onExpandClick = { viewModel.playerManager.setNowPlayingExpanded(true) },
                                onPlayPauseClick = { viewModel.playerManager.togglePlayPause() },
                                onNextClick = { viewModel.playerManager.next() }
                            )
                        }

                        NavigationBar(
                            containerColor = ARSurface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            ARNavScreen.entries.forEach { screen ->
                                val isSelected = currentScreen == screen
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        currentScreen = screen
                                        if (screen == ARNavScreen.PLAYLISTS) viewModel.selectPlaylist(null)
                                        if (screen == ARNavScreen.ALBUMS) viewModel.selectAlbum(null)
                                        if (screen == ARNavScreen.ARTISTS) viewModel.selectArtist(null)
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = { Text(screen.title, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        indicatorColor = ARPrimary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        ARNavScreen.HOME -> HomeScreen(
                            tracks = allTracks,
                            playlists = playlists,
                            albums = viewModel.albums,
                            artists = viewModel.artists,
                            currentTrack = playerState.currentTrack,
                            isPlaying = playerState.isPlaying,
                            onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onPlaylistClick = {
                                viewModel.selectPlaylist(it)
                                currentScreen = ARNavScreen.PLAYLISTS
                            },
                            onAlbumClick = {
                                viewModel.selectAlbum(it)
                                currentScreen = ARNavScreen.ALBUMS
                            },
                            onArtistClick = {
                                viewModel.selectArtist(it)
                                currentScreen = ARNavScreen.ARTISTS
                            },
                            onNavigateToSearch = { currentScreen = ARNavScreen.SEARCH },
                            onNavigateToLibrary = { currentScreen = ARNavScreen.LIBRARY },
                            onAddToPlaylist = { trackForPlaylist = it }
                        )
                        ARNavScreen.SEARCH -> SearchScreen(
                            searchQuery = searchQuery,
                            searchResults = searchResults,
                            recentSearchQueries = recentSearchQueries,
                            isSearching = isSearching,
                            apiKey = apiKey,
                            currentTrack = playerState.currentTrack,
                            isPlaying = playerState.isPlaying,
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            onPerformSearch = { viewModel.performSearch(it) },
                            onDeleteSearchQuery = { viewModel.deleteSearchQuery(it) },
                            onClearSearchHistory = { viewModel.clearSearchHistory() },
                            onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onAddToPlaylist = { trackForPlaylist = it },
                            onOpenApiKeyDialog = { showApiKeyDialog = true }
                        )
                        ARNavScreen.LIBRARY -> LibraryScreen(
                            favoriteTracks = favoriteTracks,
                            historyTracks = historyTracks,
                            playlists = playlists,
                            currentTrack = playerState.currentTrack,
                            isPlaying = playerState.isPlaying,
                            onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onAddToPlaylist = { trackForPlaylist = it },
                            onNavigateToPlaylists = { currentScreen = ARNavScreen.PLAYLISTS }
                        )
                        ARNavScreen.PLAYLISTS -> PlaylistsScreen(
                            playlists = playlists,
                            selectedPlaylist = selectedPlaylist,
                            playlistTracks = selectedPlaylistTracks,
                            currentTrack = playerState.currentTrack,
                            isPlaying = playerState.isPlaying,
                            onPlaylistSelect = { viewModel.selectPlaylist(it) },
                            onCreatePlaylist = { n, d -> viewModel.createPlaylist(n, d) },
                            onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onAddToPlaylist = { trackForPlaylist = it }
                        )
                        ARNavScreen.ALBUMS -> AlbumsScreen(
                            albums = viewModel.albums,
                            selectedAlbum = selectedAlbum,
                            currentTrack = playerState.currentTrack,
                            isPlaying = playerState.isPlaying,
                            onAlbumSelect = { viewModel.selectAlbum(it) },
                            onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onAddToPlaylist = { trackForPlaylist = it }
                        )
                        ARNavScreen.ARTISTS -> ArtistsScreen(
                            artists = viewModel.artists,
                            selectedArtist = selectedArtist,
                            currentTrack = playerState.currentTrack,
                            isPlaying = playerState.isPlaying,
                            onArtistSelect = { viewModel.selectArtist(it) },
                            onTrackClick = { t, q -> viewModel.playTrack(t, q) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onAddToPlaylist = { trackForPlaylist = it }
                        )
                    }
                }
            }
        }

        // Full Screen Now Playing Screen Animated Overlay
        AnimatedVisibility(
            visible = playerState.isNowPlayingExpanded && playerState.currentTrack != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            NowPlayingScreen(
                playerState = playerState,
                playerManager = viewModel.playerManager,
                onDismiss = { viewModel.playerManager.setNowPlayingExpanded(false) },
                onFavoriteToggle = { viewModel.toggleFavorite(it) }
            )
        }

        // Dialogs
        if (showApiKeyDialog) {
            ApiKeyDialog(
                currentKey = apiKey,
                onSaveKey = { viewModel.setApiKey(it) },
                onDismiss = { showApiKeyDialog = false }
            )
        }

        trackForPlaylist?.let { track ->
            AddToPlaylistDialog(
                track = track,
                playlists = playlists,
                onSelectPlaylist = { pl -> viewModel.addTrackToPlaylist(pl, track) },
                onCreateNewPlaylist = {
                    currentScreen = ARNavScreen.PLAYLISTS
                },
                onDismiss = { trackForPlaylist = null }
            )
        }
    }
}
