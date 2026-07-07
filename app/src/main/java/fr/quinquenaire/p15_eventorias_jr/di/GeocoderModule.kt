package fr.quinquenaire.p15_eventorias_jr.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.quinquenaire.p15_eventorias_jr.data.location.GeocoderManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeocoderModule {
    @Provides
    @Singleton
    fun provideGeocoderManager(
        @ApplicationContext context: Context
    ): GeocoderManager {
        return GeocoderManager(context)
    }
}