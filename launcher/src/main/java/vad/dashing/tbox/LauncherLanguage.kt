package vad.dashing.tbox

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

internal enum class LauncherLanguage(val tag: String, val nativeName: String) {
    English("en", "English"),
    French("fr", "Français"),
    Arabic("ar", "العربية"),
    Russian("ru", "Русский");

    val locale: Locale = Locale.forLanguageTag(tag)

    companion object {
        val supported = if (BuildConfig.FLAVOR == "ru") entries else listOf(English, French, Arabic)
        val default = if (BuildConfig.FLAVOR == "ru") Russian else English
    }
}

internal object LauncherStrings {
    private const val PREFERENCES = "launcher_language"
    private const val LANGUAGE = "language"
    private lateinit var app: Context
    private data class Selection(val language: LauncherLanguage, val resources: Resources)
    private var current by mutableStateOf<Selection?>(null)
    val language: LauncherLanguage get() = current?.language ?: LauncherLanguage.default

    fun initialize(context: Context) {
        app = context.applicationContext
        val saved = app.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(LANGUAGE, null)
        val initial = LauncherLanguage.supported.firstOrNull { it.tag == saved } ?: LauncherLanguage.default
        current = Selection(initial, resourcesFor(app, initial))
    }

    fun select(value: LauncherLanguage) {
        if (language == value) return
        val next = Selection(value, resourcesFor(app, value))
        app.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putString(LANGUAGE, value.tag).apply()
        current = next
    }

    fun resourcesFor(context: Context, value: LauncherLanguage): Resources {
        val override = Configuration().apply {
            setLocale(value.locale)
            // Left/right indicate physical vehicle positions throughout the dashboard.
            setLayoutDirection(Locale.ENGLISH)
        }
        return context.createConfigurationContext(override).resources
    }

    fun resources(): Resources = checkNotNull(current) { "Launcher language is not initialized" }.resources
}

internal fun launcherText(@StringRes id: Int): String = LauncherStrings.resources().getString(id)

internal fun launcherText(@StringRes id: Int, vararg args: Any?): String =
    LauncherStrings.resources().getString(id, *args)

@Composable
private fun LauncherLanguageContent(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val language = LauncherStrings.language
    val resources = remember(context, configuration, language) { LauncherStrings.resourcesFor(context, language) }
    CompositionLocalProvider(
        LocalResources provides resources,
        LocalConfiguration provides resources.configuration,
        LocalLayoutDirection provides LayoutDirection.Ltr,
        content = content,
    )
}

internal fun ComponentActivity.setLauncherContent(content: @Composable () -> Unit) {
    setContent { LauncherLanguageContent(content) }
}

internal fun ComposeView.setLauncherContent(content: @Composable () -> Unit) {
    setContent { LauncherLanguageContent(content) }
}
