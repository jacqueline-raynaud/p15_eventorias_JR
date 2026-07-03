package fr.quinquenaire.p15_eventorias_jr.fake

import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import fr.quinquenaire.p15_eventorias_jr.android.di.FirebaseModule

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirebaseModule::class]    // remplace le vrai module Firebase
)
@Module
object TestFirebaseModule {
    // vide — on ne fournit rien, le FakeRepository n'en a pas besoin
}