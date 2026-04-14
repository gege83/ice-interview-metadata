package com.ice.metadata.track

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc

@SpringBootTest
@AutoConfigureMockMvc
class TrackControllerTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var trackMetadataRepository: TrackMetadataRepository

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
        val trackId = "321"
        val trackName = "Test track"
        val track = buildTrack(id = trackId, name = trackName)
        trackMetadataRepository.deleteAll()
        trackMetadataRepository.save(track)

        mockMvc
            .perform(
                get("/tracks?artistId=123")
                    .with(httpBasic("user", "password"))
            )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isNotEmpty)
            .andExpect(jsonPath("$.content[0].id").value(trackId))
            .andExpect(jsonPath("$.content[0].name").value(trackName))
    }

    @Test
    fun `Get tracks for the artist when there are more than a page and return page size only`() {
        val track1 = buildTrack(id = "1")
        val track2 = buildTrack(id = "2")
        val track3 = buildTrack(id = "3")
        trackMetadataRepository.deleteAll()
        trackMetadataRepository.saveAll(listOf(track1, track2, track3))

        mockMvc
            .perform(
                get("/tracks?artistId=123&size=2")
                    .with(httpBasic("user", "password"))
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value("1"))
            .andExpect(jsonPath("$.content[1].id").value("2"))
            .andExpect(jsonPath("$.content[2]").doesNotExist())
            .andExpect(jsonPath("$.page.size").value("2"))
    }

    @Test
    fun `Get tracks for the artist when there are more than one artist in the db`() {
        val artistId = "123"
        val track1 = buildTrack(id = "1", artistId = artistId)
        val track2 = buildTrack(id = "2", artistId = "13")
        val track3 = buildTrack(id = "3", artistId = "456")
        trackMetadataRepository.deleteAll()
        trackMetadataRepository.saveAll(listOf(track1, track2, track3))

        mockMvc
            .perform(
                get("/tracks?artistId=${artistId}")
                    .with(httpBasic("user", "password"))
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value("1"))
            .andExpect(jsonPath("$.content[0].artistId").value(artistId))
            .andExpect(jsonPath("$.content[1]").doesNotExist())
    }

}