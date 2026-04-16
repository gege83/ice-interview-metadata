package com.ice.metadata.track

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/tracks")
class TrackController(private val trackMetadataService: TrackMetadataService) {

    @GetMapping
    fun getTracks(
        @RequestParam artistId: String,
        @PageableDefault(size = 10, sort = ["id"]) pageable: Pageable
    ): Page<TrackMetadata> {
        return trackMetadataService.findTracksByArtistId(artistId, pageable)
    }

    @PostMapping
    @Secured("ROLE_ARTIST")
    fun createTrack(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: CreateTrackRequest
    ): TrackMetadata {
        return trackMetadataService.create(request, userDetails.username)
    }

    @PutMapping("/{id}")
    @Secured("ROLE_ARTIST")
    fun updateTrack(
        @PathVariable id: String,
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: UpdateTrackRequest
    ): TrackMetadata {
        return trackMetadataService.update(id=id, updateTrack = request, userId = userDetails.username)
    }
}