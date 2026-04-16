workspace "ICE Interview" "Design for ICE interview exercise" {

    !identifiers hierarchical

    model {
        a = person "Artist" "Music Owner"
        u = person "User" "Browswes music record"
        dl = softwareSystem "Data Lake" {
            tags "External"
        }
        ss = softwareSystem "Music metadata system" {
            wa = container "Web Application"
            db = container "Metadata storage" "postgreSQL database" {
                tags "Database"
            }
        }

        a -> ss "Manage my music metadata in"
        a -> ss.wa "Add music metadata to a catalogue in"
        a -> ss.wa "Edit my musics metadata in"
        a -> ss.wa "Manage artist alias in"
        a -> ss.wa "Fetch my musics metadata from"
        u -> ss.wa "Fetch artist of the day from"
        u -> ss.wa "Fetch music metadata from"
        ss.wa -> ss.db "Reads from and writes to"
        ss.wa -> dl "Sends music metadata changes to" "via messaging queue"
    }

    views {

        systemContext ss "SystemContext" "We are focusing on the music metadata system. We are assuming that all requests are going through an api gateway that will force authentication when it needs to and sends an authentication token to the music metadata system" {
            include *
        }

        container ss "Container" {
            include *
        }

        styles {
            element "Element" {
                color #0773af
                stroke #0773af
                strokeWidth 7
                shape roundedbox
            }
            element "Person" {
                shape person
            }
            element "Database" {
                shape cylinder
            }
            element "Boundary" {
                strokeWidth 5
            }
            relationship "Relationship" {
                thickness 4
            }
        }
    }

}
