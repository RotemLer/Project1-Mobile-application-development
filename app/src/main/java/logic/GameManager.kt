package logic

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import com.example.tar1.Constants
import com.example.tar1.FinalActivity
import com.example.tar1.R
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView


class GameManager(private val activity: Activity,
                  private val lifeCount: Int = 3,
                  private val useTiltControl: Boolean = false,
                  private val speedType: String = "slow"
) {

    private lateinit var main_LBL_score: MaterialTextView
    private lateinit var main_BTN_Left: MaterialButton
    private lateinit var main_BTN_Right: MaterialButton
    private lateinit var main_IMG_hearts: Array<AppCompatImageView>
    private lateinit var main_IMG_grid: Array<AppCompatImageView>
    private lateinit var main_COIN_grid: Array<AppCompatImageView>

    private lateinit var penguin: AppCompatImageView

    private val fallDelay: Long = if (speedType == "fast") 50L else 300L

    var score: Int = 0
        private set

    var countCollision: Int = 0
        private set

    var penguinPosition: Int = Constants.GameLogic.PENGUIN_START_LOCATION
        private set

    val isGameOver: Boolean
        get() = countCollision == lifeCount

    private var currentToast: Toast? = null
    private var gameStopped = false

    private var numRows = 8
    private val numCols = 5

    fun initGame() {
        findViews()
        initViews()

        if (isGameOver) {
            Log.d("GameManager", "Game already over at start.")
            return
        }

        runSnowLoop()
    }

    private fun runSnowLoop() {
        if (isGameOver || gameStopped) {
            goToScoreActivity()
            return
        }

        startSnowFall {
            if (!gameStopped) {
                Handler(activity.mainLooper).postDelayed({
                    runSnowLoop()
                }, fallDelay)
            }
        }
    }

    private fun goToScoreActivity() {
        gameStopped = true
        Log.d("GameManager", "🛑 goToScoreActivity started")

        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w("GameManager", "⚠️ No location permission – saved with default")
            val fallback = GameRecord(System.currentTimeMillis(), score, 32.0853, 34.7818) // TLV
            ScoreStorage.saveScore(activity, fallback)
            launchScoreActivity()
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                val lat = location?.latitude ?: 32.0853
                val lng = location?.longitude ?: 34.7818

                Log.d("GameManager", "📍 location = $lat, $lng")

                val record = GameRecord(System.currentTimeMillis(), score, lat, lng)
                ScoreStorage.saveScore(activity, record)
                launchScoreActivity()
            }
            .addOnFailureListener {
                Log.e("GameManager", "❌ Location retrieval failure: ${it.message}")
                val fallback = GameRecord(System.currentTimeMillis(), score, 32.0853, 34.7818)
                ScoreStorage.saveScore(activity, fallback)
                launchScoreActivity()
            }
    }


    private fun launchScoreActivity() {
        val bundle = Bundle().apply {
            putInt(Constants.BundleKeys.SCORE_KEY, score)
            putString(Constants.BundleKeys.MESSAGE_KEY, "Game Over 😢")
        }

        val intent = Intent(activity, FinalActivity::class.java)
        intent.putExtras(bundle)

        if (!activity.isFinishing && !activity.isDestroyed) {
            Log.d("GameManager", "📤 launching ScoreActivity...")
            activity.startActivity(intent)
            Log.d("GameManager", "✅ intent sent")
            activity.finish()
        } else {
            Log.e("GameManager", "⛔ Attempting to switch to ScoreActivity when Activity is already closed")
        }
    }


    private fun showToast(message: String) {
        if (gameStopped || activity.isFinishing || activity.isDestroyed) return
        currentToast?.cancel()
        currentToast = Toast.makeText(activity.applicationContext, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    private fun refreshUI() {
        if (isGameOver || gameStopped) {
            Log.d("Game Status", "Game Over! Score: ${score}")
            return
        } else {
            main_LBL_score.text = score.toString()
            if (countCollision in 1..main_IMG_hearts.size) {
                main_IMG_hearts[main_IMG_hearts.size - countCollision].visibility = View.INVISIBLE
            }
        }
    }

    private fun findViews() {
        main_LBL_score = activity.findViewById(R.id.main_LBL_score)
        main_BTN_Left = activity.findViewById(R.id.main_BTN_Left)
        main_BTN_Right = activity.findViewById(R.id.main_BTN_Right)
        penguin = activity.findViewById(R.id.main_IMG_penguin)

        main_IMG_hearts = arrayOf(
            activity.findViewById(R.id.main_IMG_heart0),
            activity.findViewById(R.id.main_IMG_heart1),
            activity.findViewById(R.id.main_IMG_heart2)
        )

        val tempGrid = mutableListOf<AppCompatImageView>()
        val tempCoins = mutableListOf<AppCompatImageView>()

        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                val imgResName = "main_IMG_${row}${col}"
                val imgResId = activity.resources.getIdentifier(imgResName, "id", activity.packageName)
                if (imgResId != 0) {
                    val imageView = activity.findViewById<AppCompatImageView>(imgResId)
                    tempGrid.add(imageView)
                } else {
                    Log.e("GameManager", "❌ Missing view ID: $imgResName")
                }

                val coinResName = "main_COIN_${row}${col}"
                val coinResId = activity.resources.getIdentifier(coinResName, "id", activity.packageName)
                if (coinResId != 0) {
                    val coinView = activity.findViewById<AppCompatImageView>(coinResId)
                    tempCoins.add(coinView)
                } else {
                    Log.e("GameManager", "❌ Missing coin ID: $coinResName")
                }
            }
        }

        main_IMG_grid = tempGrid.toTypedArray()
        main_COIN_grid = tempCoins.toTypedArray()
    }

    private fun initViews() {
        main_LBL_score.text = score.toString()

        if (useTiltControl) {
            main_BTN_Left.visibility = View.GONE
            main_BTN_Right.visibility = View.GONE
        } else {
            main_BTN_Left.setOnClickListener {
                moveLeft()
                updatePenguinUI()
            }

            main_BTN_Right.setOnClickListener {
                moveRight()
                updatePenguinUI()
            }
        }

        main_IMG_grid.forEach { img: AppCompatImageView -> img.visibility = View.INVISIBLE }
        main_COIN_grid.forEach { coin: AppCompatImageView -> coin.visibility = View.GONE }

        updatePenguinUI()
    }

    fun updatePenguinUI() {
        val params = penguin.layoutParams as GridLayout.LayoutParams
        params.columnSpec = GridLayout.spec(penguinPosition)
        penguin.layoutParams = params
    }

    fun moveLeft() {
        if (penguinPosition > Constants.GameLogic.GRID_START) {
            penguinPosition--
        }
    }

    fun moveRight() {
        if (penguinPosition < Constants.GameLogic.GRID_END) {
            penguinPosition++
        }
    }

    fun checkCollision(row: Int, col: Int): Boolean {
        val penguinRow = numRows - 1
        val res = row == penguinRow && col == penguinPosition
        Log.d("COLLISION_CHECK", "Penguin at col = $penguinPosition, falling col = $col,res=$res ")
        return res
    }

    private fun vibrateOnHit() {
        if (gameStopped || activity.isFinishing || activity.isDestroyed) return
        val vibrator = activity.getSystemService(Activity.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun startSnowFall(onFinished: () -> Unit) {
        if (gameStopped) return

        val col = (0 until numCols).random()
        var row = 0
        val isSnowflake = (0..100).random() < 70
        val handler = Handler(activity.mainLooper)

        val runnable = object : Runnable {
            override fun run() {
                if (gameStopped || activity.isFinishing || activity.isDestroyed) return

                if (row < numRows - 1) {
                    val prevIndex = row * numCols + col
                    if (prevIndex in main_IMG_grid.indices) {
                        if (isSnowflake) main_IMG_grid[prevIndex].visibility = View.INVISIBLE
                        else main_COIN_grid[prevIndex].visibility = View.GONE
                    }

                    val nextIndex = (row + 1) * numCols + col
                    if (nextIndex in main_IMG_grid.indices) {
                        if (isSnowflake) main_IMG_grid[nextIndex].visibility = View.VISIBLE
                        else main_COIN_grid[nextIndex].visibility = View.VISIBLE
                    }

                    row++
                    handler.postDelayed(this, 100L)

                } else {
                    val lastIndex = row * numCols + col
                    if (lastIndex in main_IMG_grid.indices) {
                        if (isSnowflake) main_IMG_grid[lastIndex].visibility = View.INVISIBLE
                        else main_COIN_grid[lastIndex].visibility = View.GONE
                    }

                    if (isSnowflake) {
                        val isHit = checkCollision(row, col)
                        if (isHit) {
                            countCollision++
                            vibrateOnHit()
                            showToast("💔 You lost a life")
                        } else {
                            score += Constants.GameLogic.SCORE_DEFAULT
                            showToast("👏 You gained score: $score")
                        }
                    } else {
                        if(checkCollision(row, col)){
                            score += Constants.GameLogic.SCORE_COIN
                            showToast("💸 Dollar dropped: $score")

                        }
                    }
                    refreshUI()

                    onFinished()
                }
            }
        }

        handler.post(runnable)
    }
}
