package com.ice.metadata.track

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Table
data class TrackMetadata(
    @Id val id: String,
    val name: String,
    val artistId: String) {
}

interface TrackMetadataService {
    fun findTracksByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
}

@Component
class TrackMetadataServiceImpl(val trackMetadataRepository: TrackMetadataRepository) : TrackMetadataService {
    override fun findTracksByArtistId(
        artistId: String,
        pageable: Pageable
    ): Page<TrackMetadata> {
        return trackMetadataRepository.findTracksByArtistId(artistId, pageable)
    }
}

@Repository
interface TrackMetadataRepository: CrudRepository<TrackMetadata, String> {
    fun findTracksByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
}


