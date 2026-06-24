package com.investrocket.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(false, "Validation failed", errors, Instant.now()));
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatch(PasswordMismatchException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                false,
                "Validation failed",
                Map.of("confirmPassword", exception.getMessage()),
                Instant.now()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                false,
                exception.getMessage(),
                Map.of("email", exception.getMessage()),
                Instant.now()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                false,
                exception.getMessage(),
                Map.of(),
                Instant.now()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                false,
                exception.getMessage(),
                Map.of(),
                Instant.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                false,
                "Access denied",
                Map.of(),
                Instant.now()));
    }

    @ExceptionHandler({
            InvalidMarketDataRequestException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidMarketDataRequest(Exception exception) {
        String message = exception instanceof MissingServletRequestParameterException
                ? "Search query is required"
                : exception.getMessage();
        return ResponseEntity.badRequest().body(new ErrorResponse(
                false,
                message,
                Map.of(),
                Instant.now()));
    }

    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStockNotFound(StockNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                false,
                exception.getMessage(),
                Map.of(),
                Instant.now()));
    }

    @ExceptionHandler(MarketDataRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleMarketDataRateLimit(
            MarketDataRateLimitException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorResponse(
                false,
                exception.getMessage(),
                Map.of(),
                Instant.now()));
    }

    @ExceptionHandler(MarketDataConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleMarketDataConfiguration(
            MarketDataConfigurationException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse(
                false,
                exception.getMessage(),
                Map.of(),
                Instant.now()));
    }

    @ExceptionHandler(MarketDataProviderException.class)
    public ResponseEntity<ErrorResponse> handleMarketDataProvider(
            MarketDataProviderException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(
                false,
                "Unable to fetch market data",
                Map.of(),
                Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                false,
                "An unexpected error occurred",
                Map.of(),
                Instant.now()));
    }

    public record ErrorResponse(
            boolean success,
            String message,
            Map<String, String> errors,
            Instant timestamp) {
    }
}
