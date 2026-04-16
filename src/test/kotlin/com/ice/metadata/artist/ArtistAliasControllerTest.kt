package com.ice.metadata.artist

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
class ArtistAliasControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var artistAliasRepository: ArtistAliasRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        artistAliasRepository.deleteAll()
    }

    @Test
    fun `List artist aliases for artist who hasn't registered one yet`() {
        mockMvc
            .perform(
                get("/artists")
                    .with(httpBasic("artist1", "password"))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `List artist aliases for artist who has registered 1 already`() {
        val artistAlias = buildArtistAlias(name="artist alias 1", description = "description", userId = "artist2")
        artistAliasRepository.save(artistAlias)

        mockMvc
            .perform(
                get("/artists")
                    .with(httpBasic("artist2", "password"))
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content").isNotEmpty)
            .andExpect(jsonPath("$.content[0].name").value(artistAlias.name))
            .andExpect(jsonPath("$.content[0].description").value(artistAlias.description))
            .andExpect(jsonPath("$.content[0].userId").value(artistAlias.userId))
    }


    @Test
    fun `Create new alias for the user`() {
        val description = "description"
        val name = "Artist alias"
        val createArtistAliasRequest = buildCreateArtistAliasRequest(name, description)

        val userId = "artist1"
        mockMvc
            .perform(
                post("/artists")
                    .with(httpBasic(userId, "password"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createArtistAliasRequest))
            )
            .andExpect(status().isOk)

        val artistAliases = artistAliasRepository.findAllByUserId(userId)
        assertThat(artistAliases).hasSize(1)
        assertThat(artistAliases[0].name).isEqualTo("Artist alias")
        assertThat(artistAliases[0].description).isEqualTo("description")
        assertThat(artistAliases[0].userId).isEqualTo(userId)
    }

    @Test
    fun `Try to create new alias that is already registered`() {
        val name = "artist alias 1"
        val artistAlias = buildArtistAlias(name= name, userId = "artist1")
        artistAliasRepository.save(artistAlias)

        val createArtistAliasRequest = buildCreateArtistAliasRequest(name=name)

        val userId = "artist1"
        mockMvc
            .perform(
                post("/artists")
                    .with(httpBasic(userId, "password"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createArtistAliasRequest))
            )
            .andExpect(status().is4xxClientError)

        val artistAliases = artistAliasRepository.findAllByUserId(userId)
        assertThat(artistAliases).hasSize(1)

    }

}

