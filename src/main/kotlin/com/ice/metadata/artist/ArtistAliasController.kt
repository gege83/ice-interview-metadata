package com.ice.metadata.artist

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ArtistAliasController(val artistAliasService: ArtistAliasService) {
    @GetMapping("/artists")
    fun getArtistsForCurrentUser(@AuthenticationPrincipal userDetails: UserDetails): ArtistAliasResponse {
        return ArtistAliasResponse(content = artistAliasService.findAllArtistAliasesFor(userDetails.username))
    }

    @PostMapping("/artists")
    fun createArtistAliasForUser(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody artistAliasDetails: CreateArtistAliasRequest
    ): ArtistAlias {
        return artistAliasService.createArtistAlias(userDetails.username, artistAliasDetails)
    }

    @PutMapping("/artists/{artistId}")
    fun createArtistAliasForUser(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable artistId: String,
        @RequestBody artistAliasDetails: UpdateArtistAliasRequest
    ): ArtistAlias {
        return artistAliasService.updateArtistAlias(artistId, userDetails.username, artistAliasDetails)
    }
}

data class ArtistAliasResponse(val content: List<ArtistAlias> = listOf())

