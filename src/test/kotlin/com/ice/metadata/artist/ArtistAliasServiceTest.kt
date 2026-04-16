package com.ice.metadata.artist

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ArtistAliasServiceTest {

    @Mock
    private lateinit var artistAliasRepository: ArtistAliasRepository

    private lateinit var service: ArtistAliasService

    @BeforeEach
    fun setup() {
        service = ArtistAliasServiceImpl(artistAliasRepository)
    }


    @Test
    fun `List artist aliases for user when none exists`() {
        val artistAliases = service.findAllArtistAliasesFor(userId = "artist1")
        assertThat(artistAliases).hasSize(0)
    }

    @Test
    fun `List artist aliases for user when one created`() {
        val artistAlias = buildArtistAlias(id = "some id")
        whenever(artistAliasRepository.findAllByUserId(any())).thenReturn(
            listOf(
                artistAlias
            )
        )
        val artistAliases = service.findAllArtistAliasesFor(userId = "artist1")
        assertThat(artistAliases).isEqualTo(listOf(artistAlias))
    }

    @Test
    fun `Create new alias for user`() {
        val createArtistAliasRequest = buildCreateArtistAliasRequest(name = "artist", description = "some description")
        val userId = "userId"
        val expectedArtistAliasToSave = buildArtistAlias(
            id = null,
            userId = userId,
            description = createArtistAliasRequest.description,
            name = createArtistAliasRequest.name,
        )
        whenever(artistAliasRepository.save(expectedArtistAliasToSave))
            .thenReturn(expectedArtistAliasToSave.copy(id = "generated id"))

        val savedArtistAlias =
            service.createArtistAlias(userId, createArtistAliasRequest)

        assertThat(savedArtistAlias).isEqualTo(expectedArtistAliasToSave.copy(id = "generated id"))
    }

    @Test
    fun `Create new alias for user but alias is already in use`() {
        val createArtistAliasRequest = buildCreateArtistAliasRequest(name = "artist")
        val userId = "userId"

        whenever(artistAliasRepository.save(any()))
            .thenThrow(DataIntegrityViolationException("Duplicate key"))

        assertThrows<AliasAlreadyExistsException> {
            service.createArtistAlias(userId, createArtistAliasRequest)
        }
    }

    @Test
    fun `Update alias for user when alias is that is not in use`() {
        val userId = "userId"
        val artistAliasId = "id1"
        val savedArtistAlias = buildArtistAlias(
            id = artistAliasId,
            userId = userId,
        )
        val updateAliasRequest =
            buildUpdateArtistAliasRequest(name = "artist", description = "some description")
        val expectedArtistAliasToSave = buildArtistAlias(
            id = artistAliasId,
            userId = userId,
            name = updateAliasRequest.name!!,
            description = updateAliasRequest.description!!,
        )
        whenever(artistAliasRepository.findById(artistAliasId))
            .thenReturn(Optional.of(savedArtistAlias))
        whenever(artistAliasRepository.save(any()))
            .thenReturn(expectedArtistAliasToSave)

        val updatedArtistAlias = service.updateArtistAlias(artistAliasId, userId, updateAliasRequest)
        assertThat(updatedArtistAlias).isEqualTo(expectedArtistAliasToSave)
        verify(artistAliasRepository).save(expectedArtistAliasToSave)
    }

    @Test
    fun `Update alias for user when alias is already in use`() {
        val userId = "userId"
        val artistAliasId = "id2"
        val savedArtistAlias = buildArtistAlias(
            id = artistAliasId,
            userId = userId,
        )
        val updateAliasRequest =
            buildUpdateArtistAliasRequest(
                name = "already in use",
            )
        whenever(artistAliasRepository.findById(artistAliasId))
            .thenReturn(Optional.of(savedArtistAlias))
        whenever(artistAliasRepository.save(any()))
            .thenThrow(DataIntegrityViolationException("Duplicate key"))

        assertThrows<AliasAlreadyExistsException> {
            service.updateArtistAlias(artistAliasId, userId, updateAliasRequest)
        }
    }

    @Test
    fun `Update alias for user record doesn't belong to the user`() {
        val userId1 = "userId1"
        val userId2 = "userId2"
        val artistAliasId = "id3"
        val savedArtistAlias = buildArtistAlias(
            id = artistAliasId,
            userId = userId1,
        )
        val updateAliasRequest =
            buildUpdateArtistAliasRequest(
                name = "already in use",
            )
        whenever(artistAliasRepository.findById(artistAliasId))
            .thenReturn(Optional.of(savedArtistAlias))

        assertThrows<ArtistAliasDoesNotExistException> {
            service.updateArtistAlias(artistAliasId, userId2, updateAliasRequest)
        }
    }

    @Test
    fun `Update alias id that doesn't exists`() {
        val artistAliasId = "id doesn't exists"

        val updateAliasRequest =
            buildUpdateArtistAliasRequest(
                name = "already in use",
            )
        whenever(artistAliasRepository.findById(artistAliasId))
            .thenReturn(Optional.empty())

        assertThrows<ArtistAliasDoesNotExistException> {
            service.updateArtistAlias(artistAliasId, "userId2", updateAliasRequest)
        }
    }

}
