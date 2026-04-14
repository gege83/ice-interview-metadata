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
    @Version
    val version: Long = 0
)

interface TrackMetadataService {
    fun findTracksByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
    fun create(track: TrackMetadata): TrackMetadata
    fun update(updateTrack: TrackMetadata): TrackMetadata
}

@Component
class TrackMetadataServiceImpl(val trackMetadataRepository: TrackMetadataRepository) : TrackMetadataService {
    override fun findTracksByArtistId(
        artistId: String,
        pageable: Pageable
    ): Page<TrackMetadata> {
        return trackMetadataRepository.findByArtistId(artistId, pageable)
    }

    override fun create(track: TrackMetadata): TrackMetadata {
        if(track.id != null) {
            throw TrackIdAlreadyExistsException("Track id must be null when creating a new track")
        }
        return trackMetadataRepository.save(track)
    }

    override fun update(updateTrack: TrackMetadata): TrackMetadata {
        val existingTrack = trackMetadataRepository
            .findById(updateTrack.id!!)
            .orElseThrow { TrackDoesNotExistException("Track with id ${updateTrack.id} not found") }
        val trackToSave = existingTrack.copy(name = updateTrack.name, artistId = updateTrack.artistId, version = updateTrack.version)
        try {
            return trackMetadataRepository.save(trackToSave)
        } catch (_: ObjectOptimisticLockingFailureException) {
            throw TrackHasBeenModifiedException("The record you attempted to edit was modified by another user. Please reload the data and try again.")
        }
    }
}

class TrackIdAlreadyExistsException(message: String) : RuntimeException(message) {
}

class TrackDoesNotExistException(message: String) : RuntimeException(message) {
}

class TrackHasBeenModifiedException(message: String) : RuntimeException(message) {
}

@Repository
interface TrackMetadataRepository : JpaRepository<TrackMetadata, String> {
    fun findByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
}