package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ARPrimary
import com.example.ui.theme.ARSurface

@Composable
fun ARTopBar(
    onSearchClick: () -> Unit,
    onApiKeyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ARSurface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("ar_top_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Logo
        Image(
            painter = painterResource(id = R.drawable.ar_music_logo),
            contentDescription = "AR Music Logo",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Branding
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AR Music",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• আর মিউজিক",
                    fontSize = 12.sp,
                    color = ARPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "Stream Official YouTube & Local Offline",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Search action
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.testTag("top_bar_search")
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Settings / API key action
        IconButton(
            onClick = onApiKeyClick,
            modifier = Modifier.testTag("top_bar_api_key")
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = "YouTube API Configuration",
                tint = ARPrimary
            )
        }
    }
}
