package com.ice.metadata.track

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
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
}