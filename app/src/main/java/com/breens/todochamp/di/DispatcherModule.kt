package com.breens.todochamp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    // providing coroutine dispatcher to the whole app
    // we provided an IO dispatcher which will be used in repository layer
    // to make the CRUD tasks in the background layer
}

@Retention
@Qualifier
annotation class IoDispatcher