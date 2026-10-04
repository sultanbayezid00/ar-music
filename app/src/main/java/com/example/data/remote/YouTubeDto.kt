package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(
    @Json(name = "items") val items: List<YouTubeSearchItem> = emptyList(),
    @Json(name = "nextPageToken") val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSearchItem(
    @Json(name = "id") val id: YouTubeId,
    @Json(name = "snippet") val snippet: YouTubeSnippet
)

@JsonClass(generateAdapter = true)
data class YouTubeId(
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "videoId") val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeSnippet(
    @Json(name = "title") val title: String = "",
    @Json(name = "channelTitle") val channelTitle: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "thumbnails") val thumbnails: YouTubeThumbnails? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnails(
    @Json(name = "medium") val medium: YouTubeThumbnailDetails? = null,
    @Json(name = "high") val high: YouTubeThumbnailDetails? = null,
    @Json(name = "default") val defaultThumb: YouTubeThumbnailDetails? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnailDetails(
    @Json(name = "url") val url: String = ""
)
