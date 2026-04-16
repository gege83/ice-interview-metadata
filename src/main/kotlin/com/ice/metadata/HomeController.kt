package com.ice.metadata

import com.ice.metadata.artist.ArtistAliasService
import com.ice.metadata.artist.ArtistOfTheDayService
import com.ice.metadata.artist.CreateArtistAliasRequest
import com.ice.metadata.track.CreateTrackRequest
import com.ice.metadata.track.TrackMetadataService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import java.time.LocalDate

@Controller
class HomeController(
    val artistAliasService: ArtistAliasService,
    val trackMetadataService: TrackMetadataService,
    val artistOfTheDayService: ArtistOfTheDayService
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/")
    fun home(): String {

        return "home"
    }

    @PostMapping("/")
    fun generateBaseData(): String {
        // Generate some base data for artist1
        try {
            val alias1 = artistAliasService.createArtistAlias(
                "artist1",
                CreateArtistAliasRequest("Rock Star", "A legendary rock musician")
            )
            artistAliasService.createArtistAlias(
                "artist1",
                CreateArtistAliasRequest("Jazz Maestro", "An expert in jazz compositions")
            )

            trackMetadataService.create(
                CreateTrackRequest(
                    "I'm a Rock Star",
                    artistId = alias1.id!!,
                    123,
                    genre = "Rock"
                ),
                userId = "artist1"
            )
        } catch (e: Exception) {
            logger.warn("Failed to create alias for artist1: ${e.message}")
        }

        // Generate some base data for artist2
        try {
            artistAliasService.createArtistAlias("artist2", CreateArtistAliasRequest("Pop Icon", "A famous pop singer"))
            artistAliasService.createArtistAlias(
                "artist2",
                CreateArtistAliasRequest("Soul Singer", "Known for soulful melodies")
            )
        } catch (e: Exception) {
            logger.warn("Failed to create alias for artist2: ${e.message}")
        }

        artistOfTheDayService.findArtistOfTheDay(LocalDate.now().minusDays(1))

        return "redirect:/"
    }
}
