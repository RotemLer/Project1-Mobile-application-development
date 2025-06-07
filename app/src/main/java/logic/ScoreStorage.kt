package logic


import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ScoreStorage {

    private const val PREF_NAME = "score_storage"
    private const val KEY_SCORES = "last_scores"

    fun saveScore(context: Context, newScore: GameRecord) {
        val scores = getScores(context).toMutableList()
        scores.add(0, newScore)
        if (scores.size > 10) scores.removeAt(scores.lastIndex)
        val json = Gson().toJson(scores)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit() { putString(KEY_SCORES, json) }
    }

    fun getScores(context: Context): List<GameRecord> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SCORES, null)
        return if (json != null) {
            val type = object : TypeToken<List<GameRecord>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }


}