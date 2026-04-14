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
            val artistId = "2345"
            val track = buildTrack(artistId = artistId, id = null)
            val generatedId = "generatedId"
            whenever(trackMetadataRepository.save(track))
                .thenReturn(track.copy(id = generatedId))
            val savedTrack: TrackMetadata = service.create(track)
            assertEquals("generatedId", savedTrack.id)
            assertEquals(artistId, savedTrack.artistId)
        }

        @Test
        fun `Should throw exception when id is not null when creating a new track`() {
            val artistId = "2345"
            val track = buildTrack(artistId = artistId, id = "alreadyExistingId")

            assertThrows<TrackIdAlreadyExistsException> {
                service.create(track)
            }
        }
    }

    @Nested
    inner class UpdateExistingTrackForArtistTests {
        @Test
        fun `Should update existing track`() {
            val artistId = "2345"
            val trackId = "track id"
            val originalTrack = buildTrack(artistId = artistId, name = "some name", id = trackId)
            val newName = "new Name"
            val updateTrack = buildTrack(artistId = artistId, name = newName, id = trackId)

            whenever(trackMetadataRepository.findById(trackId)).thenReturn(Optional.of(originalTrack))
            whenever(trackMetadataRepository.save(updateTrack))
                .thenReturn(updateTrack)
            val savedTrack: TrackMetadata = service.update(updateTrack)
            assertEquals(newName, savedTrack.name)
        }

        @Test
        fun `Should throw not existing exception if track doesn't exist`() {
            val artistId = "2345"
            val trackId = "track id"
            val newName = "new Name"
            val updateTrack = buildTrack(artistId = artistId, name = newName, id = trackId)

            whenever(trackMetadataRepository.findById(trackId)).thenReturn(Optional.empty())
            assertThrows<TrackDoesNotExistException> {
                service.update(updateTrack)
            }
        }
    }
}