package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ApkProject
import com.example.generator.AndroidProjectGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("APK Builder", appName)
  }

  @Test
  fun `verify AndroidProjectGenerator generates valid files`() {
    val project = ApkProject(
      appName = "App Gallery",
      packageName = "com.appgallery.games",
      websiteUrl = "https://html5games.com"
    )
    val files = AndroidProjectGenerator.generateProjectFiles(project)
    assertTrue("Should generate files", files.isNotEmpty())
    
    val manifest = files.firstOrNull { it.relativePath.contains("AndroidManifest.xml") }
    assertTrue("Manifest must exist", manifest != null)
    assertTrue(manifest!!.content.contains("com.appgallery.games"))

    val mainActivity = files.firstOrNull { it.relativePath.contains("MainActivity.kt") }
    assertTrue("MainActivity must exist", mainActivity != null)
    assertTrue(mainActivity!!.content.contains("https://html5games.com"))
  }
}
