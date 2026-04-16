package com.ice.metadata.track

import com.ice.metadata.artist.ArtistAlias
import com.ice.metadata.artist.ArtistAliasRepository
import com.ice.metadata.artist.buildArtistAlias
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import tools.jackson.databind.ObjectMapper
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
class TrackControllerTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var trackMetadataRepository: TrackMetadataRepository

    @Autowired
    private lateinit var artistAliasRepository: ArtistAliasRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        trackMetadataRepository.deleteAll()
        artistAliasRepository.deleteAll()
    }

    private fun createArtistAlias(name: String, userId: String): ArtistAlias {
        return artistAliasRepository.save(buildArtistAlias(
            id = null,
            name = name,
            description = "Test Description",
            userId = userId
        ))
    }

    @Test
    fun `Get tracks for the artist when no tracks are found`() {
        val artist = createArtistAlias("Default Artist", "artist1")

        mockMvc
            .perform(
                get("/tracks?artistId=${artist.id}")
                    .with(httpBasic("user", "password"))
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isEmpty)
    }

    @Test
    fun `Get tracks for the artist when no tracks are found with artist role`() {
        mockMvc
            .perform(
                get("/tracks?artistId=123")
                    .with(httpBasic("artist2", "password"))
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isEmpty)
    }

    @Test
    fun `Get tracks for the artist when there are less than a page`() {
        val artist = createArtistAlias("Default Artist", "artist1")

        val trackName = "Test track"
        val track = buildTrack(name = trackName, artistId = artist.id!!)
        val savedTrack = trackMetadataRepository.save(track)

        mockMvc
            .perform(
                get("/tracks?artistId=${artist.id}")
                    .with(httpBasic("user", "password"))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isNotEmpty)
            .andExpect(jsonPath("$.content[0].id").value(savedTrack.id))
            .andExpect(jsonPath("$.content[0].name").value(trackName))
    }

    @Test
    fun `Get tracks for the artist when there are more than a page and return page size only`() {
        val artistId = 123
        trackMetadataRepository.saveAll(
            listOf(
                buildTrack(artistId = "$artistId"),
                buildTrack(artistId = "$artistId"),
                buildTrack(artistId = "$artistId")
            )
        )

        mockMvc
            .perform(
                get("/tracks?artistId=$artistId&size=2")
                    .with(httpBasic("user", "password"))
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.page.size").value("2"))
            .andExpect(jsonPath("$.content[0]").exists())
            .andExpect(jsonPath("$.content[1]").exists())
            .andExpect(jsonPath("$.content[2]").doesNotExist())
    }

    @Test
    fun `Get tracks for the artist when there are more than one artist in the db`() {
        val artistId = "123"
        val track1 = buildTrack(artistId = artistId)
        val track2 = buildTrack(artistId = "13")
        val track3 = buildTrack(artistId = "456")
        trackMetadataRepository.saveAll(listOf(track1, track2, track3))

        mockMvc
            .perform(
                get("/tracks?artistId=${artistId}")
                    .with(httpBasic("user", "password"))
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].artistId").value(artistId))
            .andExpect(jsonPath("$.content[1]").doesNotExist())
    }

    @Test
    fun `Create a new track for an artist`() {
        val artist = createArtistAlias("Test Artist", "artist1")

        val createTrackRequest = buildCreateTrackRequest(artistId = artist.id!!)

        val responseString = mockMvc
            .perform(
                post("/tracks")
                    .with(httpBasic("artist1", "password"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createTrackRequest))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andReturn().response.contentAsString

        val trackMetadata = objectMapper.readValue(responseString, TrackMetadata::class.java)
        assertTrue(trackMetadataRepository.existsById(trackMetadata.id!!))
    }

    @Test
    fun `Create a new track for an artist with more details`() {
        val artist = createArtistAlias("Test Artist", "artist1")

        val createTrackRequest = buildCreateTrackRequest(
            artistId = artist.id!!,
            name = "Feeling good",
            length = 237,
            genre = "Jazz"
        )

        mockMvc
            .perform(
                post("/tracks")
                    .with(httpBasic("artist1", "password"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createTrackRequest))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.name").value("Feeling good"))
            .andExpect(jsonPath("$.length").value(237))
            .andExpect(jsonPath("$.genre").value("Jazz"))
    }

    @Test
    fun `Update track metadata should increment version`() {
        val artist = createArtistAlias("Test Artist", "artist1")

        val track = trackMetadataRepository.save(buildTrack(artistId = artist.id!!))
        val initialVersion = track.version
        val updateTrackRequest = buildUpdateTrackRequest(artistId = track.artistId, name = "Updated Name", version = track.version)

        mockMvc
            .perform(
                put("/tracks/${track.id}")
                    .with(httpBasic("artist1", "password"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateTrackRequest))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated Name"))
            .andExpect(jsonPath("$.version").value(initialVersion + 1))
    }

    @Test
    fun `Update track metadata with old version should fail with optimistic locking error`() {
        val artist = createArtistAlias("Test Artist", "artist1")

        val track = trackMetadataRepository.save(buildTrack(artistId = artist.id!!))

        // Simulating the track being updated by someone else
        trackMetadataRepository.save(track.copy(name = "Intervening update"))

        // Now try to update with the original version
        val updateTrackRequest = buildUpdateTrackRequest(artistId = track.artistId, name = "My stale update", version = track.version)

        mockMvc
            .perform(
                put("/tracks/${track.id}")
                    .with(httpBasic("artist1", "password"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateTrackRequest))
            )
            .andExpect(status().isConflict) // Handled by GlobalExceptionHandler
    }

    @Test
    fun `Update fails if id not found`() {
        val artist = createArtistAlias("Test Artist", "artist1")

        val trackId = "non-existent-id"
        val updateTrackRequest = buildUpdateTrackRequest(artistId = artist.id!!)

        mockMvc
            .perform(
                put("/tracks/${trackId}")
                    .with(httpBasic("artist1", "password"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateTrackRequest))
            )
            .andExpect(status().isNotFound) // Handled by GlobalExceptionHandler
    }

    @Test
    fun `Create track fails if artist does not exist`() {
        val createTrackRequest = buildCreateTrackRequest(artistId = "non-existent-artist")

        mockMvc
            .perform(
                post("/tracks")
                    .with(httpBasic("artist1", "password"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createTrackRequest))
            )
            .andExpect(status().isBadRequest) // Handled by GlobalExceptionHandler
    }

    @Test
    fun `Create track fails if artist belongs to different user`() {
        // Create an artist for artist2
        val artist2 = createArtistAlias("Artist2 Exclusive", "artist2")

        val createTrackRequest = buildCreateTrackRequest(artistId = artist2.id!!)

        mockMvc
            .perform(
                post("/tracks")
                    .with(httpBasic("artist1", "password"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createTrackRequest))
            )
            .andExpect(status().isBadRequest) // Handled by GlobalExceptionHandler
    }

    @Test
    fun `Update track fails if artist belongs to different user`() {
        val artist1 = createArtistAlias("Test Artist", "artist1")

        val track = trackMetadataRepository.save(buildTrack(artistId = artist1.id!!))

        // Create an artist for artist2
        val artist2 = createArtistAlias("Artist2 Exclusive", "artist2")

        val updateTrackRequest = buildUpdateTrackRequest(artistId = artist2.id!!, version = track.version)

        mockMvc
            .perform(
                put("/tracks/${track.id}")
                    .with(httpBasic("artist1", "password"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateTrackRequest))
            )
            .andExpect(status().isBadRequest) // Handled by GlobalExceptionHandler
    }
}