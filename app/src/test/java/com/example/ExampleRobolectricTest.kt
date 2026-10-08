package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.extension.BuiltInExtensions
import com.example.extension.ExtensionManifest
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
        assertEquals("MultiBrowser", appName)
    }

    @Test
    fun `test manifest parser V3 with Robolectric`() {
        val manifest = ExtensionManifest.fromJson(BuiltInExtensions.CAPTCHA_SOLVER_MANIFEST)
        assertEquals(3, manifest.manifestVersion)
        assertEquals("Auto Captcha Solver", manifest.name)
        assertEquals("1.4.0", manifest.version)
        assertTrue(manifest.contentScripts.isNotEmpty())
    }

    @Test
    fun `test insta 2fa manifest parser with Robolectric`() {
        val manifest = ExtensionManifest.fromJson(BuiltInExtensions.INSTA_2FA_MANIFEST)
        assertEquals(3, manifest.manifestVersion)
        assertEquals("Insta Auto 2FA & Auth Assistant", manifest.name)
        assertTrue(manifest.contentScripts.isNotEmpty())
    }
}
