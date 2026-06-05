package com.example.core.database

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

data class RoutineStep(
    val name: String,
    val isCompleted: Boolean = false
)

data class ChallengeConfig(
    val taskName: String = "",
    val attemptsAllowed: Int = 3,
    val variables: Map<String, String> = emptyMap()
)

class AriseTypeConverters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return ""
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",")
    }

    @TypeConverter
    fun fromRoutineSteps(value: List<RoutineStep>?): String {
        if (value == null) return "[]"
        val arr = JSONArray()
        value.forEach { step ->
            val obj = JSONObject()
            obj.put("name", step.name)
            obj.put("isCompleted", step.isCompleted)
            arr.put(obj)
        }
        return arr.toString()
    }

    @TypeConverter
    fun toRoutineSteps(value: String?): List<RoutineStep> {
        if (value.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<RoutineStep>()
        try {
            val arr = JSONArray(value)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    RoutineStep(
                        name = obj.getString("name"),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
        } catch (e: Exception) {
            // ignore
        }
        return list
    }

    @TypeConverter
    fun fromChallengeConfig(value: ChallengeConfig?): String {
        if (value == null) return "{}"
        val obj = JSONObject()
        obj.put("taskName", value.taskName)
        obj.put("attemptsAllowed", value.attemptsAllowed)
        val varsObj = JSONObject()
        value.variables.forEach { (k, v) ->
            varsObj.put(k, v)
        }
        obj.put("variables", varsObj)
        return obj.toString()
    }

    @TypeConverter
    fun toChallengeConfig(value: String?): ChallengeConfig {
        if (value.isNullOrEmpty()) return ChallengeConfig()
        return try {
            val obj = JSONObject(value)
            val taskName = obj.optString("taskName", "")
            val attemptsAllowed = obj.optInt("attemptsAllowed", 3)
            val vars = mutableMapOf<String, String>()
            if (obj.has("variables")) {
                val varsObj = obj.getJSONObject("variables")
                val keys = varsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    vars[key] = varsObj.getString(key)
                }
            }
            ChallengeConfig(taskName, attemptsAllowed, vars)
        } catch (e: Exception) {
            ChallengeConfig()
        }
    }
}
