package com.ice.metadata.track

import jakarta.persistence.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Entity
data class TrackMetadata(
    @Id
    val id: String,
    val name: String,
    val artistId: String
)

interface TrackMetadataService {
    fun findTracksByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
}

@Component
class TrackMetadataServiceImpl(val trackMetadataRepository: TrackMetadataRepository) : TrackMetadataService {
    override fun findTracksByArtistId(
        artistId: String,
        pageable: Pageable
    ): Page<TrackMetadata> {
        return trackMetadataRepository.findByArtistId(artistId, pageable)
    }
}

@Repository
interface TrackMetadataRepository : JpaRepository<TrackMetadata, String> {
    fun findByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
}