package com.ice.metadata.artist

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
val logger: Logger = LoggerFactory.getLogger(ArtistOfTheDayService::class.java)

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

    private fun findTodayArtistOfTheDay(date: LocalDate): ArtistAlias? {
        logger.debug("Fetching today's artist of the day for date: {}", date)
        val artistOfTheDay = artistRepository.findByArtistOfTheDayDate(date)
        logger.info("Fetched today's artist of the day for date: {}, name: {}", date, artistOfTheDay?.name)
        return artistOfTheDay
    }


    private fun updateArtistOfTheDayIfNeeded(
        artistOfTheDay: ArtistAlias,
        date: LocalDate
    ): ArtistAlias {
        return if (isUpdateNeeded(artistOfTheDay, date)) {
            val updatedArtist = artistOfTheDay.copy(artistOfTheDayDate = date)
            try {
                logger.debug("Updating artist of the day. Artist: {}, date: {}", updatedArtist.name, date)
                val updated = artistRepository.save(updatedArtist)
                logger.debug("Updated artist of the day. Artist: {}, date: {}", updated.name, date)
                updated
            } catch (_: DataIntegrityViolationException) {
                logger.info("Save failed, probably concurrent update. Retrying today's artist of the day")
                return findTodayArtistOfTheDay(date) ?: throw NoArtistOfTheDayException()
            }
        } else {
            logger.debug("no update needed")
            artistOfTheDay
        }
    }

    private fun isUpdateNeeded(artistOfTheDay: ArtistAlias, date: LocalDate): Boolean =
        artistOfTheDay.artistOfTheDayDate == null || artistOfTheDay.artistOfTheDayDate.isBefore(date)

    private fun findOldestArtistOfTheDay(): ArtistAlias? {
        logger.debug("Fetching oldest artist of the day")
        val artistOfTheDay = artistRepository
            .findAll(
                PageRequest.of(
                    0,
                    1,
                    Sort.by("artistOfTheDayDate").ascending()
                )
            )
            .content.firstOrNull()
        logger.info("Fetched oldest artist of the day: {}", artistOfTheDay?.name)
        return artistOfTheDay
    }

    private fun findArtistWhoHaveNotBeenBefore(): ArtistAlias? {
        logger.debug("Fetching artist who have not been artist of the day before")
        val artistAlias = artistRepository
            .findAllByArtistOfTheDayDateIsNull(
                PageRequest.of(0, 1)
            )
            .content.firstOrNull()
        logger.info("Fetched artist who have been artist of the day before. Artist of the day: {}", artistAlias?.name)
        return artistAlias
    }
}