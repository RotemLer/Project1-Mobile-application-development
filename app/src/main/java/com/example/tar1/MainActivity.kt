package com.example.tar1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import logic.GameManager

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var gameManager: GameManager

    private var useTiltControl: Boolean = true

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var lastMoveTime: Long = 0
    private val moveCooldown: Long = 300

    private lateinit var speedType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        useTiltControl = intent.getBooleanExtra("useTiltControl", false)
        speedType = intent.getStringExtra("speedType") ?: "slow"

        gameManager = GameManager(this, lifeCount = 3, useTiltControl = useTiltControl, speedType = speedType)
        gameManager.initGame()

        if (useTiltControl) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
    }

    override fun onResume() {
        super.onResume()
        if (useTiltControl) {
            accelerometer?.also {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (useTiltControl) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!useTiltControl || event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val now = System.currentTimeMillis()
        if (now - lastMoveTime < moveCooldown) return

        val x = event.values[0]

        if (x > 2.5) {
            gameManager.moveLeft()
            gameManager.updatePenguinUI()
            lastMoveTime = now
        } else if (x < -2.5) {
            gameManager.moveRight()
            gameManager.updatePenguinUI()
            lastMoveTime = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}
