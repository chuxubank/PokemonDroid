package top.chuxubank.pokemondroid.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import top.chuxubank.pokemondroid.domain.model.Pokemon
import top.chuxubank.pokemondroid.domain.model.PokemonSpecies
import top.chuxubank.pokemondroid.domain.model.SearchResult
import java.io.IOException
import javax.inject.Inject

class PokeApiClient @Inject constructor(
    private val client: OkHttpClient
) {
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private val query = """
        query SearchSpecies(${'$'}name: String!, ${'$'}limit: Int!, ${'$'}offset: Int!) {
          pokemon_v2_pokemonspecies(
            where: {name: {_ilike: ${'$'}name}},
            limit: ${'$'}limit,
            offset: ${'$'}offset
          ) {
            id
            name
            capture_rate
            pokemon_v2_pokemoncolor {
              id
              name
            }
            pokemon_v2_pokemons {
              id
              name
              pokemon_v2_pokemonabilities {
                id
                pokemon_v2_ability {
                  name
                }
              }
            }
          }
          pokemon_v2_pokemonspecies_aggregate(where: {name: {_ilike: ${'$'}name}}) {
            aggregate {
              count
            }
          }
        }
    """.trimIndent()

    suspend fun searchSpecies(name: String, limit: Int, offset: Int): SearchResult {
        if (name.isBlank()) {
            return SearchResult(totalCount = 0, species = emptyList())
        }

        val bodyJson = JSONObject()
            .put("query", query)
            .put(
                "variables",
                JSONObject()
                    .put("name", "%${name.trim().lowercase()}%")
                    .put("limit", limit)
                    .put("offset", offset)
            )

        val request = Request.Builder()
            .url(BASE_URL)
            .post(bodyJson.toString().toRequestBody(mediaType))
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected response ${'$'}{response.code}")
                }

                val rawBody = response.body?.string().orEmpty()
                val root = JSONObject(rawBody).getJSONObject("data")
                val speciesArray = root.getJSONArray("pokemon_v2_pokemonspecies")
                val count = root
                    .getJSONObject("pokemon_v2_pokemonspecies_aggregate")
                    .getJSONObject("aggregate")
                    .getInt("count")

                val speciesList = mutableListOf<PokemonSpecies>()
                for (index in 0 until speciesArray.length()) {
                    val speciesJson = speciesArray.getJSONObject(index)
                    val colorName = speciesJson
                        .optJSONObject("pokemon_v2_pokemoncolor")
                        ?.optString("name")

                    val pokemonArray = speciesJson.getJSONArray("pokemon_v2_pokemons")
                    val pokemons = mutableListOf<Pokemon>()
                    for (pokemonIndex in 0 until pokemonArray.length()) {
                        val pokemonJson = pokemonArray.getJSONObject(pokemonIndex)
                        val abilityArray = pokemonJson.getJSONArray("pokemon_v2_pokemonabilities")
                        val abilities = mutableListOf<String>()
                        for (abilityIndex in 0 until abilityArray.length()) {
                            val abilityJson = abilityArray.getJSONObject(abilityIndex)
                            val abilityName = abilityJson
                                .optJSONObject("pokemon_v2_ability")
                                ?.optString("name")
                                .orEmpty()
                            if (abilityName.isNotBlank()) {
                                abilities.add(abilityName)
                            }
                        }
                        pokemons.add(
                            Pokemon(
                                id = pokemonJson.getInt("id"),
                                name = pokemonJson.getString("name"),
                                abilities = abilities.distinct()
                            )
                        )
                    }

                    speciesList.add(
                        PokemonSpecies(
                            id = speciesJson.getInt("id"),
                            name = speciesJson.getString("name"),
                            captureRate = speciesJson.getInt("capture_rate"),
                            colorName = colorName,
                            pokemons = pokemons
                        )
                    )
                }

                SearchResult(totalCount = count, species = speciesList)
            }
        }
    }

    companion object {
        private const val BASE_URL = "https://beta.pokeapi.co/graphql/v1beta"
    }
}
