package top.chuxubank.pokemondroid.domain.usecase

import top.chuxubank.pokemondroid.domain.repository.FirstLaunchRepository
import javax.inject.Inject

class SetFirstLaunchCompleteUseCase @Inject constructor(
    private val repository: FirstLaunchRepository
) {
    suspend operator fun invoke() {
        repository.setFirstLaunchCompleted()
    }
}
