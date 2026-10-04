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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Artist
import com.example.data.model.Track
import com.example.ui.components.TrackListItem
import com.example.ui.theme.ARPrimary
import com.example.ui.theme.ARSurfaceCard

@Composable
fun ArtistsScreen(
    artists: List<Artist>,
    selectedArtist: Artist?,
    currentTrack: Track?,
    isPlaying: Boolean,
    onArtistSelect: (Artist?) -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedArtist == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("artists_screen")
        ) {
            Text(
                text = "Artists (${artists.size})",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "Discover top voices, producers, and musicians",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(artists) { artist ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onArtistSelect(artist) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = artist.imageUrl,
                            contentDescription = artist.name,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(ARSurfaceCard),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${artist.monthlyListeners} monthly",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    } else {
        // Artist Detail
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("artist_detail_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                IconButton(onClick = { onArtistSelect(null) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Artists"
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = selectedArtist.imageUrl,
                        contentDescription = selectedArtist.name,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = selectedArtist.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedArtist.monthlyListeners} monthly listeners",
                            style = MaterialTheme.typography.bodyMedium.copy(color = ARPrimary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Featured on AR Music",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (selectedArtist.topTracks.isNotEmpty()) {
                                onTrackClick(selectedArtist.topTracks.first(), selectedArtist.topTracks)
                            }
                        },
                        enabled = selectedArtist.topTracks.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = ARPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Top Songs", color = MaterialTheme.colorScheme.onPrimary)
                    }

                    OutlinedButton(
                        onClick = {
                            if (selectedArtist.topTracks.isNotEmpty()) {
                                val shuffled = selectedArtist.topTracks.shuffled()
                                onTrackClick(shuffled.first(), shuffled)
                            }
                        },
                        enabled = selectedArtist.topTracks.isNotEmpty()
                    ) {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Top Songs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(selectedArtist.topTracks) { track ->
                TrackListItem(
                    track = track,
                    isPlaying = isPlaying,
                    isCurrent = track.id == currentTrack?.id,
                    onTrackClick = { onTrackClick(track, selectedArtist.topTracks) },
                    onFavoriteToggle = { onFavoriteToggle(track) },
                    onAddToPlaylist = { onAddToPlaylist(track) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
