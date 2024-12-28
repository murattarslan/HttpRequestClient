package com.murattarslan.httprequestclient

import org.json.JSONObject
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

fun Serializable.toJson(): String {
    return try {
        val jsonObject = JSONObject()
        val kClass = this::class
        val properties = kClass.memberProperties
        properties.forEach { property ->
            when (val value = property.getter.call(this)){
                is String -> jsonObject.put(property.name, value)
                is Int -> jsonObject.put(property.name, value)
                is Boolean -> jsonObject.put(property.name, value)
                is Double -> jsonObject.put(property.name, value)
                is Serializable -> jsonObject.put(property.name, value.toJsonObject())
                else -> jsonObject.put(property.name, null)
            }
        }
        jsonObject.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        "{}"
    }
}

fun Serializable.toJsonObject(): JSONObject {
    val jsonObject = JSONObject()
    return try {
        val kClass = this::class
        val properties = kClass.memberProperties
        properties.forEach { property ->
            when (val value = property.getter.call(this)){
                is String -> jsonObject.put(property.name, value)
                is Int -> jsonObject.put(property.name, value)
                is Boolean -> jsonObject.put(property.name, value)
                is Double -> jsonObject.put(property.name, value)
                is Serializable -> jsonObject.put(property.name, value.toJsonObject())
                else -> jsonObject.put(property.name, null)
            }
        }
        jsonObject
    } catch (e: Exception) {
        e.printStackTrace()
        jsonObject
    }
}

fun <T : Any> String.fromJson(classOfT: KClass<T>): T? {
    return try {
        val jsonObject = JSONObject(this)
        val constructor = classOfT.primaryConstructor
        val args = constructor?.parameters?.associateWith { parameter ->
            val property = classOfT.memberProperties.find { it.name == parameter.name }
            property?.let {
                when (it.returnType.toString()) {
                    "kotlin.String" -> jsonObject.getString(it.name)
                    "kotlin.Int" -> jsonObject.getInt(it.name)
                    "kotlin.Boolean" -> jsonObject.getBoolean(it.name)
                    "kotlin.Double" -> jsonObject.getDouble(it.name)
                    else -> null //(jsonObject.get(it.name) as? JSONObject).toString().fromJson(it.returnType::class)
                }
            }
        }
        constructor?.callBy(args ?: emptyMap())
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}