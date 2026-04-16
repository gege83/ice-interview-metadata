package com.ice.metadata.track

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
    lateinit var service: TrackMetadataService

    @BeforeEach
    fun setup() {
        service = TrackMetadataServiceImpl(trackMetadataRepository)
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
            val createTrackRequest = buildCreateTrackRequest(
                artistId = "2345",
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
            whenever(trackMetadataRepository.save(any()))
                .thenReturn(
                    expectedTrackToSave.copy(id = generatedId)
                )
            val savedTrack: TrackMetadata = service.create(createTrackRequest)
            assertEquals(expectedTrackToSave.copy(id = generatedId), savedTrack)
            verify(trackMetadataRepository).save(expectedTrackToSave)
        }
    }

    @Nested
    inner class UpdateExistingTrackForArtistTests {
        @Test
        fun `Should update existing track with all fields`() {
            val trackId = "track id"
            val originalTrack = buildTrack(id = trackId)
            val updateTrack = buildUpdateTrackRequest(
                name = "new Name",
                genre = "Pop",
                length = 123L,
                artistId = "new artist id",
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

            whenever(trackMetadataRepository.findById(trackId)).thenReturn(Optional.of(originalTrack))
            whenever(trackMetadataRepository.save(any()))
                .thenReturn(expectedUpdateTrack)
            val savedTrack: TrackMetadata = service.update(trackId, updateTrack)
            assertEquals(expectedUpdateTrack, savedTrack)
            verify(trackMetadataRepository).save(expectedUpdateTrack)
        }

        @Test
        fun `Should update existing track with non-null fields`() {
            val trackId = "track id"
            val originalTrack = buildTrack(length = 123, id = trackId)
            val newLength = 1234L
            val updateTrack = buildUpdateTrackRequest(length = newLength, version = 0)
            val expectedUpdateTrack = buildTrack(id = trackId, length = newLength, version = 0)

            whenever(trackMetadataRepository.findById(trackId)).thenReturn(Optional.of(originalTrack))
            whenever(trackMetadataRepository.save(any()))
                .thenReturn(expectedUpdateTrack)
            val savedTrack: TrackMetadata = service.update(trackId, updateTrack)
            assertEquals(expectedUpdateTrack, savedTrack)
            verify(trackMetadataRepository).save(expectedUpdateTrack)
        }

        @Test
        fun `Should throw not existing exception if track doesn't exist`() {
            val trackId = "track id"
            val updateTrack = buildUpdateTrackRequest()

            whenever(trackMetadataRepository.findById(trackId)).thenReturn(Optional.empty())
            assertThrows<TrackDoesNotExistException> {
                service.update(trackId, updateTrack)
            }
        }
    }
}