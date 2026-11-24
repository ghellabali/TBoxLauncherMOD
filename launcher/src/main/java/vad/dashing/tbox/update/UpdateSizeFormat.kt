package vad.dashing.tbox.update

import vad.dashing.tbox.launcherText
import vad.dashing.tbox.R
import java.util.Locale
import kotlin.math.roundToInt

fun formatApkSizeMegabytes(bytes: Long): String {
    if (bytes <= 0L) return "0"
    val megabytes = bytes / (1024.0 * 1024.0)
    return if (megabytes >= 10.0) {
        megabytes.roundToInt().toString()
    } else {
        String.format(Locale.US, "%.1f", megabytes)
    }
}

fun formatDownloadSpeed(bytesPerSecond: Long): String {
    if (bytesPerSecond <= 0L) return launcherText(R.string.label_0_kb_s)
    val kibibytes = bytesPerSecond / 1024.0
    if (kibibytes < 1024.0) {
        return launcherText(R.string.label_kb_s, kibibytes.roundToInt().coerceAtLeast(1))
    }
    val mebibytes = kibibytes / 1024.0
    return if (mebibytes >= 10.0) {
        launcherText(R.string.label_mb_s, mebibytes.roundToInt())
    } else {
        String.format(Locale.US, launcherText(R.string.label_mb_s_2), mebibytes)
    }
}

fun formatDownloadEta(seconds: Long): String {
    val totalSeconds = seconds.coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val remainingSeconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, remainingSeconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds)
    }
}
