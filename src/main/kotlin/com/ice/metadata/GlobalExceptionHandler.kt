package com.ice.metadata

import com.ice.metadata.artist.AliasAlreadyExistsException
import com.ice.metadata.utils.ConflictExceptions
import com.ice.metadata.utils.DoesNotExistsExceptions
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ConflictExceptions::class)
    fun handleOptimisticLockingFailure(
        ex: ConflictExceptions
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message
                ?: "The record you attempted to edit was modified by another user. Please reload the data and try again."
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(AliasAlreadyExistsException::class)
    fun handleAliasAlreadyExistsException(
        ex: AliasAlreadyExistsException
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "The name already exists. Please choose a different alias"
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(DoesNotExistsExceptions::class)
    fun handleDoesNotExistsException(
        ex: DoesNotExistsExceptions
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "The name already exists. Please choose a different alias"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    data class ErrorResponse(val status: Int, val message: String)
}
