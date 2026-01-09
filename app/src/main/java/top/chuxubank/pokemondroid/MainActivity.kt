package top.chuxubank.pokemondroid

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import top.chuxubank.pokemondroid.model.Pokemon
import top.chuxubank.pokemondroid.model.PokemonSpecies
import top.chuxubank.pokemondroid.ui.theme.PokémonDroidTheme
import top.chuxubank.pokemondroid.ui.colorForPokemonColorName
import top.chuxubank.pokemondroid.ui.readableTextColor
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokémonDroidTheme {
                PokeApp()
            }
        }
    }
}

@Composable
fun PokeApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("pokemon_prefs", Context.MODE_PRIVATE) }
    var showSplash by rememberSaveable { mutableStateOf(prefs.getBoolean("first_launch", true)) }

    if (showSplash) {
        SplashScreen(
            onContinue = {
                prefs.edit { putBoolean("first_launch", false) }
                showSplash = false
            }
        )
        return
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                state = viewModel.uiState,
                onQueryChange = viewModel::onQueryChange,
                onNextPage = viewModel::loadNextPage,
                onPreviousPage = viewModel::loadPreviousPage,
                onPokemonClick = { pokemon ->
                    viewModel.selectPokemon(pokemon)
                    navController.navigate("detail")
                }
            )
        }
        composable("detail") {
            DetailScreen(
                navController = navController,
                pokemon = viewModel.uiState.selectedPokemon
            )
        }
    }
}

@Composable
fun SplashScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1B1B))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to PokémonDroid",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Search Pokémon species and jump into details.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFE1E1E1),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onContinue) {
            Text(text = "Start")
        }
    }
}

@Composable
fun HomeScreen(
    state: UiState,
    onQueryChange: (String) -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onPokemonClick: (Pokemon) -> Unit
) {
    val totalPages = if (state.totalCount == 0) 1 else
        (state.totalCount + state.pageSize - 1) / state.pageSize
    val canGoNext = (state.currentPage + 1) * state.pageSize < state.totalCount

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Pokémon Search",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                label = { Text(text = "Search species") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Searching as you type…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_loading),
                        contentDescription = "Loading",
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Loading Pokémon...", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                if (state.species.isEmpty() && state.query.isNotBlank() && state.hasSearched) {
                    Text(
                        text = "No species found. Try another search.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = true)
                ) {
                    items(state.species, key = { it.id }) { species ->
                        SpeciesCard(
                            species = species,
                            onPokemonClick = onPokemonClick
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPreviousPage,
                        enabled = state.currentPage > 0
                    ) {
                        Text(text = "Prev")
                    }
                    Text(
                        text = "Page ${state.currentPage + 1} of ${totalPages}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = onNextPage,
                        enabled = canGoNext
                    ) {
                        Text(text = "Next")
                    }
                }
            }
        }
    }
}

@Composable
fun SpeciesCard(
    species: PokemonSpecies,
    onPokemonClick: (Pokemon) -> Unit
) {
    val background = colorForPokemonColorName(species.colorName)
    val textColor = readableTextColor(background)

    Card(
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = species.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "Capture rate: ${species.captureRate}",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pokémon",
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            species.pokemons.forEach { pokemon ->
                Text(
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPokemonClick(pokemon) }
                        .padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun DetailScreen(navController: NavController, pokemon: Pokemon?) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1B1B))
            ) {
                Text(text = "Back", color = Color.White)
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (pokemon == null) {
                Text(
                    text = "No Pokémon selected.",
                    style = MaterialTheme.typography.bodyLarge
                )
                return@Column
            }

            Text(
                text = pokemon.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Abilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (pokemon.abilities.isEmpty()) {
                Text(text = "No abilities listed.")
            } else {
                pokemon.abilities.forEach { ability ->
                    Text(
                        text = ability.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    PokémonDroidTheme {
        HomeScreen(
            state = UiState(
                query = "pika",
                species = emptyList(),
                totalCount = 0
            ),
            onQueryChange = {},
            onNextPage = {},
            onPreviousPage = {},
            onPokemonClick = {}
        )
    }
}
