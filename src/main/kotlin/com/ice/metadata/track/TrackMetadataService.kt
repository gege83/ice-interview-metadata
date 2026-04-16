package com.ice.metadata.track

import com.ice.metadata.artist.ArtistAliasDoesNotExistException
import com.ice.metadata.artist.ArtistAliasService
import com.ice.metadata.utils.ConflictExceptions
import com.ice.metadata.utils.DoesNotExistsExceptions
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component

interface TrackMetadataService {
    // For simplicity, we will return track metadata.
    fun findTracksByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
    fun create(newTrack: CreateTrackRequest, userId: String): TrackMetadata
    fun update(id: String, updateTrack: UpdateTrackRequest, userId: String): TrackMetadata
}

data class UpdateTrackRequest(
    val version: Long,
    val artistId: String? = null,
    val name: String? = null,
    val length: Long? = null,
    val genre: String? = null,
)


data class CreateTrackRequest(
    val name: String,
    val artistId: String,
    val length: Long,
    val genre: String? = null
)

@Component
class TrackMetadataServiceImpl(
    val trackMetadataRepository: TrackMetadataRepository,
    val artistAliasService: ArtistAliasService
) : TrackMetadataService {

    private fun validateArtistIdBelongsToUser(artistId: String, userId: String) {
        try {
            artistAliasService.validateArtistBelongsToUser(artistId, userId)
        } catch (_: ArtistAliasDoesNotExistException) {
            throw InvalidArtistIdException(artistId, "Artist does not exist or does not belong to this user")
        }
    }

    override fun findTracksByArtistId(
        artistId: String,
        pageable: Pageable
    ): Page<TrackMetadata> {
        return trackMetadataRepository.findByArtistId(artistId, pageable)
    }

    override fun create(newTrack: CreateTrackRequest, userId: String): TrackMetadata {
        validateArtistIdBelongsToUser(newTrack.artistId, userId)

        val track = TrackMetadata(
            id = null,
            name = newTrack.name,
            artistId = newTrack.artistId,
            length = newTrack.length,
            genre = newTrack.genre
        )
        //TODO send notification about the new track to save history
        return trackMetadataRepository.save(track)
    }

    override fun update(id: String, updateTrack: UpdateTrackRequest, userId: String): TrackMetadata {
        val existingTrack = trackMetadataRepository
            .findById(id)
            .orElseThrow { TrackDoesNotExistException(id) }

        // If artistId is being updated, validate it belongs to the user
        val newArtistId = updateTrack.artistId ?: existingTrack.artistId
        validateArtistIdBelongsToUser(newArtistId, userId)

        val trackToSave = existingTrack.copy(
            name = updateTrack.name ?: existingTrack.name,
            artistId = newArtistId,
            length = updateTrack.length?: existingTrack.length,
            genre = updateTrack.genre?: existingTrack.genre,
            version = updateTrack.version
        )
        try {
            //TODO send notification about the track change to save history
            return trackMetadataRepository.save(trackToSave)
        } catch (_: ObjectOptimisticLockingFailureException) {
            throw TrackHasBeenModifiedException(id)
        }
    }
}

class TrackDoesNotExistException(id: String) : DoesNotExistsExceptions(id, entityName = "Track")
class TrackHasBeenModifiedException(id: String) : ConflictExceptions(id, entityName = "Track")
class InvalidArtistIdException(id: String, message: String) : RuntimeException("Artist ID '$id': $message")
