package com.example.tar1

import android.os.Bundle
import android.util.Log
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import logic.GameRecord
import logic.ScoreStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinalActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var tableLayout: TableLayout
    private lateinit var gameList: List<GameRecord>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_final)

            gameList = ScoreStorage.getScores(this)

            tableLayout = findViewById(R.id.final_game_table)
            mapView = findViewById(R.id.mapView)
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this)

            setupTable()

        } catch (e: Exception) {
            Log.e("FinalActivity", "\uD83D\uDCA5 error onCreate: ${e.message}", e)
        }
    }

    private fun setupTable() {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tableLayout.removeAllViews()

        val headerRow = TableRow(this)
        listOf("date", "score", "location").forEach {
            val text = TextView(this).apply {
                text = it
                setPadding(16, 16, 16, 16)
            }
            headerRow.addView(text)
        }
        tableLayout.addView(headerRow)

        gameList.forEach { game ->
            val row = TableRow(this)

            val date = TextView(this).apply { text = formatter.format(Date(game.time)) }
            val score = TextView(this).apply { text = game.score.toString() }
            val location = TextView(this).apply { text = "${game.lat}, ${game.lng}" }

            listOf(date, score, location).forEach {
                it.setPadding(16, 16, 16, 16)
                row.addView(it)
            }

            row.setOnClickListener {
                if (::map.isInitialized) {
                    if (game.lat == 0.0 && game.lng == 0.0) {
                        Toast.makeText(this, "⛔ location isn't available", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val pos = game.toLatLng()
                    Log.d("FinalActivity", "📍 jump to location: $pos")
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 13f))
                    map.clear()
                    map.addMarker(MarkerOptions().position(pos).title("game location"))
                }
            }

            tableLayout.addView(row)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (gameList.isNotEmpty()) {
            gameList.forEachIndexed { index, game ->
                val pos = game.toLatLng()
                map.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title("game ${index + 1} - score: ${game.score}")
                )
            }

            val latest = gameList.first()
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latest.toLatLng(), 12f))
        } else {
            val defaultLocation = LatLng(32.0853, 34.7818)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
