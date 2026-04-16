package com.ice.metadata.artist

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Version
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Entity
data class ArtistAlias(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String?,
    val name: String,
    val description: String,
    val userId: String,
    @Version
    val version: Long,
    val artistOfTheDayDate: LocalDate? = null,
)

@Repository
interface ArtistAliasRepository : JpaRepository<ArtistAlias, String> {
    fun findAllByUserId(userId: String): List<ArtistAlias>
    fun findByArtistOfTheDayDate(date: LocalDate): ArtistAlias?
    fun findAllByArtistOfTheDayDateIsNull(pageable: Pageable): Page<ArtistAlias>
}