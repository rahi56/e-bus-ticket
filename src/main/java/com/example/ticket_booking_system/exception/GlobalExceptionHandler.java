
package com.example.ticket_booking_system.exception;

import com.example.ticket_booking_system.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;
import com.example.ticket_booking_system.exception.PassengerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);
    // HANDLE RESPONSE STATUS EXCEPTIONS
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatusCode status = ex.getStatusCode();

        String errorMessage = ex.getReason() != null
                ? ex.getReason()
                : "Request failed";

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.toString(),
                List.of(errorMessage),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(apiError);
    }

    // HANDLE VALIDATION EXCEPTIONS
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .toList();

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                400,
                "Bad Request",
                validationErrors,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error occurred: ", ex);

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                500,
                "Internal Server Error",
                List.of("An unexpected error occurred. Please try again later."),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(500)
                .body(apiError);
    }
    @ExceptionHandler(PassengerNotFoundException.class)
    public ResponseEntity<ApiError> handlePassengerNotFoundException(
            PassengerNotFoundException ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                404,
                "Not Found",
                List.of(ex.getMessage()),
                request.getRequestURI()
        );

        return ResponseEntity.status(404).body(apiError);
    }
}
