package logic

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GameStorage {

    private val PREF_NAME = "game_storage"
    private val KEY_GAMES = "last_games"

    fun saveGame(context: Context, newGame: GameRecord) {
        val games = getGames(context).toMutableList()
        games.add(0, newGame)
        if (games.size > 10) games.removeAt(games.lastIndex)
        val json = Gson().toJson(games)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit() {
            putString(
                KEY_GAMES,
                json
            )
        }
    }

    fun getGames(context: Context): List<GameRecord> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GAMES, null)
        return if (json != null) {
            val type = object : TypeToken<List<GameRecord>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }

}