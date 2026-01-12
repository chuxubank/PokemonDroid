package top.chuxubank.pokemondroid.data.remote

import com.apollographql.apollo.ApolloClient
import top.chuxubank.pokemondroid.domain.model.Pokemon
import top.chuxubank.pokemondroid.domain.model.PokemonSpecies
import top.chuxubank.pokemondroid.domain.model.SearchResult
import top.chuxubank.pokemondroid.graphql.SearchSpeciesQuery
import java.io.IOException
import javax.inject.Inject

class PokeApiClient @Inject constructor(
    private val apolloClient: ApolloClient
) {
    suspend fun searchSpecies(name: String, limit: Int, offset: Int): SearchResult {
        if (name.isBlank()) {
            return SearchResult(totalCount = 0, species = emptyList())
        }

        val response = apolloClient
            .query(
                SearchSpeciesQuery(
                    name = "%${name.trim().lowercase()}%",
                    limit = limit,
                    offset = offset
                )
            )
            .execute()

        response.exception?.let { throw IOException("Unexpected response", it) }
        if (!response.errors.isNullOrEmpty()) {
            throw IOException("GraphQL errors: ${'$'}{response.errors}")
        }

        val data = response.data ?: throw IOException("Empty response body")
        val count = data
            .pokemon_v2_pokemonspecies_aggregate
            ?.aggregate
            ?.count
            ?: 0

        val speciesList = data.pokemon_v2_pokemonspecies.map { species ->
            val pokemons = species.pokemon_v2_pokemons.map { pokemon ->
                val abilities = pokemon.pokemon_v2_pokemonabilities
                    .mapNotNull { ability ->
                        ability.pokemon_v2_ability?.name?.takeIf { it.isNotBlank() }
                    }
                    .distinct()

                Pokemon(
                    id = pokemon.id,
                    name = pokemon.name,
                    abilities = abilities
                )
            }

            PokemonSpecies(
                id = species.id,
                name = species.name,
                captureRate = species.capture_rate,
                colorName = species.pokemon_v2_pokemoncolor?.name,
                pokemons = pokemons
            )
        }

        return SearchResult(totalCount = count, species = speciesList)
    }

    companion object {
        const val BASE_URL = "https://beta.pokeapi.co/graphql/v1beta"
    }
}
