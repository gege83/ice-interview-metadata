package com.ice.metadata.artist

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

const val ARTISTS_OF_THE_DAY_URL = "/public/artist-of-the-day"

@SpringBootTest
@AutoConfigureMockMvc
class ArtistOfTheDayControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var artistAliasRepository: ArtistAliasRepository

    @BeforeEach
    fun setup() {
        artistAliasRepository.deleteAll()
    }

    @Test
    fun `it should return an error if there is no artist alias found`() {
        mockMvc.perform(
            get(ARTISTS_OF_THE_DAY_URL)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `it should return an the only artist alias from the db`() {
        val artistAlias = artistAliasRepository.save(buildArtistAlias())
        mockMvc.perform(
            get(ARTISTS_OF_THE_DAY_URL)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(artistAlias.id))
            .andExpect(jsonPath("$.name").value(artistAlias.name))
    }

    @Test
    fun `it should return with the same artist for the same day`() {
        artistAliasRepository.save(buildArtistAlias(name = "1",))
        artistAliasRepository.save(buildArtistAlias(name = "2",))

        val firstResponse = mockMvc.perform(
            get(ARTISTS_OF_THE_DAY_URL)
        )
            .andExpect(status().isOk).andReturn().response.contentAsString

        val secondResponse = mockMvc.perform(
            get(ARTISTS_OF_THE_DAY_URL)
        )
            .andExpect(status().isOk).andReturn().response.contentAsString

        assertEquals(firstResponse, secondResponse)
    }


    @Test
    fun `it should return with the a new artist for different days`() {
        val zone = java.time.Clock.systemUTC().zone
        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        artistAliasRepository.save(buildArtistAlias(name = "1", artistOfTheDayDate= yesterday))
        artistAliasRepository.save(buildArtistAlias(name = "2"))

        mockMvc.perform(
            get(ARTISTS_OF_THE_DAY_URL)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("2"))
            .andExpect(jsonPath("$.artistOfTheDayDate").value(today.toString()))
    }

    @Test
    fun `it should return with the a oldest artist of the day`() {
        val zone = java.time.Clock.systemUTC().zone
        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        val theDayBeforeYesterday = today.minusDays(2)
        artistAliasRepository.save(buildArtistAlias(name = "1", artistOfTheDayDate = yesterday))
        artistAliasRepository.save(buildArtistAlias(name = "2", artistOfTheDayDate = theDayBeforeYesterday))

        mockMvc.perform(
            get(ARTISTS_OF_THE_DAY_URL)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("2"))
            .andExpect(jsonPath("$.artistOfTheDayDate").value(today.toString()))
    }
}