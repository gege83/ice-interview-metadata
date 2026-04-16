package com.ice.metadata.artist

import com.ice.metadata.utils.ConflictExceptions
import com.ice.metadata.utils.DoesNotExistsExceptions
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Component

data class CreateArtistAliasRequest(val name: String, val description: String)
data class UpdateArtistAliasRequest(val name: String?, val description: String?, val version: Long)

interface ArtistAliasService {
    // For simplicity, we will return ArtistAlias.
    fun findAllArtistAliasesFor(userId: String): List<ArtistAlias>
    fun createArtistAlias(userId: String, artistAliasDetails: CreateArtistAliasRequest): ArtistAlias
    fun updateArtistAlias(
        artistAliasId: String,
        userId: String,
        updateAliasRequest: UpdateArtistAliasRequest
    ): ArtistAlias
}

@Component
class ArtistAliasServiceImpl(val artistAliasRepository: ArtistAliasRepository) : ArtistAliasService {
    override fun findAllArtistAliasesFor(userId: String): List<ArtistAlias> {
        return artistAliasRepository.findAllByUserId(userId)
    }

    override fun createArtistAlias(
        userId: String,
        artistAliasDetails: CreateArtistAliasRequest
    ): ArtistAlias {
        val artistAliasToSave = ArtistAlias(
            id = null,
            name = artistAliasDetails.name,
            description = artistAliasDetails.description,
            userId = userId,
            version = 0
        )
        try {
            return artistAliasRepository.save(artistAliasToSave)
        } catch (_: DataIntegrityViolationException) {
            throw AliasAlreadyExistsException("Alias with name ${artistAliasDetails.name} already exists!")
        }
    }

    override fun updateArtistAlias(
        artistAliasId: String,
        userId: String,
        updateAliasRequest: UpdateArtistAliasRequest
    ): ArtistAlias {
        val existingAlias = artistAliasRepository.findById(artistAliasId)
            .orElseThrow { ArtistAliasDoesNotExistException(artistAliasId) }
        if (existingAlias.userId != userId) {
            throw ArtistAliasDoesNotExistException(artistAliasId)
        }
        val aliasToSave = existingAlias.copy(
            name = updateAliasRequest.name ?: existingAlias.name,
            description = updateAliasRequest.description ?: existingAlias.description,
            version = updateAliasRequest.version
        )
        try {
            return artistAliasRepository.save(aliasToSave)
        } catch (_: OptimisticLockingFailureException) {
            throw ArtistAliasHasBeenModifiedException(artistAliasId)
        } catch (_: DataIntegrityViolationException) {
            throw AliasAlreadyExistsException("Alias with name ${updateAliasRequest.name} already exists!")
        }
    }
}

class AliasAlreadyExistsException(message: String) : RuntimeException(message)
class ArtistAliasDoesNotExistException(id: String) : DoesNotExistsExceptions(id, entityName = "ArtistAlias")
class ArtistAliasHasBeenModifiedException(id: String) : ConflictExceptions(id, entityName = "ArtistAlias")