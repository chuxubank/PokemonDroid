package top.chuxubank.pokemondroid.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import top.chuxubank.pokemondroid.data.local.PokeAppDataStore
import top.chuxubank.pokemondroid.domain.model.Pokemon
import top.chuxubank.pokemondroid.domain.model.PokemonSpecies
import top.chuxubank.pokemondroid.domain.usecase.SearchPokemonSpeciesUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val searchPokemonSpeciesUseCase: SearchPokemonSpeciesUseCase,
    private val pokeAppDataStore: PokeAppDataStore
) : ViewModel() {
    var uiState by mutableStateOf(UiState())
        private set
    private var searchJob: Job? = null
    val isFirstLaunch: StateFlow<Boolean> = pokeAppDataStore.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun completeFirstLaunch() {
        viewModelScope.launch {
            pokeAppDataStore.setFirstLaunchCompleted()
        }
    }

    fun onQueryChange(value: String) {
        uiState = uiState.copy(query = value)
        searchJob?.cancel()
        if (value.isBlank()) {
            uiState = uiState.copy(
                isLoading = false,
                species = emptyList(),
                totalCount = 0,
                currentPage = 0,
                selectedPokemon = null,
                errorMessage = null,
                hasSearched = false
            )
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            searchPage(0)
        }
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
        uiState = uiState.copy(hasSearched = true)
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val offset = page * uiState.pageSize
                val result = searchPokemonSpeciesUseCase(
                    uiState.query,
                    uiState.pageSize,
                    offset
                )
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
                    errorMessage = "Failed to load Pokémon data. Please try again.",
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
    val errorMessage: String? = null,
    val hasSearched: Boolean = false
)
