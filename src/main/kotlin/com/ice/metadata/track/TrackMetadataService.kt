package com.ice.metadata.track

import com.ice.metadata.utils.ConflictExceptions
import com.ice.metadata.utils.DoesNotExistsExceptions
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component

interface TrackMetadataService {
    // For simplicity, we will return track metadata.
    fun findTracksByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
    fun create(newTrack: CreateTrackRequest): TrackMetadata
    fun update(id: String, updateTrack: UpdateTrackRequest): TrackMetadata
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
class TrackMetadataServiceImpl(val trackMetadataRepository: TrackMetadataRepository) : TrackMetadataService {
    override fun findTracksByArtistId(
        artistId: String,
        pageable: Pageable
    ): Page<TrackMetadata> {
        return trackMetadataRepository.findByArtistId(artistId, pageable)
    }

    override fun create(newTrack: CreateTrackRequest): TrackMetadata {
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

    override fun update(id: String, updateTrack: UpdateTrackRequest): TrackMetadata {
        val existingTrack = trackMetadataRepository
            .findById(id)
            .orElseThrow { TrackDoesNotExistException(id) }
        val trackToSave = existingTrack.copy(
            name = updateTrack.name ?: existingTrack.name,
            artistId = updateTrack.artistId ?: existingTrack.artistId,
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

