package com.ice.metadata.track

fun buildTrack(id: String? = null, name: String = "Waiting all night", artistId: String = "123", version: Long = 0): TrackMetadata {
    return TrackMetadata(id = id, name = name, artistId = artistId, version = version)
}

fun buildCreateTrackRequest(artistId: String, name: String = "Test track"): CreateTrackRequest {
    return CreateTrackRequest(
        name = name,
        artistId = artistId
    )
}

fun buildUpdateTrackRequest(artistId: String, name: String = "Test track", version: Long = 0): UpdateTrackRequest {
    return UpdateTrackRequest(
        name = name,
        artistId = artistId,
        version = version
    )
}