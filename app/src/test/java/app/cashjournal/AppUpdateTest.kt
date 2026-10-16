package app.cashjournal

import app.cashjournal.data.update.GitHubAssetDto
import app.cashjournal.data.update.GitHubReleaseDto
import app.cashjournal.data.update.VersionComparator
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

import app.cashjournal.data.update.AppUpdateService
import java.io.File

class AppUpdateTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testVersionComparator() {
        // Newer minor/patch
        assertTrue(VersionComparator.isNewer("1.0.18", "v1.0.19"))
        assertTrue(VersionComparator.isNewer("1.0.18-dev", "v1.0.19-dev"))
        assertTrue(VersionComparator.isNewer("1.0.9", "v1.0.10"))
        assertTrue(VersionComparator.isNewer("1.0.18", "v2.0.0"))

        // Equal versions
        assertFalse(VersionComparator.isNewer("1.0.18", "v1.0.18"))
        assertFalse(VersionComparator.isNewer("1.0.18-dev", "v1.0.18-dev"))

        // Older versions
        assertFalse(VersionComparator.isNewer("1.0.19", "v1.0.18"))
        assertFalse(VersionComparator.isNewer("1.0.19-dev", "v1.0.18-dev"))
        assertFalse(VersionComparator.isNewer("2.0.0", "v1.9.9"))

        // Final vs dev release
        assertTrue(VersionComparator.isNewer("1.0.18-dev", "v1.0.18"))
        assertFalse(VersionComparator.isNewer("1.0.18", "v1.0.18-dev"))
    }

    @Test
    fun testGitHubReleaseDeserialization() {
        val jsonPayload = """
            {
              "tag_name": "v1.0.19-dev",
              "name": "v1.0.19-dev: Phase 16",
              "body": "### Features\n- GitHub OTA Updater\n- Account Categories",
              "published_at": "2026-10-16T12:00:00Z",
              "assets": [
                {
                  "name": "app-debug.apk",
                  "size": 17391506,
                  "browser_download_url": "https://github.com/CanYouSpeakEng/CashJournal/releases/download/v1.0.19-dev/app-debug.apk",
                  "content_type": "application/vnd.android.package-archive"
                },
                {
                  "name": "app-release.apk",
                  "size": 12234647,
                  "browser_download_url": "https://github.com/CanYouSpeakEng/CashJournal/releases/download/v1.0.19-dev/app-release.apk",
                  "content_type": "application/vnd.android.package-archive"
                }
              ]
            }
        """.trimIndent()

        val release = json.decodeFromString<GitHubReleaseDto>(jsonPayload)

        assertEquals("v1.0.19-dev", release.tagName)
        assertEquals("v1.0.19-dev: Phase 16", release.name)
        assertEquals(2, release.assets.size)
        assertEquals("app-debug.apk", release.assets[0].name)
        assertEquals(17391506L, release.assets[0].size)
        assertEquals("app-release.apk", release.assets[1].name)
        assertEquals(12234647L, release.assets[1].size)
    }

    @Test
    fun testAssetSelectionLogic() {
        val assets = listOf(
            GitHubAssetDto(
                name = "app-debug.apk",
                size = 17_000_000L,
                browserDownloadUrl = "https://github.com/download/app-debug.apk"
            ),
            GitHubAssetDto(
                name = "app-release.apk",
                size = 12_000_000L,
                browserDownloadUrl = "https://github.com/download/app-release.apk"
            )
        )

        // Debug build preference
        val debugAsset = assets.filter { it.name.endsWith(".apk") }
            .find { it.name.contains("debug", ignoreCase = true) }
        assertEquals("app-debug.apk", debugAsset?.name)

        // Release build preference
        val releaseAsset = assets.filter { it.name.endsWith(".apk") }
            .find { it.name.contains("release", ignoreCase = true) }
        assertEquals("app-release.apk", releaseAsset?.name)
    }

    @Test
    fun testCleanStaleUpdateApks() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "cashjournal_test_${System.currentTimeMillis()}")
        val updatesDir = File(tempDir, "updates").apply { mkdirs() }

        // 1. Equal to current version (just installed) -> should be deleted
        val equalApk = File(updatesDir, "CashJournal-1.0.21-dev.apk").apply { writeText("dummy content") }
        // 2. Older than current version (stale/superseded) -> should be deleted
        val olderApk = File(updatesDir, "CashJournal-1.0.19-dev.apk").apply { writeText("dummy content") }
        // 3. Newer than current version (ready to install) -> should be kept
        val newerApk = File(updatesDir, "CashJournal-1.0.22-dev.apk").apply { writeText("dummy content") }
        // 4. Stray / non-versioned file in updates cache -> should be deleted
        val strayApk = File(updatesDir, "unknown-patch.apk").apply { writeText("dummy content") }
        // 5. Non-apk file -> ignored / kept
        val nonApk = File(updatesDir, "metadata.json").apply { writeText("{}") }

        val service = AppUpdateService()
        service.cleanStaleUpdateApks(tempDir, currentVersion = "1.0.21-dev")

        assertFalse("Equal version APK should be deleted", equalApk.exists())
        assertFalse("Older version APK should be deleted", olderApk.exists())
        assertTrue("Newer version APK should be preserved", newerApk.exists())
        assertFalse("Stray APK should be deleted", strayApk.exists())
        assertTrue("Non-APK file should not be touched", nonApk.exists())

        tempDir.deleteRecursively()
    }
}
