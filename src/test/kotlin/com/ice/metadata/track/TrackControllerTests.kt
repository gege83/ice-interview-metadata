package com.ice.metadata.track

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
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        trackMetadataRepository.deleteAll()
    }

    @Test
    fun `Get tracks for the artist when no tracks are found`() {
        mockMvc
            .perform(
                get("/tracks?artistId=123")
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
        val trackName = "Test track"
        val artistId = "123"
        val track = buildTrack(name = trackName, artistId = artistId)
        val savedTrack = trackMetadataRepository.save(track)

        mockMvc
            .perform(
                get("/tracks?artistId=$artistId")
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
        val createTrackRequest = buildCreateTrackRequest(artistId = "1233")

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
        val createTrackRequest = buildCreateTrackRequest(
            artistId = "1233",
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
        val track = trackMetadataRepository.save(buildTrack())
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
        val track = trackMetadataRepository.save(buildTrack())

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
        val trackId = "non-existent-id"
        val updateTrackRequest = buildUpdateTrackRequest()

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
}