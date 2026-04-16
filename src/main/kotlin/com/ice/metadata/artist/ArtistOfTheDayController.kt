package com.ice.metadata.artist

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId

@RestController
@RequestMapping("/public")
class ArtistOfTheDayController(val artistOfTheDayService: ArtistOfTheDayService) {

    @GetMapping("/artist-of-the-day")
    fun artistOfTheDay(): ArtistAlias {
        val date = java.time.LocalDate.now(ZoneId.of("UTC"))
        return artistOfTheDayService.findArtistOfTheDay(date)
    }

}

class NoArtistOfTheDayException : RuntimeException("No artist alias found in the db")