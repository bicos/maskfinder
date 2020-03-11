package com.ravypark.maskfinder.model

import android.content.Context
import android.content.SharedPreferences

class Preference {
    companion object {
        private const val SHARED_PREFS_NAME = "com.ravypark.maskfinder.pref"

        private const val KEY_SHOW_NOTICE = "show_notice"

        private fun getInstance(context: Context): SharedPreferences {
            return context.getSharedPreferences(SHARED_PREFS_NAME, 0)
        }

        fun isShowNotice(context: Context): Boolean {
            return getInstance(context).getBoolean(KEY_SHOW_NOTICE, false)
        }

        fun setShowNotice(context: Context, isShow: Boolean) {
            getInstance(context).edit().putBoolean(KEY_SHOW_NOTICE, isShow).apply()
        }
    }
}