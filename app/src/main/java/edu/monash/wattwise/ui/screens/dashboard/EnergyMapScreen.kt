package edu.monash.wattwise.ui.screens.dashboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import edu.monash.wattwise.R
import edu.monash.wattwise.constants.ContextThresholds
import edu.monash.wattwise.constants.MapSettings
import edu.monash.wattwise.constants.WeatherEnergyRules
import edu.monash.wattwise.data.model.MapLayer
import edu.monash.wattwise.data.model.MapPlace
import edu.monash.wattwise.data.remote.WeatherResponse
import edu.monash.wattwise.ui.components.StatTile
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.OnAccent
import edu.monash.wattwise.ui.theme.SoftBlue
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.ThemeSettings
import edu.monash.wattwise.utils.DemandLevel
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.WeatherCalculator
// Named MarkerIcon here because Compose also has a composable called Icon.
import org.maplibre.android.annotations.Icon as MarkerIcon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.math.roundToInt

/**
 * Energy map. Every town has a marker showing today's weather from the
 * weather API in the layer the user picks: temperature, heating and cooling
 * demand, or solar strength. Tapping a town, or any point on the map, shows
 * what its weather means for energy use, and it can be saved as the home
 * location that the recommendations use.
 *
 * @param savedPlace the home location currently saved in Room.
 * @param placeWeather today's forecast for each place, keyed by place name.
 * @param tariff the household's tariff, used for the "best time" advice.
 * @param onPlaceSelected called when the selection changes, so its forecast can be requested.
 */
@Composable
fun EnergyMapScreen(
    savedPlace: MapPlace,
    placeWeather: Map<String, WeatherResponse>,
    tariff: String,
    onPlaceSelected: (MapPlace) -> Unit,
    onSave: (MapPlace) -> Unit,
) {
    var layer by remember { mutableStateOf(MapLayer.TEMPERATURE) }
    var selectedPlace by remember { mutableStateOf(savedPlace) }
    // Kept in state so the same map object is used across recompositions.
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    // The marker for each town, so its label can be redrawn when the layer or weather changes.
    var townMarkers by remember { mutableStateOf<Map<String, Marker>>(emptyMap()) }
    // The marker added where the user tapped; replaced on the next tap.
    var tappedMarker by remember { mutableStateOf<Marker?>(null) }

    val selectedWeather = placeWeather[selectedPlace.name]

    fun select(place: MapPlace) {
        selectedPlace = place
        onPlaceSelected(place)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = Dimens.ScreenPaddingCompact)) {
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ENERGY MAP", style = MaterialTheme.typography.labelLarge)
                Text(
                    "Today’s weather and what it means for your energy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            OutlinedButton(onClick = { mapLibreMap?.animateCamera(CameraUpdateFactory.zoomIn()) }) {
                Text("+", color = TextPrimary)
            }
            Spacer(Modifier.width(6.dp))
            OutlinedButton(onClick = { mapLibreMap?.animateCamera(CameraUpdateFactory.zoomOut()) }) {
                Text("−", color = TextPrimary)
            }
        }
        Spacer(Modifier.height(4.dp))

        // A FilterChip works like a toggle: the same towns shown with one of three kinds of data.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MapLayer.entries.forEach { option ->
                FilterChip(
                    selected = layer == option,
                    onClick = { layer = option },
                    label = { Text(option.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Electric,
                        selectedLabelColor = OnAccent,
                        containerColor = Surface,
                        labelColor = TextPrimary,
                    ),
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // MapView is a traditional Android View, so AndroidView places it inside Compose.
        AndroidView(
            modifier = Modifier.fillMaxWidth().weight(1f),
            factory = { context ->
                MapView(context).apply {
                    onCreate(null)
                    onStart()
                    onResume()
                    // getMapAsync prepares the map without blocking the UI and then runs this block.
                    getMapAsync { map ->
                        mapLibreMap = map
                        map.setStyle(if (ThemeSettings.isLight) MapSettings.LIGHT_STYLE_URL else MapSettings.DARK_STYLE_URL) {
                            map.cameraPosition = overviewCamera()

                            // One marker for each town in the list.
                            townMarkers = MapSettings.PLACES.associate { place ->
                                place.name to map.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(place.latitude, place.longitude))
                                        .title(place.name),
                                )
                            }
                            // A pin for the home, when it is a tapped point and not one of the towns.
                            if (MapSettings.PLACES.none { it.name == savedPlace.name }) {
                                tappedMarker = map.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(savedPlace.latitude, savedPlace.longitude))
                                        .title(MapSettings.HOME_MARKER_TITLE),
                                )
                            }
                        }

                        // Tapping a town's marker selects it. Returning false also shows its label.
                        map.setOnMarkerClickListener { marker ->
                            val town = MapSettings.PLACES.firstOrNull { it.name == marker.title }
                            if (town != null) select(town)
                            false
                        }

                        // Tapping anywhere else selects that exact point and marks it.
                        map.addOnMapClickListener { point ->
                            tappedMarker?.let { map.removeMarker(it) }
                            tappedMarker = map.addMarker(
                                MarkerOptions().position(point).title(MapSettings.CUSTOM_PLACE_NAME),
                            )
                            select(MapPlace(MapSettings.CUSTOM_PLACE_NAME, point.latitude, point.longitude))
                            true
                        }
                    }
                }
            },
            // Runs when the layer changes or a forecast arrives: redraws each town's label.
            update = { mapView ->
                townMarkers.forEach { (name, marker) ->
                    val weather = placeWeather[name]
                    if (weather != null) {
                        marker.icon = labelIcon(mapView.context, markerText(layer, weather), markerColor(layer, weather))
                        marker.snippet = detailText(layer, weather)
                    }
                }
            },
        )

        Spacer(Modifier.height(10.dp))
        val isSaved = selectedPlace == savedPlace

        // Place name with one action. The button is hidden once the place is the saved home.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.map_icon), contentDescription = null, tint = Electric, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(selectedPlace.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    if (isSaved) "YOUR HOME LOCATION" else "SELECTED LOCATION",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            if (!isSaved) {
                OutlinedButton(onClick = { onSave(selectedPlace) }) {
                    Text("SET AS HOME", style = MaterialTheme.typography.labelLarge, color = Electric)
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        // Three figures side by side; the last one follows the layer chosen above the map.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                label = "Now",
                value = selectedWeather?.let { "${it.current.temperatureCelsius.roundToInt()}°C" } ?: "…",
                isSelected = false,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = "High / low",
                value = selectedWeather?.let { highLowText(it) } ?: "…",
                isSelected = false,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = layerLabel(layer, selectedWeather),
                value = selectedWeather?.let { layerValue(layer, it) } ?: "…",
                isSelected = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        SurfaceCard {
            Text("BEST TIME FOR APPLIANCES", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.height(4.dp))
            Text(
                selectedWeather?.let { WeatherCalculator.bestTimeText(tariff, it) } ?: "Loading the forecast…",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

/** Camera position that shows every town in the list at once. */
private fun overviewCamera(): CameraPosition =
    CameraPosition.Builder()
        .target(LatLng(MapSettings.OVERVIEW_LATITUDE, MapSettings.OVERVIEW_LONGITUDE))
        .zoom(MapSettings.OVERVIEW_ZOOM)
        .build()

/** Today's forecast high and low, for example "24° / 13°". */
private fun highLowText(weather: WeatherResponse): String {
    val high = weather.daily?.maxTemperatures?.firstOrNull() ?: return "–"
    val low = weather.daily.minTemperatures.firstOrNull() ?: return "–"
    return "${high.roundToInt()}° / ${low.roundToInt()}°"
}

/** Label of the third tile, which shows the figure for the chosen layer. */
private fun layerLabel(layer: MapLayer, weather: WeatherResponse?): String = when (layer) {
    MapLayer.TEMPERATURE -> "Humidity"
    MapLayer.DEMAND -> if (weather != null && WeatherCalculator.coolingDegrees(weather) > 0.0) "Cooling" else "Heating"
    MapLayer.SOLAR -> "Sun at ${weather?.let { Formatters.formatClockHour(WeatherCalculator.peakSolarHour(it)) } ?: "peak"}"
}

private fun layerValue(layer: MapLayer, weather: WeatherResponse): String = when (layer) {
    MapLayer.TEMPERATURE -> "${weather.current.humidityPercent}%"
    MapLayer.DEMAND -> WeatherCalculator.demandLevel(weather).label
    MapLayer.SOLAR -> "${WeatherCalculator.peakSolar(weather).roundToInt()} W/m²"
}

/** The short text drawn on a town's marker for the chosen layer. */
private fun markerText(layer: MapLayer, weather: WeatherResponse): String = when (layer) {
    MapLayer.TEMPERATURE -> "${weather.current.temperatureCelsius.roundToInt()}°"
    MapLayer.DEMAND -> WeatherCalculator.demandLevel(weather).label.uppercase()
    MapLayer.SOLAR -> "${WeatherCalculator.peakSolar(weather).roundToInt()}"
}

/** The marker colour for the chosen layer. The text on the marker always gives the value too. */
private fun markerColor(layer: MapLayer, weather: WeatherResponse): Color = when (layer) {
    MapLayer.TEMPERATURE -> when {
        weather.current.temperatureCelsius <= ContextThresholds.COLD_TEMPERATURE -> SoftBlue
        weather.current.temperatureCelsius >= ContextThresholds.HOT_TEMPERATURE -> Coral
        else -> Electric
    }
    MapLayer.DEMAND -> when (WeatherCalculator.demandLevel(weather)) {
        DemandLevel.LOW -> Electric
        DemandLevel.MEDIUM -> Amber
        DemandLevel.HIGH -> Coral
    }
    MapLayer.SOLAR -> when {
        WeatherCalculator.peakSolar(weather) >= WeatherEnergyRules.STRONG_SOLAR_RADIATION -> Amber
        WeatherCalculator.peakSolar(weather) >= WeatherEnergyRules.GOOD_SOLAR_RADIATION -> Electric
        else -> SoftBlue
    }
}

/** One sentence explaining the chosen layer's value for a place. */
private fun detailText(layer: MapLayer, weather: WeatherResponse): String = when (layer) {
    MapLayer.TEMPERATURE ->
        "Now ${weather.current.temperatureCelsius.roundToInt()}°C with ${weather.current.humidityPercent}% humidity."
    MapLayer.DEMAND -> {
        val heating = WeatherCalculator.heatingDegrees(weather)
        val cooling = WeatherCalculator.coolingDegrees(weather)
        val average = Formatters.formatKwh(WeatherCalculator.averageTemperature(weather))
        when {
            heating > 0.0 -> "Heating demand is ${WeatherCalculator.demandLevel(weather).label.lowercase()}: today averages " +
                "$average°C, ${Formatters.formatKwh(heating)}° below the ${WeatherEnergyRules.HEATING_BASE_TEMPERATURE.toInt()}°C comfort level."
            cooling > 0.0 -> "Cooling demand is ${WeatherCalculator.demandLevel(weather).label.lowercase()}: today averages " +
                "$average°C, ${Formatters.formatKwh(cooling)}° above the ${WeatherEnergyRules.COOLING_BASE_TEMPERATURE.toInt()}°C comfort level."
            else -> "No heating or cooling should be needed: today averages $average°C."
        }
    }
    MapLayer.SOLAR ->
        "Sunlight peaks at ${WeatherCalculator.peakSolar(weather).roundToInt()} W/m² around " +
            "${Formatters.formatClockHour(WeatherCalculator.peakSolarHour(weather))}."
}

/**
 * Draws a small rounded label such as "19°" to use as a map marker. MapLibre
 * markers take a bitmap, so the label is drawn onto one with the Android Canvas.
 */
private fun labelIcon(context: Context, text: String, color: Color): MarkerIcon {
    val density = context.resources.displayMetrics.density
    val width = (MapSettings.MARKER_WIDTH_DP * density).toInt()
    val height = (MapSettings.MARKER_HEIGHT_DP * density).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = color.toArgb()
    canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), height / 2f, height / 2f, paint)

    paint.color = OnAccent.toArgb()
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = height * 0.54f
    paint.typeface = ResourcesCompat.getFont(context, R.font.dinpro_bold)
    // Move the text down by half its height so it sits in the middle of the label.
    val textY = height / 2f - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(text, width / 2f, textY, paint)

    return IconFactory.getInstance(context).fromBitmap(bitmap)
}
