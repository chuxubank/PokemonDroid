package top.chuxubank.pokemondroid.domain.repository

import top.chuxubank.pokemondroid.domain.model.SearchResult

interface PokemonRepository {
    suspend fun searchSpecies(query: String, limit: Int, offset: Int): SearchResult
}
