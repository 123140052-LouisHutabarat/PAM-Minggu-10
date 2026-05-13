package org.example.project.data

import com.russhwolf.settings.Settings

class AppSettings(private val settings: Settings = Settings()) {

    companion object {
        private const val KEY_DARK_MODE      = "dark_mode"
        private const val KEY_SORT_ORDER     = "sort_order"
        private const val KEY_SHOW_FAV_FIRST = "show_fav_first"
    }

    var isDarkMode: Boolean
        get() = settings.getBoolean(KEY_DARK_MODE, false)
        set(value) { settings.putBoolean(KEY_DARK_MODE, value) }

    var sortOrder: String
        get() = settings.getString(KEY_SORT_ORDER, "newest")
        set(value) { settings.putString(KEY_SORT_ORDER, value) }

    var showFavoritesFirst: Boolean
        get() = settings.getBoolean(KEY_SHOW_FAV_FIRST, false)
        set(value) { settings.putBoolean(KEY_SHOW_FAV_FIRST, value) }
}