package fr.quinquenaire.p15_eventorias_jr.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.quinquenaire.p15_eventorias_jr.data.repository.EventRepositoryImpl
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository
}