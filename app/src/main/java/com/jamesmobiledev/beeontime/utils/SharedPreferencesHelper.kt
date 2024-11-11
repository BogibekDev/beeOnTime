package com.jamesmobiledev.beeontime.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jamesmobiledev.beeontime.model.Branch
import java.lang.reflect.Type


object SharedPreferencesHelper {
    private const val PREFS_NAME = "beeOnTimePrefs"
    private const val LOCATIONS_KEY = "locations"
    private const val EMAILS_KEY = "emails"
    private const val BRANCH_KEY = "branch"
    private const val USERNAME_KEY = "userName"
    private const val UID = "userId"
    private const val USER_STATUS_KEY = "userStatus"

    fun saveLocationList(context: Context, locationList: List<String>?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(locationList)
        editor.putString(LOCATIONS_KEY, json)
        editor.apply()
    }


    fun saveUserEmails(context: Context, email: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val list = getUserEmails(context)
        if (!list.contains(email)) {
            val new = list.toMutableList().also { it.add(email) }
            val json: String = gson.toJson(new)
            editor.putString(EMAILS_KEY, json)
        }
        editor.apply()

    }

    fun saveBranch(context: Context, branch: Branch) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(BRANCH_KEY, Gson().toJson(branch))
        editor.apply()

    }

    fun getBranch(context: Context): Branch {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(BRANCH_KEY, null)
        return Gson().fromJson(json, Branch::class.java)
    }

    fun getLocationList(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(LOCATIONS_KEY, null)
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        val locationList: List<String>? = gson.fromJson(json, type)
        return locationList ?: ArrayList()
    }

    fun getUserEmails(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(EMAILS_KEY, null)
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        val locationList: List<String>? = gson.fromJson(json, type)
        return locationList ?: ArrayList()
    }

    fun saveUserName(context: Context, username: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(USERNAME_KEY, username)
        editor.apply()
    }

    fun getUserName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(USERNAME_KEY, "") ?: ""
    }

    fun removeUserName(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(USERNAME_KEY)
        editor.apply()
    }

    fun getUserId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(UID, "") ?: ""
    }

    fun saveUserId(context: Context, uid: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(UID, uid)
        editor.apply()
    }

    fun removeUserId(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(UID)
        editor.apply()
    }

    fun getUserStatus(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(USER_STATUS_KEY, "") ?: ""
    }

    fun saveUserStatus(context: Context, uid: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(USER_STATUS_KEY, uid)
        editor.apply()
    }

    fun removeUserStatus(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(USER_STATUS_KEY)
        editor.apply()
    }

    fun clearSharedPref(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }


}

