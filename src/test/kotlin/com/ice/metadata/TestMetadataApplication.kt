package com.ice.metadata

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<MetadataApplication>().with(TestcontainersConfiguration::class).run(*args)
}
