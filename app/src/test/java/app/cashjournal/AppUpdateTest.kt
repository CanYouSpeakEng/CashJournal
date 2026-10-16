package app.cashjournal

import app.cashjournal.data.update.GitHubAssetDto
import app.cashjournal.data.update.GitHubReleaseDto
import app.cashjournal.data.update.VersionComparator
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
