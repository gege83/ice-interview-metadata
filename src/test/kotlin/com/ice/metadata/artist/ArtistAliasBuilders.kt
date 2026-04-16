package com.ice.metadata.artist

import java.time.LocalDate

fun buildArtistAlias(
    id: String? = null,
    name: String = "artist",
    description: String = "artist description",
    userId: String = "artist1",
    version: Long = 0,
    artistOfTheDayDate: LocalDate? = null,
): ArtistAlias {
    return ArtistAlias(
        id = id,
        name = name,
        description = description,
        userId = userId,
        version = version,
        artistOfTheDayDate = artistOfTheDayDate,
    )
}

fun buildCreateArtistAliasRequest(
    name: String = "artist alias1",
    description: String = "description"
): CreateArtistAliasRequest =
    CreateArtistAliasRequest(name = name, description = description)

fun buildUpdateArtistAliasRequest(
    name: String? = null,
    description: String? = null,
    version: Long = 0
) = UpdateArtistAliasRequest(
    name = name,
    description = description,
    version = version
)