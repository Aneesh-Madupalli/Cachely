package com.cachely.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val ASSISTED_PREFERRED = booleanPreferencesKey("assisted_preferred")

class PreferencesRepository(private val context: Context) {

    val assistedPreferred: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ASSISTED_PREFERRED] ?: false
    }

    suspend fun setAssistedPreferred(enabled: Boolean) {
        context.dataStore.edit { it[ASSISTED_PREFERRED] = enabled }
    }
}
