package com.example.cagbclabex6

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cagbclabex6.ui.theme.Cagbclabex6Theme
import kotlin.math.sqrt

// Gyroscope magnitude (rad/s) above which the device is considered rotating.
private const val ROTATION_THRESHOLD = 1.0f

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var temperatureSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    // Compose-observable state
    private var temperature by mutableStateOf<Float?>(null)

    private var gyroX by mutableStateOf(0f)
    private var gyroY by mutableStateOf(0f)
    private var gyroZ by mutableStateOf(0f)

    private var accelX by mutableStateOf(0f)
    private var accelY by mutableStateOf(0f)
    private var accelZ by mutableStateOf(0f)

    private var temperatureAvailable by mutableStateOf(false)
    private var gyroscopeAvailable by mutableStateOf(false)
    private var accelerometerAvailable by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager

        temperatureSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        gyroscopeSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometerSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        temperatureAvailable = temperatureSensor != null
        gyroscopeAvailable = gyroscopeSensor != null
        accelerometerAvailable = accelerometerSensor != null

        setContent {
            Cagbclabex6Theme {
                SensorScreen(
                    temperature = temperature,
                    temperatureSensor = temperatureSensor,
                    temperatureAvailable = temperatureAvailable,
                    gyroX = gyroX,
                    gyroY = gyroY,
                    gyroZ = gyroZ,
                    gyroscopeSensor = gyroscopeSensor,
                    gyroscopeAvailable = gyroscopeAvailable,
                    accelX = accelX,
                    accelY = accelY,
                    accelZ = accelZ,
                    accelerometerSensor = accelerometerSensor,
                    accelerometerAvailable = accelerometerAvailable
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        listOfNotNull(temperatureSensor, gyroscopeSensor, accelerometerSensor)
            .forEach { sensor ->
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                temperature = event.values[0]
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]
                gyroY = event.values[1]
                gyroZ = event.values[2]
            }

            Sensor.TYPE_ACCELEROMETER -> {
                accelX = event.values[0]
                accelY = event.values[1]
                accelZ = event.values[2]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not required for this lab.
    }
}

@Composable
fun SensorScreen(
    temperature: Float?,
    temperatureSensor: Sensor?,
    temperatureAvailable: Boolean,
    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,
    gyroscopeSensor: Sensor?,
    gyroscopeAvailable: Boolean,
    accelX: Float,
    accelY: Float,
    accelZ: Float,
    accelerometerSensor: Sensor?,
    accelerometerAvailable: Boolean
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Android Sensor Monitor",
                style = MaterialTheme.typography.headlineMedium
            )

            SensorValueCard(
                title = "Ambient Temperature",
                available = temperatureAvailable,
                unavailableMessage = "Temperature sensor is not available.",
                sensor = temperatureSensor
            ) {
                if (temperature == null) {
                    Text("Waiting for sensor reading...")
                } else {
                    Text(
                        text = "%.1f °C".format(temperature),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            SensorValueCard(
                title = "Gyroscope",
                available = gyroscopeAvailable,
                unavailableMessage = "Gyroscope is not available.",
                sensor = gyroscopeSensor
            ) {
                Text("X: %.3f rad/s".format(gyroX))
                Text("Y: %.3f rad/s".format(gyroY))
                Text("Z: %.3f rad/s".format(gyroZ))
                Spacer(Modifier.height(8.dp))
                MovementIndicator(
                    rotating = sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ) >
                        ROTATION_THRESHOLD
                )
            }

            SensorValueCard(
                title = "Accelerometer",
                available = accelerometerAvailable,
                unavailableMessage = "Accelerometer is not available.",
                sensor = accelerometerSensor
            ) {
                Text("X: %.2f m/s²".format(accelX))
                Text("Y: %.2f m/s²".format(accelY))
                Text("Z: %.2f m/s²".format(accelZ))
            }
        }
    }
}

/**
 * Shared card used by every sensor: title, then either the unavailable message
 * or the readings followed by the sensor's hardware details.
 */
@Composable
fun SensorValueCard(
    title: String,
    available: Boolean,
    unavailableMessage: String,
    sensor: Sensor?,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(8.dp))

            if (!available || sensor == null) {
                Text(unavailableMessage)
            } else {
                content()
                Spacer(Modifier.height(8.dp))
                SensorDetails(sensor)
            }
        }
    }
}

@Composable
fun SensorDetails(sensor: Sensor) {
    val style = MaterialTheme.typography.bodySmall
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Text("Name: ${sensor.name}", style = style, color = color)
    Text("Vendor: ${sensor.vendor}", style = style, color = color)
    Text("Max range: ${sensor.maximumRange}", style = style, color = color)
    Text("Resolution: ${sensor.resolution}", style = style, color = color)
}

@Composable
fun MovementIndicator(rotating: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(
                    color = if (rotating) Color.Red else Color(0xFF2E7D32),
                    shape = CircleShape
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(if (rotating) "Rotating" else "Still")
    }
}
