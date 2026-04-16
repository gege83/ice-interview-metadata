package com.ice.metadata

import org.springframework.boot.fromApplication

fun main(args: Array<String>) {
	fromApplication<MetadataApplication>().run(*args)
}
