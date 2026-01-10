package top.chuxubank.pokemondroid.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.chuxubank.pokemondroid.domain.repository.FirstLaunchRepository
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "pokemon_prefs")
private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")

class FirstLaunchDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : FirstLaunchRepository {
    override val isFirstLaunch: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[FIRST_LAUNCH_KEY] ?: true }

    override suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { prefs ->
            prefs[FIRST_LAUNCH_KEY] = false
        }
    }
}
