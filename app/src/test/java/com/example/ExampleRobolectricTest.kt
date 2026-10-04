package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Track
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("আর মিউজিক", appName)
  }

  @Test
  fun `verify track duration formatting`() {
    val track = Track(
      id = "yt_test",
      title = "Test Song",
      artist = "Test Artist",
      durationSeconds = 245
    )
    assertEquals("4:05", track.durationFormatted)
  }
}
