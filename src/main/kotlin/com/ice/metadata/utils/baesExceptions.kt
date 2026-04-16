package com.ice.metadata.utils

open class DoesNotExistsExceptions(id: String, entityName: String) :
    RuntimeException("$entityName with id $id does not exist")

open class ConflictExceptions(id: String, entityName: String) :
    RuntimeException("$entityName with id $id has been modified. Please reload the data and try again")