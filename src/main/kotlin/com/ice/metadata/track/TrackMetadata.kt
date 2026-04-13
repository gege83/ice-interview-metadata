package com.ice.metadata.track

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

data class TrackMetadata(val id: String, val name: String) {
}

interface TrackMetadataService {
    fun findTracksByArtistId(artistId: String, pageable: Pageable): Page<TrackMetadata>
}

@Component
class DummyTrackMetadataService : TrackMetadataService {
    override fun findTracksByArtistId(
        artistId: String,
        pageable: Pageable
    ): Page<TrackMetadata> {
        return PageImpl(listOf(TrackMetadata(id="1", name="test")))
    }
}


