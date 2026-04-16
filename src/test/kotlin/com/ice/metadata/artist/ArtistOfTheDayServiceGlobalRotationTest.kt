package com.ice.metadata.artist

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class ArtistOfTheDayServiceGlobalRotationTest {

    @Test
    fun `it should find today's artist`() {
        val repository = mock<ArtistAliasRepository>()

        val today = java.time.LocalDate.now()
        val artistAlias = buildArtistAlias(artistOfTheDayDate = today)
        whenever(repository.findByArtistOfTheDayDate(any())).thenReturn(artistAlias)

        val service = ArtistOfTheDayServiceGlobalRotation(repository)

        val artistOfTheDay = service.findArtistOfTheDay(today)

        assertThat(artistOfTheDay).isEqualTo(artistAlias)
    }

    @Test
    fun `it should find an artist who has not been before when today's artist is not selected`() {
        val repository = mock<ArtistAliasRepository>()

        val today = java.time.LocalDate.now()
        val artistAlias = buildArtistAlias(artistOfTheDayDate = today)
        whenever(repository.findByArtistOfTheDayDate(any())).thenReturn(null)
        whenever(repository.findAllByArtistOfTheDayDateIsNull(any()))
            .thenReturn(PageImpl(listOf(artistAlias)))

        val service = ArtistOfTheDayServiceGlobalRotation(repository)

        val artistOfTheDay = service.findArtistOfTheDay(today)

        assertThat(artistOfTheDay).isEqualTo(artistAlias)
    }

    @Test
    fun `it should find the artist who has not been the longest when there is no artist of the day and all artist been before`() {
        val repository = mock<ArtistAliasRepository>()

        val today = java.time.LocalDate.now()
        val artistAlias = buildArtistAlias(artistOfTheDayDate = today)
        whenever(repository.findByArtistOfTheDayDate(any())).thenReturn(null)
        whenever(repository.findAllByArtistOfTheDayDateIsNull(any()))
            .thenReturn(PageImpl(listOf()))
        whenever(repository.findAll(any<Pageable>()))
            .thenReturn(PageImpl(listOf(artistAlias)))

        val service = ArtistOfTheDayServiceGlobalRotation(repository)

        val artistOfTheDay = service.findArtistOfTheDay(today)

        assertThat(artistOfTheDay).isEqualTo(artistAlias)
    }

    @Test
    fun `it should update the artist with today's date if selected `() {
        val repository = mock<ArtistAliasRepository>()

        val today = java.time.LocalDate.now()
        val artistAlias = buildArtistAlias()
        whenever(repository.findByArtistOfTheDayDate(any())).thenReturn(null)
        whenever(repository.findAllByArtistOfTheDayDateIsNull(any()))
            .thenReturn(PageImpl(listOf(artistAlias)))
        whenever(repository.save(any()))
            .thenReturn(artistAlias.copy(artistOfTheDayDate = today))

        val service = ArtistOfTheDayServiceGlobalRotation(repository)

        val artistOfTheDay = service.findArtistOfTheDay(today)

        assertThat(artistOfTheDay.artistOfTheDayDate).isEqualTo(today)
    }


    @Test
    fun `it should try to fetch today's artist of the day again when update fails`() {
        val repository = mock<ArtistAliasRepository>()

        val today = java.time.LocalDate.now()
        val artistAlias = buildArtistAlias(name="unseen artist")
        val artistAliasOfTheDay = buildArtistAlias(name="today's artist", artistOfTheDayDate = today)
        whenever(repository.findByArtistOfTheDayDate(any()))
            .thenReturn(null) // first not found
            .thenReturn(artistAliasOfTheDay) // try again
        whenever(repository.findAllByArtistOfTheDayDateIsNull(any()))
            .thenReturn(PageImpl(listOf(artistAlias)))
        whenever(repository.save(any()))
            .thenThrow(DataIntegrityViolationException("Duplicate key"))

        val service = ArtistOfTheDayServiceGlobalRotation(repository)

        val artistOfTheDay = service.findArtistOfTheDay(today)

        assertThat(artistOfTheDay).isEqualTo(artistAliasOfTheDay)
    }

    @Test
    fun `it should throw an error when update fails and no today's artist found`() {
        val repository = mock<ArtistAliasRepository>()

        val today = java.time.LocalDate.now()
        val artistAlias = buildArtistAlias(name="unseen artist")
        whenever(repository.findByArtistOfTheDayDate(any()))
            .thenReturn(null) // first not found
            .thenReturn(null) // try again
        whenever(repository.findAllByArtistOfTheDayDateIsNull(any()))
            .thenReturn(PageImpl(listOf(artistAlias)))
        whenever(repository.save(any()))
            .thenThrow(DataIntegrityViolationException("Duplicate key"))

        val service = ArtistOfTheDayServiceGlobalRotation(repository)

        assertThrows<NoArtistOfTheDayException> {
            service.findArtistOfTheDay(today)
        }
    }
}