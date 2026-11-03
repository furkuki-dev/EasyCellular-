package com.werty.easycellular.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class SpeedRecord(
    val id: Long,
    val time: Long,
    val down: Double,
    val up: Double,
    val ping: Double?,
    val gen: Gen,
    val carrier: String?
)

/** История тестов в SharedPreferences (последние 200 записей). */
class HistoryStore(context: Context) {

    private val prefs = context.getSharedPreferences("ec_history", Context.MODE_PRIVATE)
    private val _records = MutableStateFlow(load())
    val records: StateFlow<List<SpeedRecord>> = _records.asStateFlow()

    fun add(record: SpeedRecord) {
        val list = (listOf(record) + _records.value).take(200)
        _records.value = list
        save(list)
    }

    fun clear() {
        _records.value = emptyList()
        save(emptyList())
    }

    private fun load(): List<SpeedRecord> = try {
        val arr = JSONArray(prefs.getString("items", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            SpeedRecord(
                id = o.getLong("id"),
                time = o.getLong("time"),
                down = o.getDouble("down"),
                up = o.getDouble("up"),
                ping = if (o.isNull("ping")) null else o.getDouble("ping"),
                gen = Gen.valueOf(o.getString("gen")),
                carrier = if (o.isNull("carrier")) null else o.getString("carrier")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    private fun save(list: List<SpeedRecord>) {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject()
                    .put("id", r.id)
                    .put("time", r.time)
                    .put("down", r.down)
                    .put("up", r.up)
                    .put("ping", r.ping ?: JSONObject.NULL)
                    .put("gen", r.gen.name)
                    .put("carrier", r.carrier ?: JSONObject.NULL)
            )
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }
}
