package com.example.tar1

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton


class WelcomeActivity : AppCompatActivity() {

    private lateinit var welcome_IMG_logo: AppCompatImageView

    private lateinit var welcome_BTN_Start: MaterialButton

    private lateinit var welcome_BTN_Slow: MaterialButton

    private lateinit var welcome_BTN_Fast: MaterialButton

    private lateinit var welcome_BTN_Sensor: MaterialButton

    private var selectedSpeed: String = "slow" //byDefault

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViews()
        initViews()
        checkLocationPermission()

    }

    private fun initViews() {
        welcome_BTN_Slow.setOnClickListener {
            selectedSpeed = "slow"
            welcome_BTN_Slow.setBackgroundColor(ContextCompat.getColor(this, R.color.pink_300))
            welcome_BTN_Fast.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }

        welcome_BTN_Fast.setOnClickListener {
            selectedSpeed = "fast"
            welcome_BTN_Fast.setBackgroundColor(ContextCompat.getColor(this, R.color.pink_300))
            welcome_BTN_Slow.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        }


        welcome_BTN_Start.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("useTiltControl", false)
            intent.putExtra("speedType", selectedSpeed)
            startActivity(intent)
            finish()
        }

        welcome_BTN_Sensor.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("useTiltControl", true)
            intent.putExtra("speedType", selectedSpeed)
            startActivity(intent)
            finish()
        }
    }

    private fun findViews() {
        welcome_BTN_Start  = findViewById(R.id.welcome_BTN_Start)
        welcome_BTN_Sensor = findViewById(R.id.welcome_BTN_Sensor)
        welcome_BTN_Slow = findViewById(R.id.welcome_BTN_Slow)
        welcome_BTN_Fast = findViewById(R.id.welcome_BTN_Fast)
        welcome_IMG_logo = findViewById(R.id.welcome_IMG_logo)
    }

    private fun checkLocationPermission() {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "✔️ Location permission approved")
            } else {
                Toast.makeText(this, "⚠️ Without location permission – the map will not show real locations", Toast.LENGTH_LONG).show()
            }
        }
    }


}