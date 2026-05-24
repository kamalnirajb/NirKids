package com.nirkids.app.domain.usecase

import com.nirkids.app.data.local.AlphabetsDatabase
import com.nirkids.app.domain.repository.IAlphabetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SeedDataUseCaseTest {

    private lateinit var useCase: SeedDataUseCase
    private lateinit var repository: IAlphabetRepository
    private lateinit var database: AlphabetsDatabase

    @Before
    fun setup() {
        repository = mockk()
        database = mockk()
        useCase = SeedDataUseCase(repository, database)
    }

    @Test
    fun `invoke calls repository seedInitialData`() = runBlocking {
        coEvery { repository.seedInitialData(database) } returns Unit

        useCase()

        coVerify(exactly = 1) { repository.seedInitialData(database) }
    }
}
