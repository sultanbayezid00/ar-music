package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.YoutubeSearchedFor
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.components.TrackListItem
import com.example.ui.components.YouTubePolicyNoticeCard
import com.example.ui.theme.ARPrimary
import com.example.ui.theme.ARSecondary
import com.example.ui.theme.ARSurfaceCard
import com.example.ui.theme.ARWarning

@Composable
fun SearchScreen(
    searchQuery: String,
    searchResults: List<Track>,
    recentSearchQueries: List<String>,
    isSearching: Boolean,
    apiKey: String,
    currentTrack: Track?,
    isPlaying: Boolean,
    onQueryChange: (String) -> Unit,
    onPerformSearch: (String) -> Unit,
    onDeleteSearchQuery: (String) -> Unit,
    onClearSearchHistory: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onOpenApiKeyDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var selectedGenre by remember { mutableStateOf("All") }
    val genres = listOf("All", "বাংলা গান", "Arijit Singh", "Bollywood", "Lo-Fi Chill", "Pop Hits", "Rock", "Acoustic")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
            .testTag("search_screen")
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                onQueryChange(it)
                if (it.length >= 3) {
                    onPerformSearch(it)
                }
            },
            placeholder = { Text("Search songs, artists on YouTube...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = ARPrimary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onPerformSearch(searchQuery)
                }
            ),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ARSurfaceCard,
                unfocusedContainerColor = ARSurfaceCard,
                focusedBorderColor = ARPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("search_text_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // API Key Status Banner / Shortcut
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ARSurfaceCard)
                .clickable { onOpenApiKeyDialog() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.YoutubeSearchedFor,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (apiKey.isNotBlank()) "YouTube Data API v3 Connected" else "YouTube API Key: Curated Online Catalog",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (apiKey.isNotBlank()) ARPrimary else ARWarning
                )
            }
            Text(
                text = "Key Settings",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ARSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recent Search History Chips
        if (recentSearchQueries.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = ARPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recent Searches",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Clear",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clickable { onClearSearchHistory() }
                        .padding(4.dp)
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                items(recentSearchQueries) { query ->
                    InputChip(
                        selected = false,
                        onClick = {
                            onQueryChange(query)
                            onPerformSearch(query)
                        },
                        label = { Text(query, fontSize = 12.sp) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove search query",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onDeleteSearchQuery(query) }
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = ARSurfaceCard,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = null
                    )
                }
            }
        }

        // Genre Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                val isSelected = selectedGenre == genre
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedGenre = genre
                        val term = if (genre == "All") "" else genre
                        onQueryChange(term)
                        onPerformSearch(term)
                    },
                    label = { Text(genre, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ARPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = ARSurfaceCard
                    ),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ARPrimary)
            }
        }

        // Search Results List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            if (searchResults.isEmpty() && !isSearching) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "Type any song or artist to search YouTube" else "No YouTube tracks found for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        YouTubePolicyNoticeCard()
                    }
                }
            } else {
                item {
                    Text(
                        text = "Results (${searchResults.size} tracks)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                items(searchResults) { track ->
                    TrackListItem(
                        track = track,
                        isPlaying = isPlaying,
                        isCurrent = track.id == currentTrack?.id,
                        onTrackClick = { onTrackClick(track, searchResults) },
                        onFavoriteToggle = { onFavoriteToggle(track) },
                        onAddToPlaylist = { onAddToPlaylist(track) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    YouTubePolicyNoticeCard()
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
