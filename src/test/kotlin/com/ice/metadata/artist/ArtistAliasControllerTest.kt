package com.ice.metadata.artist

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
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

    @Nested
    inner class ListTest {
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
            val artistAlias = buildArtistAlias(name = "artist alias 1", description = "description", userId = "artist2")
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
    }

    @Nested
    inner class CreateTest {
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
            val artistAlias = buildArtistAlias(name = name, userId = "artist1")
            artistAliasRepository.save(artistAlias)

            val createArtistAliasRequest = buildCreateArtistAliasRequest(name = name)

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

        @Test
        fun `Create artist alias should be restricted to artists`() {
            val userId = "user"
            val createArtistAliasRequest = buildCreateArtistAliasRequest(name = "My stale update")
            mockMvc
                .perform(
                    post("/artists")
                        .with(httpBasic(userId, "password"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createArtistAliasRequest))
                )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    inner class UpdateTest {
        @Test
        fun `Update fails when alias id not found`() {
            val userId = "artist1"
            val artistAlias = artistAliasRepository.save(buildArtistAlias(userId = "userId"))

            val updateTrackRequest = buildUpdateArtistAliasRequest()

            mockMvc
                .perform(
                    put("/artists/${artistAlias.id}")
                        .with(httpBasic(userId, "password"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTrackRequest))
                )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `Update artist alias name fails when name already exists`() {
            val userId1 = "artist1"
            val userId2 = "artist2"
            val name = "my artist alias"
            artistAliasRepository.save(buildArtistAlias(userId = userId1, name = name))
            val aliasToUpdate = artistAliasRepository.save(buildArtistAlias(userId = userId2, name = "some name"))

            //update the value to an existing name
            val updateTrackRequest = buildUpdateArtistAliasRequest(name = name, version = aliasToUpdate.version)

            mockMvc
                .perform(
                    put("/artists/${aliasToUpdate.id}")
                        .with(httpBasic(userId2, "password"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTrackRequest))
                )
                .andExpect(status().isConflict) // Handled by GlobalExceptionHandler
        }

        @Test
        fun `Update artist alias for the user`() {
            val userId = "artist1"
            val artistAlias = artistAliasRepository.save(buildArtistAlias(userId = userId))

            // Now try to update with the original version
            val updateTrackRequest = buildUpdateArtistAliasRequest(
                name = "My update",
                description = "my updated description",
                version = artistAlias.version
            )

            mockMvc
                .perform(
                    put("/artists/${artistAlias.id}")
                        .with(httpBasic(userId, "password"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTrackRequest))
                )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value(updateTrackRequest.name))
                .andExpect(jsonPath("$.description").value(updateTrackRequest.description))
                .andExpect(jsonPath("$.version").value(artistAlias.version + 1))
        }

        @Test
        fun `Update artist alias with old version should fail with optimistic locking error`() {
            val userId = "artist1"
            val artistAlias = artistAliasRepository.save(buildArtistAlias(userId = userId))

            artistAliasRepository.save(artistAlias.copy(name = "some other name"))

            val updateTrackRequest =
                buildUpdateArtistAliasRequest(name = "My stale update", version = artistAlias.version)

            mockMvc
                .perform(
                    put("/artists/${artistAlias.id}")
                        .with(httpBasic(userId, "password"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTrackRequest))
                )
                .andExpect(status().isConflict) // Handled by GlobalExceptionHandler
        }


        @Test
        fun `Update artist alias should be restricted to artists`() {
            val userId = "user"
            val updateTrackRequest = buildUpdateArtistAliasRequest(name = "My stale update", version = 0)
            mockMvc
                .perform(
                    put("/artists/some-id")
                        .with(httpBasic(userId, "password"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTrackRequest))
                )
                .andExpect(status().isForbidden)
        }
    }
}

