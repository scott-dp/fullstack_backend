package stud.ntnu.no.fullstack_project.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that translates application exceptions into
 * standardised {@link ApiError} responses.
 *
 * <p>Each handler method maps a specific exception type to an HTTP status code
 * and a human-readable error message.</p>
 */
@Slf4j
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles illegal argument exceptions (e.g. validation failures, not-found errors).
   *
   * @param exception the thrown exception
   * @return a 400 Bad Request response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
    log.warn("IllegalArgumentException: {}", exception.getMessage());
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
  }

  /**
   * Handles bad credentials during authentication.
   *
   * @param exception the thrown exception
   * @return a 401 Unauthorized response
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException exception) {
    log.warn("BadCredentialsException: Invalid username or password");
    return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password", Map.of());
  }

  /**
   * Handles access denied exceptions for insufficient permissions.
   *
   * @param exception the thrown exception
   * @return a 403 Forbidden response
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception) {
    log.warn("AccessDeniedException: {}", exception.getMessage());
    return buildResponse(HttpStatus.FORBIDDEN, "You do not have access to this resource", Map.of());
  }

  /**
   * Handles bean validation failures and returns field-level error details.
   *
   * @param exception the thrown validation exception
   * @return a 400 Bad Request response with per-field errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();

    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }

    log.warn("Validation failed: {}", errors);
    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
  }

  /**
   * Catches any other runtime exceptions as a fallback handler.
   *
   * @param exception the thrown exception
   * @return a 400 Bad Request response
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiError> handleRuntime(RuntimeException exception) {
    log.error("Unhandled RuntimeException: {}", exception.getMessage(), exception);
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
  }

  /**
   * Builds a standardised error response.
   *
   * @param status  the HTTP status code
   * @param message the error message
   * @param errors  field-level validation errors (may be empty)
   * @return the error response entity
   */
  private ResponseEntity<ApiError> buildResponse(
      HttpStatus status,
      String message,
      Map<String, String> errors
  ) {
    return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), message, errors));
  }
}
