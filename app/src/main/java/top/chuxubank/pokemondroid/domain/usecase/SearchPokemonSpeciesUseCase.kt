package top.chuxubank.pokemondroid.domain.usecase

import top.chuxubank.pokemondroid.domain.model.SearchResult
import top.chuxubank.pokemondroid.domain.repository.PokemonRepository
import javax.inject.Inject

class SearchPokemonSpeciesUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(query: String, limit: Int, offset: Int): SearchResult {
        return repository.searchSpecies(query, limit, offset)
    }
}
