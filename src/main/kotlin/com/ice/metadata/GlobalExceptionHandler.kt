package com.ice.metadata

import com.ice.metadata.artist.AliasAlreadyExistsException
import com.ice.metadata.artist.NoArtistOfTheDayException
import com.ice.metadata.utils.ConflictExceptions
import com.ice.metadata.utils.DoesNotExistsExceptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ConflictExceptions::class)
    fun handleOptimisticLockingFailure(
        ex: ConflictExceptions
    ): ResponseEntity<ErrorResponse> {
        logger.info("Optimistic locking exception: {}", ex.message, ex)
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
        logger.info("Alias already exist exception: {}", ex.message, ex)
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
        logger.info("Does not exist exception: {}", ex.message, ex)
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "The name already exists. Please choose a different alias"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(NoArtistOfTheDayException::class)
    fun handleNoArtistOfTheDayException(
        ex: NoArtistOfTheDayException
    ): ResponseEntity<ErrorResponse> {
        logger.info("No-art of the-day exception: {}", ex.message, ex)
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "No artist alias found in the db"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(AccessDeniedException::class, AuthorizationDeniedException::class)
    fun handleAccessDeniedException(
        ex: AccessDeniedException
    ): ResponseEntity<ErrorResponse> {
        logger.debug("access denied: {}", ex.message, ex)
        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            message = ex.message ?: "Access denied is required."
        )
        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled Exception: {}", ex.message, ex)
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An internal error occurred"
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    data class ErrorResponse(val status: Int, val message: String)
}
