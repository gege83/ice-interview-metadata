package com.ice.metadata.track

fun buildTrack(
    id: String? = null,
    name: String = "Test track",
    artistId: String = "123",
    length: Long = 12,
    genre: String? = "Rock",
    version: Long = 0
): TrackMetadata {
    return TrackMetadata(
        id = id,
        name = name,
        artistId = artistId,
        length = length,
        genre = genre,
        version = version
    )
}

fun buildCreateTrackRequest(
    artistId: String = "123",
    name: String = "Test track",
    length: Long = 12,
    genre: String? = "Rock"
): CreateTrackRequest {
    return CreateTrackRequest(
        name = name,
        artistId = artistId,
        length = length,
        genre = genre
    )
}

fun buildUpdateTrackRequest(
    artistId: String? = null,
    name: String? = null,
    length: Long? = null,
    genre: String? = null,
    version: Long = 0
): UpdateTrackRequest {
    return UpdateTrackRequest(
        name = name,
        artistId = artistId,
        length = length,
        genre = genre,
        version = version
    )
}