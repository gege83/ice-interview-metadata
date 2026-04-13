package com.ice.metadata.track

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class TrackControllerTests {
    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var trackMetadataService: TrackMetadataService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun `Get tracks for the artist when no tracks are found`() {
        whenever(trackMetadataService.findTracksByArtistId(any(), any()))
            .thenReturn(PageImpl(emptyList()))

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/tracks?artistId=123")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isEmpty)
    }

    @Test
    fun `Get tracks for the artist when there are less than a page`() {
        val trackId = "321"
        val trackName = "Test track"
        val track = buildTrack(id = trackId, name = trackName)
        whenever(trackMetadataService.findTracksByArtistId(any(), any()))
            .thenReturn(PageImpl(listOf(track)))

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/tracks?artistId=123")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(trackId))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(trackName))
    }

    @Test
    fun `Get tracks for the artist when there are more than a page and return page size only`() {
        val track1 = buildTrack(id = "1")
        val track2 = buildTrack(id = "2")
        whenever(trackMetadataService.findTracksByArtistId(any(), any()))
            .thenReturn(PageImpl(listOf(track1, track2), Pageable.ofSize(2), 3))

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/tracks?artistId=123&size=2")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value("1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").value("2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content[2]").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.size").value("2"))

    }

    private fun buildTrack(id: String = "123", name: String = "Waiting all night"): TrackMetadata {
        return TrackMetadata(id = id, name = name)
    }

}