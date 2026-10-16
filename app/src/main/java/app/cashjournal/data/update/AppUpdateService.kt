package app.cashjournal.data.update

import app.cashjournal.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class AppUpdateService(
    private val repoOwner: String = "CanYouSpeakEng",
    private val repoName: String = "CashJournal"
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val downloadClient by lazy {
        NetworkClient.client.newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    suspend fun fetchLatestRelease(
        currentVersion: String,
        isDebugBuild: Boolean
    ): Result<AppReleaseInfo> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "CashJournal-Android")
                .build()

            val response = NetworkClient.client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Failed to fetch release: HTTP ${response.code} ${response.message}")
                )
            }

            val body = response.body?.string() ?: return@withContext Result.failure(
                IOException("Empty response body from GitHub API")
            )

            val releaseDto = json.decodeFromString<GitHubReleaseDto>(body)
            val apkAsset = selectApkAsset(releaseDto.assets, isDebugBuild)
                ?: return@withContext Result.failure(
                    IOException("No suitable APK asset found in release ${releaseDto.tagName}")
                )

            val cleanVersion = releaseDto.tagName.trim().removePrefix("v").removePrefix("V")
            val isNewer = VersionComparator.isNewer(currentVersion, releaseDto.tagName)

            val info = AppReleaseInfo(
                tagName = releaseDto.tagName,
                versionName = cleanVersion,
                title = releaseDto.name ?: releaseDto.tagName,
                body = releaseDto.body.orEmpty(),
                apkDownloadUrl = apkAsset.browserDownloadUrl,
                apkSize = apkAsset.size,
                isNewer = isNewer
            )

            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadApk(
        downloadUrl: String,
        destinationFile: File,
        onProgress: (bytesDownloaded: Long, totalBytes: Long, progress: Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            destinationFile.parentFile?.mkdirs()
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "CashJournal-Android")
                .build()

            val response = downloadClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Download failed: HTTP ${response.code} ${response.message}")
                )
            }

            val responseBody = response.body ?: return@withContext Result.failure(
                IOException("Empty download body from server")
            )

            val totalBytes = responseBody.contentLength()
            var bytesDownloaded = 0L

            responseBody.byteStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesDownloaded += read

                        val progress = if (totalBytes > 0) {
                            (bytesDownloaded.toFloat() / totalBytes).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        onProgress(bytesDownloaded, totalBytes, progress)
                    }
                    output.flush()
                }
            }

            if (destinationFile.length() == 0L) {
                destinationFile.delete()
                return@withContext Result.failure(IOException("Downloaded APK file is empty"))
            }

            Result.success(destinationFile)
        } catch (e: Exception) {
            destinationFile.delete()
            Result.failure(e)
        }
    }

    private fun selectApkAsset(assets: List<GitHubAssetDto>, isDebugBuild: Boolean): GitHubAssetDto? {
        val apkAssets = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        if (apkAssets.isEmpty()) return null

        return if (isDebugBuild) {
            apkAssets.find { it.name.contains("debug", ignoreCase = true) }
                ?: apkAssets.firstOrNull()
        } else {
            apkAssets.find { it.name.contains("release", ignoreCase = true) }
                ?: apkAssets.firstOrNull()
        }
    }

    /**
     * Deletes stale APK update files from the internal cache directory.
     * Any APK whose version is older than or equal to currentVersion is removed.
     * Any unrecognized or stray APK files in the updates cache are also pruned.
     */
    fun cleanStaleUpdateApks(cacheDir: File, currentVersion: String) {
        try {
            val updatesDir = File(cacheDir, "updates")
            if (!updatesDir.exists() || !updatesDir.isDirectory) return

            val apkFiles = updatesDir.listFiles { file ->
                file.isFile && file.name.endsWith(".apk", ignoreCase = true)
            } ?: return

            val prefix = "CashJournal-"
            val suffix = ".apk"
            for (apk in apkFiles) {
                val isNamedProperly = apk.name.startsWith(prefix, ignoreCase = true) &&
                        apk.name.endsWith(suffix, ignoreCase = true)

                if (!isNamedProperly) {
                    apk.delete()
                    continue
                }

                val apkVersion = apk.name.removePrefix(prefix).removeSuffix(suffix)
                val isNewer = VersionComparator.isNewer(currentVersion, apkVersion)
                if (!isNewer) {
                    apk.delete()
                }
            }
        } catch (_: Exception) {
            // Non-critical cache cleanup failure ignored
        }
    }
}
