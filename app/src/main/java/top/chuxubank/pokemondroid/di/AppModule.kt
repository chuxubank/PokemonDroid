package top.chuxubank.pokemondroid.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import top.chuxubank.pokemondroid.data.local.FirstLaunchDataStore
import top.chuxubank.pokemondroid.data.repository.PokemonRepositoryImpl
import top.chuxubank.pokemondroid.domain.repository.FirstLaunchRepository
import top.chuxubank.pokemondroid.domain.repository.PokemonRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindings {
    @Binds
    @Singleton
    abstract fun bindPokemonRepository(
        impl: PokemonRepositoryImpl
    ): PokemonRepository

    @Binds
    @Singleton
    abstract fun bindFirstLaunchRepository(
        impl: FirstLaunchDataStore
    ): FirstLaunchRepository
}
