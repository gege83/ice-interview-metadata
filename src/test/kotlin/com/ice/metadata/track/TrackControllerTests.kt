package com.ice.metadata.track

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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

    @Test
    fun `Get tracks for the artist when no tracks are found`() {
        trackMetadataRepository.deleteAll()

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
    fun `Get tracks for the artist when there are less than a page`() {
        val trackName = "Test track"
        val artistId = "123"
        val track = buildTrack(name = trackName, artistId = artistId)
        trackMetadataRepository.deleteAll()
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
        trackMetadataRepository.deleteAll()
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
        trackMetadataRepository.deleteAll()
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
        trackMetadataRepository.deleteAll()
        val createTrackRequest = buildCreateTrackRequest(artistId = "1233")

        val responseString = mockMvc
            .perform(
                post("/tracks")
                    .with(httpBasic("user", "password"))
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
}