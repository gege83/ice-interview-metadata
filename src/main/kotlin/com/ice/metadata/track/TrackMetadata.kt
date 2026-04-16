package com.ice.metadata.track

import jakarta.persistence.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Entity
data class TrackMetadata(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String?,
    val name: String,
    val artistId: String,
    val length: Long,
    val genre: String? = null,
    @Version
    val version: Long = 0
)

interface TrackMetadataService {
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
            .orElseThrow { TrackDoesNotExistException("Track with id $id not found") }
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
            throw TrackHasBeenModifiedException("The record you attempted to edit was modified by another user. Please reload the data and try again.")
        }
    }
}

class TrackIdAlreadyExistsException(message: String) : RuntimeException(message)

class TrackDoesNotExistException(message: String) : RuntimeException(message)

class TrackHasBeenModifiedException(message: String) : RuntimeException(message)

@Repository
interface TrackMetadataRepository : JpaRepository<TrackMetadata, String> {
    fun findByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
}