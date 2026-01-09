package top.chuxubank.pokemondroid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.chuxubank.pokemondroid.data.PokeApiClient
import top.chuxubank.pokemondroid.model.Pokemon
import top.chuxubank.pokemondroid.model.PokemonSpecies

class MainViewModel : ViewModel() {
    var uiState by mutableStateOf(UiState())
        private set

    fun onQueryChange(value: String) {
        uiState = uiState.copy(query = value)
    }

    fun search() {
        if (uiState.query.isBlank()) {
            return
        }
        searchPage(0)
    }

    fun loadNextPage() {
        val nextOffset = (uiState.currentPage + 1) * uiState.pageSize
        if (nextOffset >= uiState.totalCount) {
            return
        }
        searchPage(uiState.currentPage + 1)
    }

    fun loadPreviousPage() {
        if (uiState.currentPage == 0) {
            return
        }
        searchPage(uiState.currentPage - 1)
    }

    fun selectPokemon(pokemon: Pokemon) {
        uiState = uiState.copy(selectedPokemon = pokemon)
    }

    private fun searchPage(page: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val offset = page * uiState.pageSize
                val result = PokeApiClient.searchSpecies(uiState.query, uiState.pageSize, offset)
                uiState = uiState.copy(
                    isLoading = false,
                    species = result.species,
                    totalCount = result.totalCount,
                    currentPage = page,
                    selectedPokemon = null
                )
            } catch (exception: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Failed to load Pokemon data. Please try again.",
                    species = emptyList(),
                    totalCount = 0,
                    currentPage = 0
                )
            }
        }
    }
}

data class UiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val species: List<PokemonSpecies> = emptyList(),
    val totalCount: Int = 0,
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val selectedPokemon: Pokemon? = null,
    val errorMessage: String? = null
)
