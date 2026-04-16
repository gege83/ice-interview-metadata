package com.ice.metadata.artist

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.time.LocalDate

interface ArtistOfTheDayService {
    fun findArtistOfTheDay(date: LocalDate): ArtistAlias
}

@Component
class ArtistOfTheDayServiceGlobalRotation(val artistRepository: ArtistAliasRepository) : ArtistOfTheDayService {

    override fun findArtistOfTheDay(date: LocalDate): ArtistAlias {
        var artistOfTheDay = findTodayArtistOfTheDay(date)
        if (artistOfTheDay == null) {
            artistOfTheDay = findArtistWhoHaveNotBeenBefore()
        }
        if (artistOfTheDay == null) {
            artistOfTheDay = findOldestArtistOfTheDay()
        }
        // no artist found in the db
        if (artistOfTheDay == null) {
            throw NoArtistOfTheDayException()
        }

        return updateArtistOfTheDayIfNeeded(artistOfTheDay, date)
    }

    private fun findTodayArtistOfTheDay(date: LocalDate): ArtistAlias? =
        artistRepository.findByArtistOfTheDayDate(date)

    private fun updateArtistOfTheDayIfNeeded(
        artistOfTheDay: ArtistAlias,
        date: LocalDate
    ): ArtistAlias {
        return if (isUpdateNeeded(artistOfTheDay, date)) {
            val updatedArtist = artistOfTheDay.copy(artistOfTheDayDate = date)
            try {
                artistRepository.save(updatedArtist)
            } catch (_: DataIntegrityViolationException) {
                return findTodayArtistOfTheDay(date) ?: throw NoArtistOfTheDayException()
            }
        } else {
            artistOfTheDay
        }
    }

    private fun isUpdateNeeded(artistOfTheDay: ArtistAlias, date: LocalDate): Boolean =
        artistOfTheDay.artistOfTheDayDate == null || artistOfTheDay.artistOfTheDayDate.isBefore(date)

    private fun findOldestArtistOfTheDay(): ArtistAlias? =
        artistRepository
            .findAll(
                PageRequest.of(
                    0,
                    1,
                    Sort.by("artistOfTheDayDate").ascending()
                )
            )
            .content.firstOrNull()

    private fun findArtistWhoHaveNotBeenBefore(): ArtistAlias? =
        artistRepository
            .findAllByArtistOfTheDayDateIsNull(
                PageRequest.of(0, 1)
            )
            .content.firstOrNull()
}