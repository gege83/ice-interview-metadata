package com.ice.metadata

import com.ice.metadata.track.TrackDoesNotExistException
import com.ice.metadata.track.TrackHasBeenModifiedException
import com.ice.metadata.track.TrackIdAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TrackHasBeenModifiedException::class)
    fun handleOptimisticLockingFailure(
        ex: TrackHasBeenModifiedException
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "The record you attempted to edit was modified by another user. Please reload the data and try again."
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(TrackIdAlreadyExistsException::class)
    fun handleTrackIdAlreadyExistsException(
        ex: TrackIdAlreadyExistsException,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = ex.message ?: "Track id must be null when creating a new track"
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(TrackDoesNotExistException::class)
    fun handleTrackDoesNotExistException(
        ex: TrackIdAlreadyExistsException,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message ?: "Track with the specified id does not exist"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    data class ErrorResponse(val status: Int, val message: String)
}
