package com.ice.metadata.track

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Version
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
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

@Repository
interface TrackMetadataRepository : JpaRepository<TrackMetadata, String> {
    fun findByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
}