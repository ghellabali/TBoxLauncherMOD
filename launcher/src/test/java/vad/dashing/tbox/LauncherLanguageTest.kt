package vad.dashing.tbox

import android.app.Application
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import java.util.concurrent.TimeUnit
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import vad.dashing.tbox.ui.launcher.TopShadeTileSpec
import vad.dashing.tbox.ui.launcher.TopShadeTileType

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class LauncherLanguageTest {
    private val app: Application get() = RuntimeEnvironment.getApplication()

    @Before
    fun resetLanguage() {
        app.getSharedPreferences("launcher_language", 0).edit().clear().commit()
        LauncherStrings.initialize(app)
    }

    @Test
    fun choosingALanguageUpdatesResourcesAndSurvivesReinitialization() {
        val choices = listOf(
            LauncherLanguage.English to "Interface language",
            LauncherLanguage.French to "Langue de l’interface",
            LauncherLanguage.Arabic to "لغة الواجهة",
        )
        val original = app.resources.configuration.locales[0]
        for ((language, caption) in choices) {
            LauncherStrings.select(language)
            assertEquals(caption, launcherText(R.string.launcher_interface_language))
            LauncherStrings.initialize(app)
            assertEquals(language, LauncherStrings.language)
            val resources = LauncherStrings.resources()
            assertEquals(language.tag, resources.configuration.locales[0].language)
            assertEquals(View.LAYOUT_DIRECTION_LTR, resources.configuration.layoutDirection)
            assertEquals(original, app.resources.configuration.locales[0])
        }
    }

    @Test
    fun bothComposeHostsObserveLanguageChangesWithoutMovingTheDashboard() {
        val first = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val second = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val observed = mutableMapOf<String, Pair<String, LayoutDirection>>()
        val probe: @Composable (String) -> Unit = { host ->
            val label = stringResource(R.string.launcher_interface_language)
            val direction = LocalLayoutDirection.current
            SideEffect { observed[host] = label to direction }
        }
        try {
            first.get().setLauncherContent { probe("activity") }
            val view = ComposeView(second.get())
            second.get().setContentView(view)
            view.setLauncherContent { probe("window") }
            for ((language, label) in listOf(
                LauncherLanguage.English to "Interface language",
                LauncherLanguage.Arabic to "لغة الواجهة",
                LauncherLanguage.French to "Langue de l’interface",
            )) {
                LauncherStrings.select(language)
                shadowOf(Looper.getMainLooper()).idleFor(250, TimeUnit.MILLISECONDS)
                assertEquals(label to LayoutDirection.Ltr, observed["activity"])
                assertEquals(label to LayoutDirection.Ltr, observed["window"])
            }
        } finally {
            second.pause().stop().destroy()
            first.pause().stop().destroy()
        }
    }

    @Test
    fun changingLanguagePreservesStoredTileIdentityAndCustomCaptions() {
        val tile = TopShadeTileSpec("opt_trunk", "Багажник", TopShadeTileType.CAR_CONTROL, "trunk_door")
        val shortcut = TopShadeTileSpec("app_custom", "My shortcut", TopShadeTileType.SHORTCUT, "example.custom")
        for ((language, title) in listOf(
            LauncherLanguage.English to "Trunk",
            LauncherLanguage.French to "Coffre",
            LauncherLanguage.Arabic to "صندوق الأمتعة",
        )) {
            LauncherStrings.select(language)
            assertEquals(title, tile.displayTitle)
            assertEquals("trunk_door", tile.actionKey)
            assertEquals("My shortcut", shortcut.displayTitle)
        }
    }
}
