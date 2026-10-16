package app.cashjournal.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import app.cashjournal.BuildConfig

object AppVersionProvider {

    /**
     * Dynamically queries the installed package manifest via PackageManager to retrieve
     * the runtime version name. This prevents compile-time constant inlining issues where
     * Kotlin incremental builds might reuse stale BuildConfig literals.
     */
    fun getVersionName(context: Context): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName ?: BuildConfig.VERSION_NAME
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: BuildConfig.VERSION_NAME
            }
        } catch (e: Exception) {
            BuildConfig.VERSION_NAME
        }
    }
}
