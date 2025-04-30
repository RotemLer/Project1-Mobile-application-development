package logic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import com.example.tar1.Constants
import android.app.Activity
import com.example.tar1.R
import com.example.tar1.ScoreActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView


class GameManager(private val activity: Activity, private val lifeCount: Int = 3) {

    private lateinit var main_LBL_score: MaterialTextView
    private lateinit var main_BTN_Left: MaterialButton
    private lateinit var main_BTN_Right: MaterialButton
    private lateinit var main_IMG_hearts: Array<AppCompatImageView>
    private lateinit var main_IMG_grid: Array<AppCompatImageView>

    private lateinit var penguin: AppCompatImageView

    var score: Int = 0
        private set

    var countCollision: Int = 0
        private set

    var penguinPosition: Int = Constants.GameLogic.PENGUIN_START_LOCATION
        private set

    val isGameOver: Boolean
        get() = countCollision == lifeCount

    private var currentToast: Toast? = null



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
        if (isGameOver) {
            goToScoreActivity()
            return
        }

        startSnowFall {
            android.os.Handler(activity.mainLooper).postDelayed({
                runSnowLoop()
            }, 300L)
        }
    }

    private fun goToScoreActivity() {
        Log.d("GameManager", "🏁 GAME OVER – launching ScoreActivity with score = $score")

        val bundle = Bundle().apply {
            putInt(Constants.BundleKeys.SCORE_KEY, score)
            putString(Constants.BundleKeys.MESSAGE_KEY, "Game Over 😢")
        }

        val intent = Intent(activity, ScoreActivity::class.java)
        intent.putExtras(bundle)
        activity.startActivity(intent)
        activity.finish()
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

        main_IMG_grid = arrayOf(
            activity.findViewById(R.id.main_IMG_00),
            activity.findViewById(R.id.main_IMG_01),
            activity.findViewById(R.id.main_IMG_02),
            activity.findViewById(R.id.main_IMG_10),
            activity.findViewById(R.id.main_IMG_11),
            activity.findViewById(R.id.main_IMG_12),
            activity.findViewById(R.id.main_IMG_20),
            activity.findViewById(R.id.main_IMG_21),
            activity.findViewById(R.id.main_IMG_22),
            activity.findViewById(R.id.main_IMG_30),
            activity.findViewById(R.id.main_IMG_31),
            activity.findViewById(R.id.main_IMG_32)
        )
    }

    private fun initViews() {
        main_LBL_score.text = score.toString()

        main_BTN_Left.setOnClickListener {
            moveLeft()
            updatePenguinUI()
        }

        main_BTN_Right.setOnClickListener {
            moveRight()
            updatePenguinUI()
        }


        for (img in main_IMG_grid) {
            img.visibility = View.INVISIBLE
        }
    }

    private fun updatePenguinUI() {
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

    fun checkCollision(snowflakeCol: Int): Boolean {
        return if (penguinPosition == snowflakeCol) {
            countCollision++
            true
        } else {
            score += Constants.GameLogic.SCORE_DEFAULT
            false
        }
    }

    private fun vibrateOnHit() {
        val vibrator = activity.getSystemService(Activity.VIBRATOR_SERVICE) as android.os.Vibrator
            vibrator.vibrate(
                android.os.VibrationEffect.createOneShot(
                    200,
                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )

    }

    private fun startSnowFall(onFinished: () -> Unit) {
        val col = (0..2).random()
        var row = 0

        val handler = android.os.Handler(activity.mainLooper)

        val runnable = object : Runnable {
            override fun run() {
                if (row > 0) {
                    val prevIndex = (row - 1) * 3 + col
                    main_IMG_grid[prevIndex].visibility = View.INVISIBLE
                }

                if (row <= 3) {
                    val index = row * 3 + col
                    main_IMG_grid[index].visibility = View.VISIBLE
                    row++
                    handler.postDelayed(this, 100L)
                } else {
                    val lastIndex = 3 * 3 + col
                    main_IMG_grid[lastIndex].visibility = View.INVISIBLE

                    val isHit = checkCollision(col)
                    refreshUI()

                    if (isHit) {
                        vibrateOnHit()
                        showToast("💔 You lost a life")
                    } else {
                        showToast("👏 You gained score: $score")
                    }


                    onFinished()
                }
            }
        }
        handler.post(runnable)
    }

    private fun showToast(message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(activity, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    private fun refreshUI() {
        if (isGameOver) {
            Log.d("Game Status", "Game Over! Score: ${score}")


            return
        } else {
            main_LBL_score.text = score.toString()

            if (countCollision in 1..main_IMG_hearts.size) {
                main_IMG_hearts[main_IMG_hearts.size - countCollision].visibility = View.INVISIBLE
            }
        }
    }

}