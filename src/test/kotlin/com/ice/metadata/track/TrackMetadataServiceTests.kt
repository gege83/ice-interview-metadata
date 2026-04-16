package com.ice.metadata.track

import com.ice.metadata.artist.ArtistAliasDoesNotExistException
import com.ice.metadata.artist.ArtistAliasService
import com.ice.metadata.artist.buildArtistAlias
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class TrackMetadataServiceTests {
    @Mock
    lateinit var trackMetadataRepository: TrackMetadataRepository

    @Mock
    lateinit var artistAliasService: ArtistAliasService
    lateinit var service: TrackMetadataService

    @BeforeEach
    fun setup() {
        service = TrackMetadataServiceImpl(trackMetadataRepository, artistAliasService)
    }

    @Nested
    inner class FindTracksForArtistTests {


        @Test
        fun `Should return no tracks when artist has no tracks`() {
            val artistId = "123"
            whenever(trackMetadataRepository.findByArtistId(any(), any())).thenReturn(PageImpl(emptyList()))
            val tracks = service.findTracksByArtistId(artistId, Pageable.ofSize(10))
            assert(tracks.content.isEmpty())
        }

        @Test
        fun `Should return all tracks when artist has less than page tracks`() {
            val artistId = "123"
            whenever(trackMetadataRepository.findByArtistId(any(), any()))
                .thenReturn(PageImpl(listOf(buildTrack(), buildTrack())))
            val tracks = service.findTracksByArtistId(artistId, Pageable.ofSize(10))
            assertEquals(2, tracks.content.count())
        }

        @Test
        fun `Should return all tracks when artist has more than page tracks`() {
            val artistId = "1233"
            val content = (1..10).map { buildTrack() }
            whenever(trackMetadataRepository.findByArtistId(any(), any()))
                .thenReturn(PageImpl(content))
            val tracks = service.findTracksByArtistId(artistId, Pageable.ofSize(10))
            assertEquals(10, tracks.content.count())
            verify(trackMetadataRepository).findByArtistId(artistId, Pageable.ofSize(10))
        }
    }

    @Nested
    inner class CreateNewTracksForArtistTests {
        @Test
        fun `Should create new track when there is no track`() {
            val userId = "artist1"
            val artistId = "2345"
            val createTrackRequest = buildCreateTrackRequest(
                artistId = artistId,
                name = "Test track",
                length = 123L,
                genre = "Pop"
            )
            val generatedId = "generatedId"
            val expectedTrackToSave = buildTrack(
                id = null,
                artistId = createTrackRequest.artistId,
                name = createTrackRequest.name,
                length = createTrackRequest.length,
                genre = createTrackRequest.genre
            )

            // Mock the artist alias validation
            val artist = buildArtistAlias(id = artistId, userId = userId)
            whenever(artistAliasService.validateArtistBelongsToUser(artistId, userId)).thenReturn(artist)

            whenever(trackMetadataRepository.save(any()))
                .thenReturn(
                    expectedTrackToSave.copy(id = generatedId)
                )

            val savedTrack: TrackMetadata = service.create(createTrackRequest, userId)

            assertEquals(expectedTrackToSave.copy(id = generatedId), savedTrack)
            verify(trackMetadataRepository).save(expectedTrackToSave)
        }

        @Test
        fun `Should throw exception when artist does not exist`() {
            val userId = "artist1"
            val artistId = "non-existent"
            val createTrackRequest = buildCreateTrackRequest(
                artistId = artistId,
                name = "Test track",
                length = 123L,
                genre = "Pop"
            )

            // Mock the artist alias to not exist
            whenever(artistAliasService.validateArtistBelongsToUser(artistId, userId))
                .thenThrow(ArtistAliasDoesNotExistException(artistId))

            assertThrows<InvalidArtistIdException> {
                service.create(createTrackRequest, userId)
            }
        }

        @Test
        fun `Should throw exception when artist does not belong to user`() {
            val userId = "artist1"
            val artistId = "2345"
            val createTrackRequest = buildCreateTrackRequest(
                artistId = artistId,
                name = "Test track",
                length = 123L,
                genre = "Pop"
            )

            // Mock the artist alias to belong to different user
            whenever(artistAliasService.validateArtistBelongsToUser(artistId, userId))
                .thenThrow(ArtistAliasDoesNotExistException(artistId))

            assertThrows<InvalidArtistIdException> {
                service.create(createTrackRequest, userId)
            }
        }
    }

    @Nested
    inner class UpdateExistingTrackForArtistTests {
        @Test
        fun `Should update existing track with all fields`() {
            val userId = "artist1"
            val trackId = "track id"
            val artistId = "new artist id"
            val originalTrack = buildTrack(id = trackId)
            val updateTrack = buildUpdateTrackRequest(
                name = "new Name",
                genre = "Pop",
                length = 123L,
                artistId = artistId,
                version = 0
            )
            val expectedUpdateTrack = buildTrack(
                id = trackId,
                name = updateTrack.name!!,
                genre = updateTrack.genre!!,
                length = updateTrack.length!!,
                artistId = updateTrack.artistId!!,
                version = updateTrack.version
            )

            // Mock the artist alias validation
            val artist = buildArtistAlias(id = artistId, userId = userId)
            whenever(artistAliasService.validateArtistBelongsToUser(artistId, userId)).thenReturn(artist)

            whenever(trackMetadataRepository.findById(trackId)).thenReturn(Optional.of(originalTrack))
            whenever(trackMetadataRepository.save(any()))
                .thenReturn(expectedUpdateTrack)
            val savedTrack: TrackMetadata = service.update(trackId, updateTrack, userId)
            assertEquals(expectedUpdateTrack, savedTrack)
            verify(trackMetadataRepository).save(expectedUpdateTrack)
        }

        @Test
        fun `Should update existing track with non-null fields`() {
            val userId = "artist1"
            val trackId = "track id"
            val originalTrack = buildTrack(length = 123, id = trackId)
            val newLength = 1234L
            val updateTrack = buildUpdateTrackRequest(length = newLength, version = 0)
            val expectedUpdateTrack = buildTrack(id = trackId, length = newLength, version = 0)

            // Mock the artist alias validation (using original artist id since artistId is not being updated)
            val artist = buildArtistAlias(id = originalTrack.artistId, userId = userId)
            whenever(artistAliasService.validateArtistBelongsToUser(originalTrack.artistId, userId)).thenReturn(artist)

            whenever(trackMetadataRepository.findById(trackId)).thenReturn(Optional.of(originalTrack))
            whenever(trackMetadataRepository.save(any()))
                .thenReturn(expectedUpdateTrack)

            val savedTrack: TrackMetadata = service.update(trackId, updateTrack, userId)

            assertEquals(expectedUpdateTrack, savedTrack)
            verify(trackMetadataRepository).save(expectedUpdateTrack)
        }

        @Test
        fun `Should throw not existing exception if track doesn't exist`() {
            val userId = "artist1"
            val trackId = "track id"
            val updateTrack = buildUpdateTrackRequest()

            whenever(trackMetadataRepository.findById(trackId)).thenReturn(Optional.empty())
            assertThrows<TrackDoesNotExistException> {
                service.update(trackId, updateTrack, userId)
            }
        }

        @Test
        fun `Should throw exception when artist does not belong to user`() {
            val userId = "artist1"
            val trackId = "track id"
            val newArtistId = "new artist id"
            val originalTrack = buildTrack(id = trackId)
            val updateTrack = buildUpdateTrackRequest(
                artistId = newArtistId,
                version = 0
            )

            // Mock the artist alias to belong to different user
            whenever(artistAliasService.validateArtistBelongsToUser(newArtistId, userId))
                .thenThrow(ArtistAliasDoesNotExistException(newArtistId))

            whenever(trackMetadataRepository.findById(trackId)).thenReturn(Optional.of(originalTrack))

            assertThrows<InvalidArtistIdException> {
                service.update(trackId, updateTrack, userId)
            }
        }
    }
}