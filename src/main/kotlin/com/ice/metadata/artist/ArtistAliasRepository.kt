package com.ice.metadata.artist

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Version
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

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