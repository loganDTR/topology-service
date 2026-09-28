package it.yourcompany.topologyservice.api.controller;

import it.yourcompany.topologyservice.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Centralised HTTP exception → structured JSON error mapping.
 *
 * <p>All handlers return an {@link ErrorResponse} body so that every error
 * response has the same shape regardless of the failure type — consistent
 * for both service-to-service callers and LLM/AI consumers.
 *
 * <p>Handler precedence (most specific wins):
 * <ol>
 *   <li>{@link ConstraintViolationException} — bean-validation on service methods</li>
 *   <li>{@link HandlerMethodValidationException} — Spring MVC method-level validation (path/query params)</li>
 *   <li>{@link MethodArgumentNotValidException} — {@code @Valid} on request bodies</li>
 *   <li>{@link NoSuchElementException} — generic not-found signal</li>
 *   <li>{@link DataAccessResourceFailureException} — Neo4j or other backend unreachable</li>
 *   <li>{@link Exception} — fallback for anything unexpected</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------------------------------------------------------------------------
    // 400 — Validation errors
    // -------------------------------------------------------------------------

    /**
     * Handles bean-validation failures on {@code @Service} / {@code @Component} methods
     * annotated with {@code @Validated}.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "validation_error", message, request);
    }

    /**
     * Handles Spring MVC method-level validation failures on controller path/query
     * parameters (Spring 6.1+ — replaces the older {@link ConstraintViolationException}
     * path for handler methods annotated with {@code @Validated}).
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {
        String message = ex.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "validation_error", message, request);
    }

    /**
     * Handles {@code @Valid} failures on {@code @RequestBody} parameters.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "validation_error", message, request);
    }

    // -------------------------------------------------------------------------
    // 404 — Not found
    // -------------------------------------------------------------------------

    /**
     * Handles the standard Java {@link NoSuchElementException} as a generic
     * not-found signal. Controllers and services may throw it directly to
     * produce a structured 404 without depending on Spring MVC internals.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(
            NoSuchElementException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "The requested resource was not found";
        return build(HttpStatus.NOT_FOUND, "not_found", message, request);
    }

    // -------------------------------------------------------------------------
    // 503 — Backend unavailable
    // -------------------------------------------------------------------------

    /**
     * Handles Spring Data connectivity failures, including Neo4j
     * {@code ServiceUnavailableException} wrapped by the SDN translation layer.
     * Logged at WARN to avoid alert noise for transient outages.
     */
    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessResourceFailure(
            DataAccessResourceFailureException ex, HttpServletRequest request) {
        log.warn("Data access resource failure: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "service_unavailable",
                "A backend data source is temporarily unavailable. Retry later.", request);
    }

    // -------------------------------------------------------------------------
    // 500 — Unexpected errors
    // -------------------------------------------------------------------------

    /**
     * Catch-all for any exception not handled by a more specific handler.
     * Logged at ERROR with a full stack trace for diagnostics.
     * The response body intentionally omits internal detail to avoid leaking
     * implementation information to callers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
                "An unexpected error occurred. Check service logs for details.", request);
    }

    // -------------------------------------------------------------------------
    // Shared builder
    // -------------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(status.value(), error, message,
                        request.getRequestURI(), Instant.now()));
    }
}

