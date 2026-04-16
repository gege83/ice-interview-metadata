package com.ice.metadata.track

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.annotation.Secured
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
    @Secured("ROLE_ARTIST")
    fun createTrack(@RequestBody request: CreateTrackRequest): TrackMetadata {
        //TODO user can make create the track with the data
        return trackMetadataService.create(request)
    }

    @PutMapping("/tracks/{id}")
    @Secured("ROLE_ARTIST")
    fun updateTrack(@PathVariable id: String, @RequestBody request: UpdateTrackRequest): TrackMetadata {
        //TODO make sure that id can be modified by user
        return trackMetadataService.update(id=id, updateTrack = request)
    }
}