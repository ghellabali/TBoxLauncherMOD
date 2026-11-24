package vad.dashing.tbox.ui.launcher

import vad.dashing.tbox.launcherText
import vad.dashing.tbox.R
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class TopShadeTileType {
    CAR_CONTROL,
    SYSTEM_TOGGLE,
    TELEMETRY,
    SHORTCUT,
}

data class TopShadeTileSpec(
    val id: String,
    val title: String,
    val type: TopShadeTileType,
    val actionKey: String,
    val iconResName: String? = null,
    val spanX: Int = 1,
    val spanY: Int = 1,
    val col: Int = -1,
    val row: Int = -1,
) {
    val displayTitle: String
        get() = builtInTileTitle(actionKey)?.let { launcherText(it) } ?: title
}

@androidx.annotation.StringRes
private fun builtInTileTitle(action: String): Int? =
    when (action) {
        "acc" -> R.string.label_adaptive_cruise_acc
        "adas_dist" -> R.string.label_acc_distance
        "avh" -> R.string.label_auto_hold_hld
        "avm_camera" -> R.string.label_360_cameras
        "brightness" -> R.string.launcher_vs_brightness
        "bsd" -> R.string.label_blind_spots_bsd
        "car_dialer" -> R.string.label_phone
        "dcl_log" -> R.string.launcher_dcl_logs_button_title
        "dow" -> R.string.label_door_warning_dow
        "drive_mode" -> R.string.car_settings_drive_mode_title
        "esc_off" -> R.string.label_esp_stability_control
        "fcw" -> R.string.label_fcw_distance
        "front_defrost" -> R.string.label_windshield_defogger
        "hdc" -> R.string.label_hill_descent_control_hdc
        "headlights" -> R.string.label_headlight_switch
        "headrest_speaker" -> R.string.label_headrest
        "hma" -> R.string.label_automatic_high_beams_ihbc
        "hvac_ac" -> R.string.label_air_conditioning
        "hvac_auto" -> R.string.label_automatic_climate_control
        "hvac_fan_direction" -> R.string.label_airflow_direction
        "hvac_fan_speed" -> R.string.label_fan_speed
        "hvac_heater_level" -> R.string.label_heater_level
        "hvac_pm25" -> R.string.label_pm2_5_purification
        "hvac_power" -> R.string.label_climate_power
        "hvac_recirc" -> R.string.launcher_vs_climate_recirc
        "hvac_sync" -> R.string.label_climate_sync
        "ldw" -> R.string.label_lane_departure_ldw
        "lka" -> R.string.label_lane_keeping_lka
        "mirror_fold" -> R.string.label_fold_mirrors
        "ngp" -> R.string.label_ngp_assist
        "parking_radar" -> R.string.label_parking_sensor
        "passenger_seat_adjust" -> R.string.label_passenger_seat
        "radio_app" -> R.string.label_radio
        "rear_defrost" -> R.string.label_rear_window_defroster
        "rear_fog" -> R.string.launcher_vs_rear_fog
        "reboot_hu" -> R.string.label_reboot_head_unit
        "screen_off" -> R.string.label_screen_off
        "seat_adjust" -> R.string.label_seat_adjustment
        "seat_heat_left" -> R.string.label_driver_seat_heating
        "seat_heat_rear_left" -> R.string.label_rear_left_seat_heating
        "seat_heat_rear_right" -> R.string.label_rear_right_seat_heating
        "seat_heat_right" -> R.string.label_passenger_seat_heating
        "seat_memory_1" -> R.string.label_driver_memory_1
        "seat_memory_2" -> R.string.label_driver_memory_2
        "seat_memory_3" -> R.string.label_driver_memory_3
        "seat_vent_left" -> R.string.label_driver_seat_ventilation
        "seat_vent_right" -> R.string.label_passenger_seat_ventilation
        "smart_acc" -> R.string.launcher_cruise_engage_action_smart_acc
        "steering_heat" -> R.string.launcher_vs_steering_heat
        "temp_driver" -> R.string.label_driver_temperature
        "temp_pass" -> R.string.label_passenger_temperature
        "theme" -> R.string.label_day_night_theme
        "trunk_door" -> R.string.launcher_vs_sim_tailgate
        "vol_media" -> R.string.widget_media_volume_title
        "vol_navi" -> R.string.label_navigation_volume
        "vol_phone" -> R.string.label_call_volume
        "vol_voice" -> R.string.label_voice_volume
        "wifi" -> R.string.label_wi_fi_settings
        "windows" -> R.string.label_windows
        "windscreen_heat" -> R.string.label_windshield_heating
        "wiper_maintenance" -> R.string.label_wiper_service_mode
        "wiper_sensitivity" -> R.string.label_automatic_wipers
        else -> null
    }

internal object LauncherTopShadeConfigStore {
    private const val TAG = "TopShadeConfigStore"
    private const val PREFS_NAME = "launcher_top_shade_prefs"
    private const val KEY_TILES_JSON = "top_shade_tiles_json"
    private const val KEY_FONT_SCALE_OFFSET = "top_shade_font_scale_offset"
    private const val KEY_ICON_SCALE_OFFSET = "top_shade_icon_scale_offset"

    const val GRID_COLS = 12
    const val GRID_ROWS = 12

    private val tilesRevision = MutableStateFlow(0)
    val tilesRevisionFlow: StateFlow<Int> = tilesRevision

    private val appearanceRevision = MutableStateFlow(0)
    val appearanceRevisionFlow: StateFlow<Int> = appearanceRevision

    fun getFontScaleOffset(context: Context): Float =
        prefs(context).getFloat(KEY_FONT_SCALE_OFFSET, 29.96199f).coerceIn(-50f, 50f)

    fun setFontScaleOffset(context: Context, offset: Float) {
        val clamped = offset.coerceIn(-50f, 50f)
        prefs(context).edit().putFloat(KEY_FONT_SCALE_OFFSET, clamped).apply()
        appearanceRevision.value++
    }

    fun getIconScaleOffset(context: Context): Float =
        prefs(context).getFloat(KEY_ICON_SCALE_OFFSET, -16.528717f).coerceIn(-50f, 50f)

    fun setIconScaleOffset(context: Context, offset: Float) {
        val clamped = offset.coerceIn(-50f, 50f)
        prefs(context).edit().putFloat(KEY_ICON_SCALE_OFFSET, clamped).apply()
        appearanceRevision.value++
    }

    fun fontScale(context: Context): Float =
        1.0f + (getFontScaleOffset(context) / 100f)

    fun iconScale(context: Context): Float =
        1.0f + (getIconScaleOffset(context) / 100f)

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    const val DEFAULT_TILES_JSON =
        "[{\"id\":\"brightness\",\"title\":\"Яркость\",\"type\":\"SYSTEM_TOGGLE\",\"actionKey\":\"brightness\",\"iconResName\":\"\",\"spanX\":4,\"spanY\":2,\"col\":0,\"row\":0},{\"id\":\"vol_media\",\"title\":\"Громкость Медиа\",\"type\":\"SYSTEM_TOGGLE\",\"actionKey\":\"vol_media\",\"iconResName\":\"\",\"spanX\":4,\"spanY\":2,\"col\":0,\"row\":2},{\"id\":\"vol_phone\",\"title\":\"Громкость Звонков\",\"type\":\"SYSTEM_TOGGLE\",\"actionKey\":\"vol_phone\",\"iconResName\":\"\",\"spanX\":4,\"spanY\":2,\"col\":4,\"row\":2},{\"id\":\"vol_navi\",\"title\":\"Громкость Навигации\",\"type\":\"SYSTEM_TOGGLE\",\"actionKey\":\"vol_navi\",\"iconResName\":\"\",\"spanX\":4,\"spanY\":2,\"col\":4,\"row\":0},{\"id\":\"opt_mirror_fold\",\"title\":\"Сложение зеркал\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"mirror_fold\",\"iconResName\":\"ic_widget_mirror_fold\",\"spanX\":1,\"spanY\":2,\"col\":0,\"row\":8},{\"id\":\"opt_seat_memory_1\",\"title\":\"Память DRV1\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"seat_memory_1\",\"iconResName\":\"ic_seat_memory_drv1\",\"spanX\":1,\"spanY\":2,\"col\":0,\"row\":6},{\"id\":\"opt_seat_memory_2\",\"title\":\"Память DRV2\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"seat_memory_2\",\"iconResName\":\"ic_seat_memory_drv2\",\"spanX\":1,\"spanY\":2,\"col\":1,\"row\":6},{\"id\":\"opt_seat_adjust\",\"title\":\"Настройка сиденья\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"seat_adjust\",\"iconResName\":\"ic_seat_memory_btn\",\"spanX\":1,\"spanY\":2,\"col\":2,\"row\":6},{\"id\":\"opt_wiper_maintenance\",\"title\":\"Сервис дворников\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"wiper_maintenance\",\"iconResName\":\"ic_wiper\",\"spanX\":1,\"spanY\":2,\"col\":1,\"row\":8},{\"id\":\"opt_trunk\",\"title\":\"Багажник\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"trunk_door\",\"iconResName\":\"ic_launcher_trunk\",\"spanX\":1,\"spanY\":2,\"col\":2,\"row\":8},{\"id\":\"opt_rear_fog\",\"title\":\"Задний ПТФ\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"rear_fog\",\"iconResName\":\"ic_launcher_rear_fog\",\"spanX\":1,\"spanY\":2,\"col\":3,\"row\":8},{\"id\":\"opt_esc_off\",\"title\":\"ESP Стабилизация\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"esc_off\",\"iconResName\":\"ic_esp_icon\",\"spanX\":1,\"spanY\":2,\"col\":0,\"row\":10},{\"id\":\"opt_parking_radar\",\"title\":\"Парктроник\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"parking_radar\",\"iconResName\":\"ic_widget_parking_radar\",\"spanX\":1,\"spanY\":2,\"col\":2,\"row\":10},{\"id\":\"opt_pass_seat_adjust\",\"title\":\"Пасс. сиденье\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"passenger_seat_adjust\",\"iconResName\":\"ic_seat_memory_btn\",\"spanX\":1,\"spanY\":2,\"col\":3,\"row\":6},{\"id\":\"opt_windows\",\"title\":\"Окна\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"windows\",\"iconResName\":\"ic_widget_side_window\",\"spanX\":1,\"spanY\":2,\"col\":3,\"row\":10},{\"id\":\"opt_hdc\",\"title\":\"Спуск с горы HDC\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"hdc\",\"iconResName\":\"ic_launcher_hdc\",\"spanX\":1,\"spanY\":2,\"col\":1,\"row\":10},{\"id\":\"opt_dcl_log\",\"title\":\"Запись лога (5 мин)\",\"type\":\"SYSTEM_TOGGLE\",\"actionKey\":\"dcl_log\",\"iconResName\":\"menu_icon_log\",\"spanX\":2,\"spanY\":2,\"col\":2,\"row\":4},{\"id\":\"opt_avm_camera\",\"title\":\"Камеры 360\",\"type\":\"CAR_CONTROL\",\"actionKey\":\"avm_camera\",\"iconResName\":\"ic_launcher_avm\",\"spanX\":2,\"spanY\":2,\"col\":0,\"row\":4}]"

    val DEFAULT_TILES = listOf(
        TopShadeTileSpec("brightness", "", TopShadeTileType.SYSTEM_TOGGLE, "brightness", null, 4, 2, 0, 0),
        TopShadeTileSpec("vol_media", "", TopShadeTileType.SYSTEM_TOGGLE, "vol_media", null, 4, 2, 0, 2),
        TopShadeTileSpec("vol_phone", "", TopShadeTileType.SYSTEM_TOGGLE, "vol_phone", null, 4, 2, 4, 2),
        TopShadeTileSpec("vol_navi", "", TopShadeTileType.SYSTEM_TOGGLE, "vol_navi", null, 4, 2, 4, 0),
        TopShadeTileSpec("opt_mirror_fold", "", TopShadeTileType.CAR_CONTROL, "mirror_fold", "ic_widget_mirror_fold", 1, 2, 0, 8),
        TopShadeTileSpec("opt_seat_memory_1", "", TopShadeTileType.CAR_CONTROL, "seat_memory_1", "ic_seat_memory_drv1", 1, 2, 0, 6),
        TopShadeTileSpec("opt_seat_memory_2", "", TopShadeTileType.CAR_CONTROL, "seat_memory_2", "ic_seat_memory_drv2", 1, 2, 1, 6),
        TopShadeTileSpec("opt_seat_adjust", "", TopShadeTileType.CAR_CONTROL, "seat_adjust", "ic_seat_memory_btn", 1, 2, 2, 6),
        TopShadeTileSpec("opt_wiper_maintenance", "", TopShadeTileType.CAR_CONTROL, "wiper_maintenance", "ic_wiper", 1, 2, 1, 8),
        TopShadeTileSpec("opt_trunk", "", TopShadeTileType.CAR_CONTROL, "trunk_door", "ic_launcher_trunk", 1, 2, 2, 8),
        TopShadeTileSpec("opt_rear_fog", "", TopShadeTileType.CAR_CONTROL, "rear_fog", "ic_launcher_rear_fog", 1, 2, 3, 8),
        TopShadeTileSpec("opt_esc_off", "", TopShadeTileType.CAR_CONTROL, "esc_off", "ic_esp_icon", 1, 2, 0, 10),
        TopShadeTileSpec("opt_parking_radar", "", TopShadeTileType.CAR_CONTROL, "parking_radar", "ic_widget_parking_radar", 1, 2, 2, 10),
        TopShadeTileSpec("opt_pass_seat_adjust", "", TopShadeTileType.CAR_CONTROL, "passenger_seat_adjust", "ic_seat_memory_btn", 1, 2, 3, 6),
        TopShadeTileSpec("opt_windows", "", TopShadeTileType.CAR_CONTROL, "windows", "ic_widget_side_window", 1, 2, 3, 10),
        TopShadeTileSpec("opt_hdc", "", TopShadeTileType.CAR_CONTROL, "hdc", "ic_launcher_hdc", 1, 2, 1, 10),
        TopShadeTileSpec("opt_dcl_log", "", TopShadeTileType.SYSTEM_TOGGLE, "dcl_log", "menu_icon_log", 2, 2, 2, 4),
        TopShadeTileSpec("opt_avm_camera", "", TopShadeTileType.CAR_CONTROL, "avm_camera", "ic_launcher_avm", 2, 2, 0, 4),
    )

    fun getTiles(context: Context): List<TopShadeTileSpec> {
        val jsonStr = prefs(context).getString(KEY_TILES_JSON, null)

        val loaded = if (jsonStr.isNull_or_blank()) {
            DEFAULT_TILES
        } else {
            try {
                val jsonArray = JSONArray(jsonStr)
                val list = mutableListOf<TopShadeTileSpec>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val typeStr = obj.optString("type", TopShadeTileType.CAR_CONTROL.name)
                    val type = runCatching { TopShadeTileType.valueOf(typeStr) }.getOrDefault(TopShadeTileType.CAR_CONTROL)

                    val rawActionKey = obj.getString("actionKey")
                    val actionKey = if (rawActionKey == "volume") "vol_media" else rawActionKey

                    list.add(
                        TopShadeTileSpec(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            type = type,
                            actionKey = actionKey,
                            iconResName = obj.optString("iconResName", "").takeIf { it.isNotBlank() },
                            spanX = obj.optInt("spanX", 1),
                            spanY = obj.optInt("spanY", 1),
                            col = obj.optInt("col", -1),
                            row = obj.optInt("row", -1),
                        )
                    )
                }
                if (list.isEmpty()) DEFAULT_TILES else list
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse top shade tiles JSON", e)
                DEFAULT_TILES
            }
        }
        return ensureValidGridCoordinates(loaded, GRID_COLS, GRID_ROWS)
    }

    fun setTiles(context: Context, tiles: List<TopShadeTileSpec>) {
        try {
            val jsonArray = JSONArray()
            for (tile in tiles) {
                val obj = JSONObject().apply {
                    put("id", tile.id)
                    put("title", tile.title.ifEmpty { tile.displayTitle })
                    put("type", tile.type.name)
                    put("actionKey", tile.actionKey)
                    put("iconResName", tile.iconResName ?: "")
                    put("spanX", tile.spanX)
                    put("spanY", tile.spanY)
                    put("col", tile.col)
                    put("row", tile.row)
                }
                jsonArray.put(obj)
            }
            prefs(context).edit().putString(KEY_TILES_JSON, jsonArray.toString()).apply()
            tilesRevision.value++
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save top shade tiles JSON", e)
        }
    }

    fun resetToDefault(context: Context) {
        prefs(context).edit()
            .remove(KEY_TILES_JSON)
            .remove(KEY_FONT_SCALE_OFFSET)
            .remove(KEY_ICON_SCALE_OFFSET)
            .apply()
        tilesRevision.value++
        appearanceRevision.value++
    }

    private fun ensureValidGridCoordinates(
        tiles: List<TopShadeTileSpec>,
        numCols: Int,
        numRows: Int,
    ): List<TopShadeTileSpec> {
        var currentRow = 0
        var currentCol = 0

        return tiles.map { tile ->
            val clampedSpanX = tile.spanX.coerceIn(1, numCols)
            val clampedSpanY = tile.spanY.coerceIn(1, numRows)

            if (tile.col >= 0 && tile.row >= 0 &&
                tile.col + clampedSpanX <= numCols &&
                tile.row + clampedSpanY <= numRows
            ) {
                tile.copy(spanX = clampedSpanX, spanY = clampedSpanY)
            } else {
                if (currentCol + clampedSpanX > numCols) {
                    currentCol = 0
                    currentRow++
                }
                val assignedRow = currentRow.coerceAtMost((numRows - clampedSpanY).coerceAtLeast(0))
                val assignedCol = currentCol.coerceAtMost((numCols - clampedSpanX).coerceAtLeast(0))
                val assigned = tile.copy(col = assignedCol, row = assignedRow, spanX = clampedSpanX, spanY = clampedSpanY)
                currentCol += clampedSpanX
                if (currentCol >= numCols) {
                    currentCol = 0
                    currentRow++
                }
                assigned
            }
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
