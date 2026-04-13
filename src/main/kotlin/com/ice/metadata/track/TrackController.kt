package com.ice.metadata.track

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@RestController
class TrackController(private val trackMetadataService: TrackMetadataService) {

    @GetMapping("/tracks")
    fun getTracks(
        @RequestParam artistId: String,
        pageable: Pageable = Pageable.ofSize(10)
    ): Page<TrackMetadata> {
        return trackMetadataService.findTracksByArtistId(artistId, pageable)
    }
}