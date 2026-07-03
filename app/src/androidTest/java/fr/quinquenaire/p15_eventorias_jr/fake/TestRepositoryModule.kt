package fr.quinquenaire.p15_eventorias_jr.fake

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import fr.quinquenaire.p15_eventorias_jr.di.RepositoryModule
import fr.quinquenaire.p15_eventorias_jr.domain.repository.EventRepository
import javax.inject.Singleton

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]   // remplace le vrai module
)
@Module
abstract class TestRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        fake: FakeEventRepository
    ): EventRepository
}