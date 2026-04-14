package com.ice.metadata.track

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable

@RestController
class TrackController(private val trackMetadataService: TrackMetadataService) {

    @GetMapping("/tracks")
    fun getTracks(
        @RequestParam artistId: String,
        @PageableDefault(size = 10, sort = ["id"]) pageable: Pageable
    ): Page<TrackMetadata> {
        return trackMetadataService.findTracksByArtistId(artistId, pageable)
    }

    @PostMapping("/tracks")
    fun createTrack(@RequestBody request: CreateTrackRequest): TrackMetadata {
        val trackMetadata = TrackMetadata(
            id = null,
            name = request.name,
            artistId = request.artistId
        )
        return trackMetadataService.create(trackMetadata)
    }

    @PutMapping("/tracks/{id}")
    fun updateTrack(@PathVariable id: String, @RequestBody request: UpdateTrackRequest): TrackMetadata {
        val trackMetadata = TrackMetadata(
            id = id,
            name = request.name,
            artistId = request.artistId,
            version = request.version
        )
        return trackMetadataService.update(trackMetadata)
    }
}

data class CreateTrackRequest(val name: String, val artistId: String)
data class UpdateTrackRequest(val name: String, val artistId: String, val version: Long)