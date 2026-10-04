package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.Album
import com.example.data.model.Track
import com.example.ui.components.TrackListItem
import com.example.ui.theme.ARPrimary
import com.example.ui.theme.ARSurfaceCard

@Composable
fun AlbumsScreen(
    albums: List<Album>,
    selectedAlbum: Album?,
    currentTrack: Track?,
    isPlaying: Boolean,
    onAlbumSelect: (Album?) -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedAlbum == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("albums_screen")
        ) {
            Text(
                text = "Albums (${albums.size})",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "Curated full albums & timeless collections",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(albums) { album ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumSelect(album) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ARSurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            AsyncImage(
                                model = album.coverUrl,
                                contentDescription = album.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = album.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${album.artist} • ${album.year}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Album Detail
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("album_detail_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                IconButton(onClick = { onAlbumSelect(null) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Albums"
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = selectedAlbum.coverUrl,
                        contentDescription = selectedAlbum.title,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = selectedAlbum.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedAlbum.artist} • ${selectedAlbum.year}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${selectedAlbum.tracks.size} tracks",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ARPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (selectedAlbum.tracks.isNotEmpty()) {
                                onTrackClick(selectedAlbum.tracks.first(), selectedAlbum.tracks)
                            }
                        },
                        enabled = selectedAlbum.tracks.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = ARPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Album", color = MaterialTheme.colorScheme.onPrimary)
                    }

                    OutlinedButton(
                        onClick = {
                            if (selectedAlbum.tracks.isNotEmpty()) {
                                val shuffled = selectedAlbum.tracks.shuffled()
                                onTrackClick(shuffled.first(), shuffled)
                            }
                        },
                        enabled = selectedAlbum.tracks.isNotEmpty()
                    ) {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            items(selectedAlbum.tracks) { track ->
                TrackListItem(
                    track = track,
                    isPlaying = isPlaying,
                    isCurrent = track.id == currentTrack?.id,
                    onTrackClick = { onTrackClick(track, selectedAlbum.tracks) },
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
