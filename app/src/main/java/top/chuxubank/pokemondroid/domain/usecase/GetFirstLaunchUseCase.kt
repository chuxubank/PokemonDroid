package top.chuxubank.pokemondroid.domain.usecase

import kotlinx.coroutines.flow.Flow
import top.chuxubank.pokemondroid.domain.repository.FirstLaunchRepository
import javax.inject.Inject

class GetFirstLaunchUseCase @Inject constructor(
    private val repository: FirstLaunchRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isFirstLaunch
}
