package app.cashjournal.data.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name")
    val tagName: String,
    val name: String? = null,
    val body: String? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    val assets: List<GitHubAssetDto> = emptyList()
)

@Serializable
data class GitHubAssetDto(
    val name: String,
    val size: Long,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    @SerialName("content_type")
    val contentType: String? = null
)

data class AppReleaseInfo(
    val tagName: String,
    val versionName: String,
    val title: String,
    val body: String,
    val apkDownloadUrl: String,
    val apkSize: Long,
    val isNewer: Boolean
)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(val info: AppReleaseInfo) : UpdateState()
    data class Downloading(
        val info: AppReleaseInfo,
        val progress: Float,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : UpdateState()
    data class ReadyToInstall(
        val info: AppReleaseInfo,
        val apkFile: File
    ) : UpdateState()
    object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

object VersionComparator {

    /**
     * Compares two semantic version strings (e.g. "1.0.18-dev" and "v1.0.19-dev").
     * Returns true if latest is strictly newer than current.
     */
    fun isNewer(currentVersion: String, latestVersion: String): Boolean {
        val currentClean = currentVersion.trim().removePrefix("v").removePrefix("V")
        val latestClean = latestVersion.trim().removePrefix("v").removePrefix("V")

        val currentIsDev = currentClean.contains("-dev", ignoreCase = true)
        val latestIsDev = latestClean.contains("-dev", ignoreCase = true)

        val currentCore = currentClean.substringBefore("-")
        val latestCore = latestClean.substringBefore("-")

        val currentParts = currentCore.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latestCore.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLen) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }

        // Numeric parts are equal (e.g. 1.0.18 vs 1.0.18)
        // If current is -dev and latest is final, latest is newer
        if (currentIsDev && !latestIsDev) {
            return true
        }

        return false
    }
}
