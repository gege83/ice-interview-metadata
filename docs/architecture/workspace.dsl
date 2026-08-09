workspace "ICE Interview" "Design for ICE interview exercise" {

    !identifiers hierarchical

    model {
        a = person "Artist" "Music Owner"
        u = person "User" "Browswes music record"
        dl = softwareSystem "Data Lake" {
            tags "External"
        }
        ss = softwareSystem "Music metadata system" {
            wa = container "Web Application" {
                group "demo" {
                    spa = component "Single Page Application" "Demonstrates system capabilities" {
                        technology "Vanila JS and HTML"
                        tags "Demo"
                    }
                    login = component "Login" "Handles user login and authentication" {
                        technology "Vanila JS and HTML"
                        tags "Demo"
                    }
                    authentication = component "Authentication" "Handles user authentication" {
                        technology "Spring Security"
                        tags "Demo"
                    }
                    seed = component "Seed" "Seeds the database with initial data" {
                        technology "Java"
                        tags "Demo"
                    }
                }
                t = component "Track API" "Manages track metadata" {
                    technology "Java"
                }
                ts = component "Track Service" "Manages track metadata" {
                    technology "Java"
                }
                a = component "Artist alias API" "Manages artists" {
                    technology "Java"
                }
                as = component "Artist service" "Handles artist alias changes" {
                    technology "Java"
                }
                aotd = component "Artist of the day" "Artist of the day" {
                    technology "Java"
                }
                aotds = component "Artist of the day service" "Selects artist of the day. If already selected of today we return it. If not we select a new one and save it to the database" {
                    technology "Java"
                }
                tr = component "Track repository" "Handles track metadata persistence" {
                    technology "Java"
                }
                ar = component "Artist repository" "Handles artist metadata persistence" {
                    technology "Java"
                }
                t -> ts "Manages track metadata changes"
                ts -> tr "Reads from and writes to"
                a -> as "manages artist alias changes"
                as -> ar "Reads from and writes to"
                aotd -> aotds "Get Artist of the day from"
                aotds -> ar "reads selected artist of the day from"
                aotds -> ar "writes selected artist of the day to"
                spa -> t "Fetches track metadata from"
                spa -> a "Fetches artist metadata from"
                login -> authentication "Authenticates user"
                spa -> seed "Seeds the database with initial data"
                spa -> aotd "Fetches artist of the day from"
                seed -> tr "Seeds track metadata to"
                seed -> ar "Seeds artist metadata to"
            }
            db = container "Metadata storage" "postgreSQL database" {
                tags "Database"
            }
            wa.tr -> db "Reads from and writes to"
            wa.ar -> db "Reads from and writes to"
        }

        a -> ss "Manage my music metadata in"
        a -> ss.wa.t "Add music metadata to a catalogue in"
        a -> ss.wa.a "Edit my musics metadata in"
        a -> ss.wa.a "Manage artist alias in"
        a -> ss.wa.t "Fetch my musics metadata from"
        u -> ss.wa.aotd "Fetch artist of the day from"
        u -> ss.wa.t "Fetch music metadata from"
        ss.wa -> dl "Sends music metadata changes to" "via messaging queue"
        a -> ss.wa.login "Login to manage my music metadata in"
        a -> ss.wa.spa "Manage my music metadata in"
        u -> ss.wa.spa "Browse my music metadata in"
    }

    views {

        systemContext ss "SystemContext" "We are focusing on the music metadata system. We are assuming that all requests are going through an api gateway that will force authentication when it needs to and sends an authentication token to the music metadata system" {
            include *
        }

        container ss "Container" {
            include *
        }

        component ss.wa "WebApplication-context" {
            include *
        }

        filtered "WebApplication-context" exclude "Demo" "WebApplication-context1"
        filtered "WebApplication-context" include "Demo,Relationship,Person,Service" "WebApplication-context2"


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
