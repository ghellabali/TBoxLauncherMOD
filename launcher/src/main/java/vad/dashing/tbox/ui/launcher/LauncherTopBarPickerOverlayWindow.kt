package vad.dashing.tbox.ui.launcher

import androidx.compose.ui.res.stringResource
import vad.dashing.tbox.launcherText
import vad.dashing.tbox.setLauncherContent
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import vad.dashing.tbox.LauncherHomeActivity
import vad.dashing.tbox.LauncherHomeActivityHolder
import vad.dashing.tbox.R
import vad.dashing.tbox.ui.MyLifecycleOwner
import vad.dashing.tbox.ui.ProvideUiClickSounds
import vad.dashing.tbox.ui.theme.TboxAppTheme

internal data class LauncherTopBarItemDescriptor(
    val id: String,
    @androidx.annotation.StringRes val titleResource: Int,
    val isButton: Boolean,
    val iconRes: Int? = null,
) {
    val title: String get() = launcherText(titleResource)
}

internal object LauncherTopBarDescriptors {
    val ALL_INDICATORS = listOf(
        LauncherTopBarItemDescriptor("time", R.string.data_title_time_widget, isButton = false, iconRes = R.drawable.ic_launcher_trip_duration),
        LauncherTopBarItemDescriptor("date", R.string.label_date_and_weekday, isButton = false, iconRes = R.drawable.ic_notification),
        LauncherTopBarItemDescriptor("connectivity", R.string.label_connectivity_indicators, isButton = false, iconRes = R.drawable.ic_launcher_bluetooth),
        LauncherTopBarItemDescriptor("temp", R.string.data_title_outside_temperature, isButton = false, iconRes = R.drawable.ic_launcher_temp_up),
        LauncherTopBarItemDescriptor("inside_temp", R.string.label_cabin_temperature, isButton = false, iconRes = R.drawable.ic_launcher_temp_down),
        LauncherTopBarItemDescriptor("combined_temp", R.string.label_inside_outside_temperature, isButton = false, iconRes = R.drawable.ic_launcher_temp_combined),
        LauncherTopBarItemDescriptor("engine_temp", R.string.data_title_engine_temperature, isButton = false, iconRes = R.drawable.ic_launcher_engine_temp),
        LauncherTopBarItemDescriptor("gearbox_temp", R.string.label_transmission_temperature, isButton = false, iconRes = R.drawable.ic_launcher_gearbox_temp),
        LauncherTopBarItemDescriptor("fuel", R.string.label_fuel_range, isButton = false, iconRes = R.drawable.ic_launcher_fuel),
        LauncherTopBarItemDescriptor("avg_fuel", R.string.label_average_consumption, isButton = false, iconRes = R.drawable.ic_launcher_fuel_drop),
        LauncherTopBarItemDescriptor("trip_cost", R.string.label_trip_cost, isButton = false, iconRes = R.drawable.ic_launcher_trip_cost),
        LauncherTopBarItemDescriptor("trip_duration", R.string.label_trip_time, isButton = false, iconRes = R.drawable.ic_launcher_trip_duration),
        LauncherTopBarItemDescriptor("trip_distance", R.string.trips_distance, isButton = false, iconRes = R.drawable.ic_launcher_trip_distance),
        LauncherTopBarItemDescriptor("air_quality", R.string.label_air_quality_pm2_5, isButton = false, iconRes = R.drawable.ic_launcher_leaf),
        LauncherTopBarItemDescriptor("voltage", R.string.label_battery_voltage, isButton = false, iconRes = R.drawable.ic_launcher_battery),
        LauncherTopBarItemDescriptor("cpu", R.string.label_cpu_load, isButton = false, iconRes = R.drawable.ic_launcher_cpu),
        LauncherTopBarItemDescriptor("ram", R.string.label_memory, isButton = false, iconRes = R.drawable.ic_launcher_ram),
        LauncherTopBarItemDescriptor("gps", R.string.label_satellites_location, isButton = false, iconRes = R.drawable.ic_launcher_satellite),
        LauncherTopBarItemDescriptor("maintenance", R.string.label_distance_to_service, isButton = false, iconRes = R.drawable.ic_launcher_maintenance),
        LauncherTopBarItemDescriptor("vehicle_status", R.string.label_vehicle_status, isButton = false, iconRes = R.drawable.ic_launcher_vehicle_status),
        LauncherTopBarItemDescriptor("camd_rec", R.string.camd_recorder_title, isButton = false, iconRes = R.drawable.ic_camd_rec_active),
    )

    val ALL_BUTTONS = listOf(
        LauncherTopBarItemDescriptor("headlights", R.string.label_headlight_switch, isButton = true, iconRes = R.drawable.ic_widget_headlights),
        LauncherTopBarItemDescriptor("trunk", R.string.label_trunk_double_tap, isButton = true, iconRes = R.drawable.ic_launcher_trunk),
        LauncherTopBarItemDescriptor("hdc", R.string.label_hill_descent_assist, isButton = true, iconRes = R.drawable.ic_launcher_hdc),
        LauncherTopBarItemDescriptor("theme", R.string.label_theme_day_night, isButton = true, iconRes = R.drawable.ic_theme_sun),
        LauncherTopBarItemDescriptor("mirrors", R.string.launcher_vs_mirror_autofold, isButton = true, iconRes = R.drawable.ic_launcher_mirror),
        LauncherTopBarItemDescriptor("autohold", R.string.launcher_vs_avh, isButton = true),
        LauncherTopBarItemDescriptor("pdc", R.string.label_parking_sensors, isButton = true, iconRes = R.drawable.ic_widget_parking_radar),
        LauncherTopBarItemDescriptor("rear_fog", R.string.label_rear_fog_light, isButton = true, iconRes = R.drawable.ic_launcher_rear_fog),
        LauncherTopBarItemDescriptor("avm", R.string.label_360_cameras, isButton = true, iconRes = R.drawable.ic_launcher_avm),
        LauncherTopBarItemDescriptor("spacer", R.string.label_divider, isButton = true),
        LauncherTopBarItemDescriptor("spacer_small", R.string.label_small_divider, isButton = true),
    )
}

/**
 * System overlay window for the top status bar item selection menu.
 */
internal object LauncherTopBarPickerOverlayWindow {
    private const val TAG = "LauncherTopBarPickerOverlay"

    val isShowingState = androidx.compose.runtime.mutableStateOf(false)

    @Volatile
    private var composeView: ComposeView? = null
    private var lifecycleOwner: MyLifecycleOwner? = null
    private var windowManager: WindowManager? = null
    private var dismissCallback: (() -> Unit)? = null

    fun isShowing(): Boolean = composeView != null

    fun show(
        context: Context,
        topBarHeightDp: Int,
        onSelectItem: (String) -> Unit,
        onDismiss: () -> Unit,
    ): Boolean {
        hide()
        val activity = LauncherHomeActivityHolder.instance ?: (context as? LauncherHomeActivity)
            ?: return false
        if (activity.isFinishing || activity.isDestroyed) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            Log.w(TAG, "SYSTEM_ALERT_WINDOW is not granted")
            return false
        }

        val owner = MyLifecycleOwner().also {
            it.setCurrentState(Lifecycle.State.CREATED)
            it.setCurrentState(Lifecycle.State.STARTED)
            it.setCurrentState(Lifecycle.State.RESUMED)
        }
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val metrics = activity.resources.displayMetrics
        val density = metrics.density
        val topOffsetPx = ((topBarHeightDp + 50) * density).toInt()
        val pickerWidthPx = ((500 * density).toInt()).coerceAtMost(metrics.widthPixels - (32 * density).toInt())

        val params = WindowManager.LayoutParams(
            pickerWidthPx,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = topOffsetPx
            dimAmount = 0.40f
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        }

        val dismiss = {
            onDismiss()
            hide()
        }

        val activeItems = LauncherAppConfigStore.topBarItems(activity) + LauncherAppConfigStore.topBarRightItems(activity)

        val view = ComposeView(activity).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setOnKeyListener { _, keyCode, event ->
                if (keyCode != KeyEvent.KEYCODE_BACK) return@setOnKeyListener false
                if (event.action == KeyEvent.ACTION_UP) dismiss()
                true
            }
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    dismiss()
                    true
                } else {
                    false
                }
            }
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(activity)
            setLauncherContent {
                ProvideUiClickSounds {
                    val isDark = LauncherThemeState.darkTheme
                    TboxAppTheme(theme = if (isDark) 2 else 1) {
                        LauncherTopBarPickerContent(
                            activeItems = activeItems,
                            onSelectItem = { itemId ->
                                onSelectItem(itemId)
                                dismiss()
                            },
                            onDismiss = dismiss,
                        )
                    }
                }
            }
        }

        return runCatching {
            lifecycleOwner = owner
            windowManager = wm
            dismissCallback = onDismiss
            wm.addView(view, params)
            composeView = view
            view.requestFocus()
            runCatching { LauncherOverlayBar.bringToFront() }
            runCatching { LauncherEdgeGestureOverlay.bringToFront() }
            isShowingState.value = true
            Log.w(TAG, "top bar picker overlay shown")
            true
        }.onFailure {
            Log.e(TAG, "Unable to add top bar picker overlay", it)
            isShowingState.value = false
            lifecycleOwner = null
            windowManager = null
            dismissCallback = null
            runCatching { owner.setCurrentState(Lifecycle.State.DESTROYED) }
            runCatching { owner.clear() }
        }.getOrDefault(false)
    }

    fun hide() {
        val view = composeView ?: return
        composeView = null
        isShowingState.value = false
        val cb = dismissCallback
        dismissCallback = null
        runCatching { windowManager?.removeViewImmediate(view) }
            .onFailure { Log.w(TAG, "Unable to remove top bar picker overlay", it) }
        windowManager = null
        lifecycleOwner?.let { owner ->
            owner.setCurrentState(Lifecycle.State.DESTROYED)
            owner.clear()
        }
        lifecycleOwner = null
        cb?.invoke()
        Log.w(TAG, "top bar picker overlay hidden")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LauncherTopBarPickerContent(
    activeItems: List<String>,
    onSelectItem: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val isDark = LauncherThemeState.darkTheme
    val popupBg = if (isDark) Color(0xF218202A) else Color(0xF2E5E7EB)
    val popupBorder = if (isDark) Color(0x5038BDF8) else Color(0x3094A3B8)
    val cardBg = if (isDark) Color(0xFF222E3C) else Color(0xFFD1D5DB)
    val buttonCircleBg = if (isDark) Color(0xFF2D3B4E) else Color(0xFFCBD5E1)
    val buttonCircleBorder = if (isDark) Color(0x6038BDF8) else Color(0x4094A3B8)

    val context = LocalContext.current
    val isCamdInstalled = remember(context) { vad.dashing.tbox.camd.CamdSocketClient.isInstalled(context) }

    val availableButtons = LauncherTopBarDescriptors.ALL_BUTTONS.filter { desc ->
        desc.id.startsWith("spacer") || !activeItems.contains(desc.id)
    }
    val availableIndicators = LauncherTopBarDescriptors.ALL_INDICATORS.filter { desc ->
        if (desc.id == "camd_rec" && !isCamdInstalled) false else !activeItems.contains(desc.id)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = popupBg,
        border = BorderStroke(1.dp, popupBorder),
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .wrapContentWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = launcherText(R.string.label_add_to_status_bar),
                    color = LauncherColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = launcherText(R.string.launcher_paint_picker_close), tint = LauncherColors.TextMuted)
                }
            }

            val context = LocalContext.current
            var cellular4gVisible by remember {
                mutableStateOf(LauncherAppConfigStore.topBarCellularNetworkTypeVisible(context))
            }
            var modemCellularVisible by remember {
                mutableStateOf(LauncherAppConfigStore.topBarModemCellularVisible(context))
            }
            var borderEnabled by remember {
                mutableStateOf(LauncherAppConfigStore.topBarIndicatorsBorderEnabled(context))
            }
            var timeSignalBorderEnabled by remember {
                mutableStateOf(LauncherAppConfigStore.topBarTimeSignalBorderEnabled(context))
            }

            // Toggle for 4G network indicator TBox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardBg)
                    .clickable {
                        val next = !cellular4gVisible
                        cellular4gVisible = next
                        LauncherAppConfigStore.setTopBarCellularNetworkTypeVisible(context, next)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = launcherText(R.string.label_4g_indicator_tbox),
                    color = LauncherColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Switch(
                    checked = cellular4gVisible,
                    onCheckedChange = { checked ->
                        cellular4gVisible = checked
                        LauncherAppConfigStore.setTopBarCellularNetworkTypeVisible(context, checked)
                    },
                    colors = launcherSwitchColors(),
                )
            }

            // Toggle for 4G network indicator Wi-Fi modem (TBM API)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardBg)
                    .clickable {
                        val next = !modemCellularVisible
                        modemCellularVisible = next
                        LauncherAppConfigStore.setTopBarModemCellularVisible(context, next)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = launcherText(R.string.label_4g_indicator_tbm_wi_fi_modem),
                    color = LauncherColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Switch(
                    checked = modemCellularVisible,
                    onCheckedChange = { checked ->
                        modemCellularVisible = checked
                        LauncherAppConfigStore.setTopBarModemCellularVisible(context, checked)
                    },
                    colors = launcherSwitchColors(),
                )
            }

            // Toggle for indicators border
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardBg)
                    .clickable {
                        val next = !borderEnabled
                        borderEnabled = next
                        LauncherAppConfigStore.setTopBarIndicatorsBorderEnabled(context, next)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = launcherText(R.string.label_indicator_outlines),
                    color = LauncherColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Switch(
                    checked = borderEnabled,
                    onCheckedChange = { checked ->
                        borderEnabled = checked
                        LauncherAppConfigStore.setTopBarIndicatorsBorderEnabled(context, checked)
                    },
                    colors = launcherSwitchColors(),
                )
            }

            // Toggle for time and signal border
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardBg)
                    .clickable {
                        val next = !timeSignalBorderEnabled
                        timeSignalBorderEnabled = next
                        LauncherAppConfigStore.setTopBarTimeSignalBorderEnabled(context, next)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = launcherText(R.string.label_outlines_around_time_and_signal),
                    color = LauncherColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Switch(
                    checked = timeSignalBorderEnabled,
                    onCheckedChange = { checked ->
                        timeSignalBorderEnabled = checked
                        LauncherAppConfigStore.setTopBarTimeSignalBorderEnabled(context, checked)
                    },
                    colors = launcherSwitchColors(),
                )
            }

            // Section 1: Кнопки
            Text(
                text = launcherText(R.string.label_buttons),
                color = LauncherColors.TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )

            if (availableButtons.isEmpty()) {
                Text(
                    text = launcherText(R.string.label_all_buttons_have_been_added),
                    color = LauncherColors.TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    availableButtons.forEach { desc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardBg)
                                .clickable { onSelectItem(desc.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(buttonCircleBg)
                                    .border(1.dp, buttonCircleBorder, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (desc.iconRes != null) {
                                    Image(
                                        painter = painterResource(desc.iconRes),
                                        contentDescription = desc.title,
                                        modifier = Modifier.size(18.dp),
                                        colorFilter = ColorFilter.tint(LauncherColors.TextPrimary),
                                    )
                                } else if (desc.id == "autohold") {
                                    Text(
                                        text = stringResource(R.string.badge_hold),
                                        color = LauncherColors.TextPrimary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = TextStyle(
                                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        ),
                                    )
                                } else if (desc.id == "spacer") {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(LauncherColors.TextPrimary),
                                    )
                                } else if (desc.id == "spacer_small") {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(LauncherColors.TextPrimary),
                                    )
                                }
                            }
                            Text(
                                text = desc.title,
                                color = LauncherColors.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            // Separator line
            HorizontalDivider(
                color = popupBorder.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp),
            )

            // Section 2: Показатели
            Text(
                text = launcherText(R.string.label_indicators),
                color = LauncherColors.TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )

            if (availableIndicators.isEmpty()) {
                Text(
                    text = launcherText(R.string.label_all_indicators_have_been_added),
                    color = LauncherColors.TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    availableIndicators.forEach { desc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardBg)
                                .clickable { onSelectItem(desc.id) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            desc.iconRes?.let { res ->
                                Image(
                                    painter = painterResource(res),
                                    contentDescription = desc.title,
                                    modifier = Modifier.size(18.dp),
                                    colorFilter = ColorFilter.tint(LauncherColors.TextPrimary),
                                )
                            }
                            Text(
                                text = desc.title,
                                color = LauncherColors.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}
