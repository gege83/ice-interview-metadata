package com.ice.metadata.artist

import com.ice.metadata.utils.ConflictExceptions
import com.ice.metadata.utils.DoesNotExistsExceptions
import jakarta.persistence.Id
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Version
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ArtistAliasController(val artistAliasService: ArtistAliasService) {
    @GetMapping("/artists")
    fun getArtistsForCurrentUser(@AuthenticationPrincipal userDetails: UserDetails): ArtistAliasResponse {
        return ArtistAliasResponse(content = artistAliasService.findAllArtistAliasesFor(userDetails.username))
    }

    @PostMapping("/artists")
    fun createArtistAliasForUser(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody artistAliasDetails: CreateArtistAliasRequest
    ): ArtistAlias {
        return artistAliasService.createArtistAlias(userDetails.username, artistAliasDetails)
    }

    @PutMapping("/artists/{artistId}")
    fun createArtistAliasForUser(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable artistId: String,
        @RequestBody artistAliasDetails: UpdateArtistAliasRequest
    ): ArtistAlias {
        return artistAliasService.updateArtistAlias(artistId, userDetails.username, artistAliasDetails)
    }
}

data class ArtistAliasResponse(val content: List<ArtistAlias> = listOf())
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

@Entity
data class ArtistAlias(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String?,
    val name: String,
    val description: String,
    val userId: String,
    @Version
    val version: Long
)

@Repository
interface ArtistAliasRepository : JpaRepository<ArtistAlias, String> {
    fun findAllByUserId(userId: String): List<ArtistAlias>
}
