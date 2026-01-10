package top.chuxubank.pokemondroid.domain.repository

import kotlinx.coroutines.flow.Flow

interface FirstLaunchRepository {
    val isFirstLaunch: Flow<Boolean>
    suspend fun setFirstLaunchCompleted()
}
