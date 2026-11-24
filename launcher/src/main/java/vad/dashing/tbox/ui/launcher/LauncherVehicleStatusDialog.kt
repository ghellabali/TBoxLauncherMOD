package vad.dashing.tbox.ui.launcher

import vad.dashing.tbox.launcherText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import vad.dashing.tbox.valueToString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vad.dashing.tbox.CanDataRepository
import vad.dashing.tbox.R
import kotlin.math.roundToInt

internal object LauncherVehicleStatusDialogState {
    var isVisible by mutableStateOf(false)
        private set

    fun show(context: android.content.Context? = vad.dashing.tbox.LauncherHomeActivityHolder.instance) {
        if (context == null || !LauncherVehicleStatusOverlayWindow.show(context)) {
            isVisible = true
        }
    }

    fun hide() {
        isVisible = false
        LauncherVehicleStatusOverlayWindow.hide()
    }
}

internal enum class LauncherVehicleHealthStatus {
    Ok,       // Зелёный значок OK в кругу
    Warning,  // Жёлтый значок "восклицательный знак в треугольнике"
    Error,    // Красный значок Warning! в прямоугольной рамке
}

internal fun computeVehicleHealthStatus(
    alerts: List<LauncherVehicleAlert>,
    hasTireAttention: Boolean,
    hasTireTempAttention: Boolean = false,
    srrSystem: LauncherSrrSystemState,
    accMode: LauncherAdasAccMode,
    simShowAll: Boolean,
): LauncherVehicleHealthStatus {
    val hasCritical = simShowAll ||
            alerts.any { it.severity == LauncherAlertSeverity.Critical } ||
            hasTireAttention ||
            srrSystem == LauncherSrrSystemState.Fault

    if (hasCritical) return LauncherVehicleHealthStatus.Error

    val hasWarning = alerts.any { it.severity == LauncherAlertSeverity.Warning } ||
            hasTireTempAttention ||
            accMode == LauncherAdasAccMode.Fault

    if (hasWarning) return LauncherVehicleHealthStatus.Warning

    return LauncherVehicleHealthStatus.Ok
}

@Composable
internal fun rememberVehicleHealthStatus(): LauncherVehicleHealthStatus {
    val context = LocalContext.current
    val tirePressureConfigRevision by LauncherAppConfigStore.tirePressureConfigRevisionFlow.collectAsStateWithLifecycle()
    val tirePressureLowThreshold = remember(context, tirePressureConfigRevision) {
        LauncherAppConfigStore.tirePressureLowThreshold(context)
    }
    val allowLowTempAlert = remember(context, tirePressureConfigRevision) {
        LauncherAppConfigStore.tireLowTempAlertEnabled(context)
    }
    val alertsState by LauncherVehicleAlertsRepository.state.collectAsStateWithLifecycle()
    val adasState by LauncherAdasRepository.state.collectAsStateWithLifecycle()
    val tiresState by LauncherTireRepository.state.collectAsStateWithLifecycle()
    val wheelsPressure by CanDataRepository.wheelsPressure.collectAsStateWithLifecycle()
    val wheelsTemperature by CanDataRepository.wheelsTemperature.collectAsStateWithLifecycle()
    val effectiveTires = remember(tiresState, wheelsPressure, wheelsTemperature) {
        mergeTireStateWithCanData(tiresState, wheelsPressure, wheelsTemperature)
    }
    val simShowAll = LauncherDevVehicleState.showAllIndicators

    val alerts = if (simShowAll) alertsState.alerts else alertsState.alerts.filterNot { it.id.isBodyOpenAlert }
    val adas = LauncherDevVehicleState.adasStateOrNull() ?: adasState

    return remember(alerts, effectiveTires, tirePressureLowThreshold, allowLowTempAlert, adas, simShowAll) {
        computeVehicleHealthStatus(
            alerts = alerts,
            hasTireAttention = effectiveTires.hasLowPressure(tirePressureLowThreshold),
            hasTireTempAttention = allowLowTempAlert && effectiveTires.hasLowTemperature(),
            srrSystem = adas.srrSystem,
            accMode = adas.accMode,
            simShowAll = simShowAll,
        )
    }
}

internal data class LauncherVehicleStatusDetailItem(
    val title: String,
    val description: String,
    val severity: LauncherAlertSeverity,
)

private val LauncherAlertId.detailDescription: String
    get() = when (this) {
        LauncherAlertId.SeatBeltDriver -> launcherText(R.string.label_driver_seat_belt_unfastened_fasten_it_before_driving)
        LauncherAlertId.SeatBeltPassenger -> launcherText(R.string.label_front_passenger_seat_belt_unfastened_fasten_the_seat_belt)
        LauncherAlertId.SeatBeltRearLeft -> launcherText(R.string.label_rear_left_passenger_seat_belt_unfastened)
        LauncherAlertId.SeatBeltRearMid -> launcherText(R.string.label_rear_center_passenger_seat_belt_unfastened)
        LauncherAlertId.SeatBeltRearRight -> launcherText(R.string.label_rear_right_passenger_seat_belt_unfastened)
        LauncherAlertId.DoorDriver -> launcherText(R.string.label_driver_door_is_not_fully_closed)
        LauncherAlertId.DoorPassenger -> launcherText(R.string.label_front_passenger_door_is_not_fully_closed)
        LauncherAlertId.DoorRearLeft -> launcherText(R.string.label_rear_left_door_is_not_fully_closed)
        LauncherAlertId.DoorRearRight -> launcherText(R.string.label_rear_right_door_is_not_fully_closed)
        LauncherAlertId.HoodOpen -> launcherText(R.string.label_hood_is_not_fully_closed_check_the_latch)
        LauncherAlertId.TrunkOpen -> launcherText(R.string.label_trunk_is_open)
        LauncherAlertId.TirePressure -> launcherText(R.string.label_tire_pressure_is_below_the_configured_threshold)
        LauncherAlertId.TireTemperature -> launcherText(R.string.label_tire_temperature_is_below_normal_allow_the_tires_to_warm_up_while_dr)
        LauncherAlertId.LowFuel -> launcherText(R.string.label_fuel_level_is_very_low_refuel_the_vehicle)
        LauncherAlertId.Speeding -> launcherText(R.string.label_vehicle_speed_exceeds_the_set_limit)
        LauncherAlertId.HighTemperature -> launcherText(R.string.label_coolant_temperature_is_high_stop_driving_and_let_the_engine_cool)
        LauncherAlertId.PressBrake -> launcherText(R.string.label_press_the_brake_pedal_to_continue_or_change_mode)
        LauncherAlertId.SysFault -> launcherText(R.string.label_icm_or_can_control_module_fault_detected)
        LauncherAlertId.BattFault -> launcherText(R.string.label_12_v_battery_or_battery_monitoring_system_fault)
        LauncherAlertId.ChargeFault -> launcherText(R.string.label_battery_charging_or_alternator_fault)
        LauncherAlertId.HvFaultStop -> launcherText(R.string.label_critical_high_voltage_system_fault_stop_the_vehicle_and_have_it_insp)
        LauncherAlertId.PowerModeFail -> launcherText(R.string.label_power_mode_switching_fault)
        LauncherAlertId.LowBatterySoc -> launcherText(R.string.label_battery_charge_is_low_low_soc)
        LauncherAlertId.KeyBatteryLow -> launcherText(R.string.label_smart_key_battery_is_low_replace_the_cr2032_battery)
    }

@Composable
internal fun rememberVehicleStatusDetailItems(): List<LauncherVehicleStatusDetailItem> {
    val context = LocalContext.current
    val tirePressureConfigRevision by LauncherAppConfigStore.tirePressureConfigRevisionFlow.collectAsStateWithLifecycle()
    val tirePressureShowHundredths = remember(context, tirePressureConfigRevision) {
        LauncherAppConfigStore.tirePressureShowHundredths(context)
    }
    val tirePressureLowThreshold = remember(context, tirePressureConfigRevision) {
        LauncherAppConfigStore.tirePressureLowThreshold(context)
    }
    val allowLowTempAlert = remember(context, tirePressureConfigRevision) {
        LauncherAppConfigStore.tireLowTempAlertEnabled(context)
    }
    val alertsState by LauncherVehicleAlertsRepository.state.collectAsStateWithLifecycle()
    val adasState by LauncherAdasRepository.state.collectAsStateWithLifecycle()
    val tiresState by LauncherTireRepository.state.collectAsStateWithLifecycle()
    val wheelsPressure by CanDataRepository.wheelsPressure.collectAsStateWithLifecycle()
    val wheelsTemperature by CanDataRepository.wheelsTemperature.collectAsStateWithLifecycle()
    val effectiveTires = remember(tiresState, wheelsPressure, wheelsTemperature) {
        mergeTireStateWithCanData(tiresState, wheelsPressure, wheelsTemperature)
    }
    val simShowAll = LauncherDevVehicleState.showAllIndicators

    val alerts = if (simShowAll) alertsState.alerts else alertsState.alerts.filterNot { it.id.isBodyOpenAlert }
    val adas = LauncherDevVehicleState.adasStateOrNull() ?: adasState

    return buildList {
        alerts.forEach { alert ->
            add(
                LauncherVehicleStatusDetailItem(
                    title = stringResource(alert.id.labelRes),
                    description = alert.id.detailDescription,
                    severity = alert.severity,
                )
            )
        }

        val corners = listOf(
            launcherText(R.string.label_front_left) to effectiveTires.fl,
            launcherText(R.string.label_front_right) to effectiveTires.fr,
            launcherText(R.string.label_rear_left) to effectiveTires.rl,
            launcherText(R.string.label_rear_right) to effectiveTires.rr,
        )
        val lowPressureCorners = corners.filter { it.second.isLowPressure(tirePressureLowThreshold) }
        lowPressureCorners.forEach { (cornerName, wheel) ->
            val accuracy = if (tirePressureShowHundredths) 2 else 1
            val press = wheel.pressureBar?.let { "${valueToString(it, accuracy)} bar" } ?: "—"
            add(
                LauncherVehicleStatusDetailItem(
                    title = launcherText(R.string.label_low_pressure, cornerName),
                    description = launcherText(R.string.label_current_pressure_is_below_the_threshold_bar_check_the_tire, press, valueToString(tirePressureLowThreshold, 1)),
                    severity = LauncherAlertSeverity.Critical,
                )
            )
        }

        if (allowLowTempAlert) {
            val lowTempCorners = corners.filter { it.second.isLowTemperature() }
            lowTempCorners.forEach { (cornerName, wheel) ->
                val tempStr = wheel.temperatureC?.let { "$it°C" } ?: "—"
                add(
                    LauncherVehicleStatusDetailItem(
                        title = launcherText(R.string.label_low_temperature, cornerName),
                        description = launcherText(R.string.label_current_temperature_is_too_low_drive_gently_while_the_tires_warm_up, tempStr),
                        severity = LauncherAlertSeverity.Warning,
                    )
                )
            }
        }

        if (adas.srrSystem == LauncherSrrSystemState.Fault) {
            add(
                LauncherVehicleStatusDetailItem(
                    title = launcherText(R.string.label_side_radar_fault_srr),
                    description = launcherText(R.string.label_blind_spot_sensors_are_obstructed_or_faulty),
                    severity = LauncherAlertSeverity.Critical,
                )
            )
        }

        if (adas.accMode == LauncherAdasAccMode.Fault) {
            add(
                LauncherVehicleStatusDetailItem(
                    title = stringResource(R.string.launcher_vehicle_status_radar_fault_title),
                    description = stringResource(R.string.launcher_vehicle_status_radar_fault_desc),
                    severity = LauncherAlertSeverity.Warning,
                )
            )
        }
    }
}

@Composable
internal fun LauncherVehicleStatusDialog(
    onDismissRequest: () -> Unit,
) {
    val items = rememberVehicleStatusDetailItems()
    val status = rememberVehicleHealthStatus()

    LauncherDarkAlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = launcherText(R.string.label_vehicle_system_status),
                    color = LauncherColors.TextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
                when (status) {
                    LauncherVehicleHealthStatus.Ok -> {
                        Text(
                            text = launcherText(R.string.label_good),
                            color = Color(0xFF9CA3AF),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    LauncherVehicleHealthStatus.Warning -> {
                        Text(
                            text = launcherText(R.string.launcher_vehicle_status_warning),
                            color = Color(0xFFF59E0B),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    LauncherVehicleHealthStatus.Error -> {
                        Text(
                            text = launcherText(R.string.label_error),
                            color = Color(0xFFEF4444),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E).copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "✓",
                                    color = Color(0xFF22C55E),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(
                                text = launcherText(R.string.label_all_vehicle_systems_are_ok),
                                color = LauncherColors.TextPrimary,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    items.forEach { item ->
                        val tint = when (item.severity) {
                            LauncherAlertSeverity.Critical -> Color(0xFFEF4444)
                            LauncherAlertSeverity.Warning -> Color(0xFFF59E0B)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LauncherColors.SurfaceDark)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(tint.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = tint,
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = item.title,
                                    color = tint,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = item.description,
                                    color = LauncherColors.TextPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    stringResource(R.string.action_close),
                    color = LauncherColors.AccentCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    )
}
