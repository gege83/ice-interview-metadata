# Domain

This is a high level overview of the music metadata landscape. 
It is not meant to be exhaustive, but rather to give an idea of the main entities and their relationships.
This domain doesn't include artist of the day feature.
```mermaid
---
title: Music metadata landscape
---
erDiagram
    LEGAL_ENTITY ||--|{ ARTIST : represents
    LEGAL_ENTITY ||--|{ PUBLISHER : represents
    PUBLISHER ||--|{ TRACK : publishes
    PUBLISHER ||--|{ BAND : manages
    PUBLISHER ||--|{ ALBUM : publishes
    PUBLISHER ||--|{ ARTIST : manages
    ALBUM }|--|{ TRACK : contains
    ARTIST ||--|{ BAND_MEMBER : is
    BAND_MEMBER }|--|| BAND : has
    BAND }|--|| TRACK : has
    TRACK ||--|{ OWNERSHIP : has
    LEGAL_ENTITY ||--|{ OWNERSHIP : has
    ARTIST {
        string name
        string description
        string legal_entity_id
    }
    BAND_MEMBER {
        string artist_id
        string band_id
        date start
        date end
    }
    BAND {
        string name
        date created
    }
    TRACK {
        string title
        int length
        string band
        string genre
        string record_style
        date recording
    }
    OWNERSHIP {
        string track_id
        string legal_entity_id
        int percentage
        date start
        date end
    }
    PUBLISHER {
        string name
        string legal_entity_id
    }
    LEGAL_ENTITY{
        string name
        string type
        string country
        string description
        string legal_entity_identification_document_number
        string legal_entity_identification_document_type
    }
    ALBUM {
        string title
        date release_date
    }
```

# Simplified domain

Clearly this is too complex for a take home interview project so I will implement the following domain to satisfy the requirements of the project:
- I will assume that a track is created by one artist.
- I will assume that a track is owned by one legal entity.
- A legal entity can be represented by multiple artists (aliases).
- I will not model publishers, bands, albums, or ownership percentages.
- I will not model the history of artists, bands, or ownership. I will assume that the history of these are recorded by another part of the system which can be queried easily by the client applications.
- I will assume that the legal entity is the user who logs in to the system and managed by the authentication system.

This means that the legal entity doesn't need to be stored in this system. 

```mermaid
---
title: Simplified music metadata landscape
---
erDiagram
    ARTIST ||--|{ TRACK : creates
    ARTIST {
        string name
        string description
        string legal_entity_id
    }
    TRACK {
        string title
        int length
        string genre
        string record_style
        date recording
        string legal_entity_id
    }
```