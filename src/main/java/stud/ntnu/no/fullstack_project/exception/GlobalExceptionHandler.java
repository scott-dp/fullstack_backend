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

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles illegal argument failures from application code.
   *
   * @param exception thrown exception
   * @return standardized API error response
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
    log.warn("Handled illegal argument exception message={}", exception.getMessage());
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
  }

  /**
   * Handles failed authentication attempts.
   *
   * @param exception thrown exception
   * @return standardized API error response
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException exception) {
    log.warn("Handled bad credentials exception");
    return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password", Map.of());
  }

  /**
   * Handles access-denied errors from Spring Security.
   *
   * @param exception thrown exception
   * @return standardized API error response
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception) {
    log.warn("Handled access denied exception message={}", exception.getMessage());
    return buildResponse(HttpStatus.FORBIDDEN, "You do not have access to this resource", Map.of());
  }

  /**
   * Handles bean-validation failures for request payloads.
   *
   * @param exception thrown exception
   * @return standardized API error response containing field validation details
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();

    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }

    log.warn("Handled validation exception errors={}", errors);
    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
  }

  /**
   * Handles uncaught runtime exceptions not matched by a more specific handler.
   *
   * @param exception thrown exception
   * @return standardized API error response
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiError> handleRuntime(RuntimeException exception) {
    log.error("Handled runtime exception message={}", exception.getMessage(), exception);
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
  }

  /**
   * Creates the standardized error response body and HTTP status wrapper.
   *
   * @param status HTTP status to return
   * @param message top-level error message
   * @param errors optional validation errors
   * @return response entity wrapping the generated {@link ApiError}
   */
  private ResponseEntity<ApiError> buildResponse(
      HttpStatus status,
      String message,
      Map<String, String> errors
  ) {
    return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), message, errors));
  }
}
