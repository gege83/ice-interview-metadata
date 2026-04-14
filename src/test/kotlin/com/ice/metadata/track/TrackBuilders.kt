package com.ice.metadata.track

fun buildTrack(id: String = "321", name: String = "Waiting all night"): TrackMetadata {
    return TrackMetadata(id = id, name = name, artistId = "123")
}