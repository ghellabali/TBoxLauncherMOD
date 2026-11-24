package vad.dashing.tbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Slim TBox façade — connection/theme/net are idle without the UDP proxy. */
class TboxViewModel : ViewModel() {
    val tboxConnected: StateFlow<Boolean> = TboxRepository.tboxConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val netState: StateFlow<NetState> = TboxRepository.netState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NetState())

    val locValues: StateFlow<LocValues> = TboxRepository.locValues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocValues())

    val currentTheme: StateFlow<Int> = TboxRepository.currentTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 2)
}

fun valueToString(
    value: Any?,
    accuracy: Int = 1,
    booleanTrue: String? = null,
    booleanFalse: String? = null,
    default: String = "",
): String {
    if (value == null) return default
    return when (value) {
        is Int -> value.toString()
        is UInt -> value.toString()
        is Float, is Double -> when (accuracy) {
            0 -> String.format(LauncherStrings.language.locale, "%.0f", value)
            1 -> String.format(LauncherStrings.language.locale, "%.1f", value)
            2 -> String.format(LauncherStrings.language.locale, "%.2f", value)
            3 -> String.format(LauncherStrings.language.locale, "%.3f", value)
            4 -> String.format(LauncherStrings.language.locale, "%.4f", value)
            5 -> String.format(LauncherStrings.language.locale, "%.5f", value)
            6 -> String.format(LauncherStrings.language.locale, "%.6f", value)
            else -> String.format(LauncherStrings.language.locale, "%.1f", value)
        }
        is Boolean -> if (value) booleanTrue ?: launcherText(R.string.value_yes) else booleanFalse ?: launcherText(R.string.value_no)
        is String -> value
        else -> ""
    }
}
