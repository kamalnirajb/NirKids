package com.nirkids.app.di

import android.content.Context
import androidx.room.Room
import com.nirkids.app.data.local.AlphabetsDatabase
import com.nirkids.app.data.repository.AlphabetRepositoryImpl
import com.nirkids.app.domain.repository.IAlphabetRepository
import com.nirkids.app.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAlphabetsDatabase(@ApplicationContext app: Context): AlphabetsDatabase {
        return Room.databaseBuilder(
            app,
            AlphabetsDatabase::class.java,
            "alphabets_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideAlphabetRepository(database: AlphabetsDatabase): IAlphabetRepository {
        return AlphabetRepositoryImpl(database)
    }

    @Provides
    @Singleton
    fun provideGetAlphabetUseCase(repository: IAlphabetRepository): GetAlphabetUseCase {
        return GetAlphabetUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetRandomLetterUseCase(repository: IAlphabetRepository): GetRandomLetterUseCase {
        return GetRandomLetterUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkLetterLearnedUseCase(repository: IAlphabetRepository): MarkLetterLearnedUseCase {
        return MarkLetterLearnedUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetProgressUseCase(repository: IAlphabetRepository): GetProgressUseCase {
        return GetProgressUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideParentGateValidateUseCase(): ParentGateValidateUseCase {
        return ParentGateValidateUseCase()
    }

    @Provides
    @Singleton
    fun provideSeedDataUseCase(repository: IAlphabetRepository, database: AlphabetsDatabase): SeedDataUseCase {
        return SeedDataUseCase(repository, database)
    }
}
