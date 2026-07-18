package fr.quinquenaire.p15_eventorias_jr.fake

import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import fr.quinquenaire.p15_eventorias_jr.di.FirebaseModule
import io.mockk.mockk
import javax.inject.Singleton

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirebaseModule::class]
)
@Module
object TestFirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return mockk(relaxed = true)
    }
}
