package top.chuxubank.pokemondroid.data.repository

import top.chuxubank.pokemondroid.data.remote.PokeApiClient
import top.chuxubank.pokemondroid.domain.model.SearchResult
import top.chuxubank.pokemondroid.domain.repository.PokemonRepository
import javax.inject.Inject

class PokemonRepositoryImpl @Inject constructor(
    private val apiClient: PokeApiClient
) : PokemonRepository {
    override suspend fun searchSpecies(query: String, limit: Int, offset: Int): SearchResult {
        return apiClient.searchSpecies(query, limit, offset)
    }
}
