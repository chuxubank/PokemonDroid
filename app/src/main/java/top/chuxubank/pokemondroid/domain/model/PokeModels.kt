package top.chuxubank.pokemondroid.domain.model

data class SearchResult(
    val totalCount: Int,
    val species: List<PokemonSpecies>
)

data class PokemonSpecies(
    val id: Int,
    val name: String,
    val captureRate: Int,
    val colorName: String?,
    val pokemons: List<Pokemon>
)

data class Pokemon(
    val id: Int,
    val name: String,
    val abilities: List<String>
)
