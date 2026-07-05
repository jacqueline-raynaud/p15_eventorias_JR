package fr.quinquenaire.p15_eventorias_jr.di

import android.R.attr.level
import android.content.Context
import coil.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.quinquenaire.p15_eventorias_jr.data.network.MapsAuthInterceptor
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageMapsLoaderModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        mapsAuthInterceptor: MapsAuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(mapsAuthInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader = ImageLoader.Builder(context)
        .okHttpClient(okHttpClient)
        .build()
}
