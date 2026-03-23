package stud.ntnu.no.fullstack_project.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException exception) {
    return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password", Map.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception) {
    return buildResponse(HttpStatus.FORBIDDEN, "You do not have access to this resource", Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();

    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }

    return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiError> handleRuntime(RuntimeException exception) {
    return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
  }

  private ResponseEntity<ApiError> buildResponse(
      HttpStatus status,
      String message,
      Map<String, String> errors
  ) {
    return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), message, errors));
  }
}
