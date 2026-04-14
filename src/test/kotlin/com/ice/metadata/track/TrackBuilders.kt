package com.ice.metadata.track

fun buildTrack(id: String? = null, name: String = "Waiting all night", artistId:String = "123"): TrackMetadata {
    return TrackMetadata(id = id, name = name, artistId = artistId)
}

fun buildCreateTrackRequest(artistId: String, name: String = "Test track"): CreateTrackRequest {
    return CreateTrackRequest(
        name = name,
        artistId = artistId
    )
}