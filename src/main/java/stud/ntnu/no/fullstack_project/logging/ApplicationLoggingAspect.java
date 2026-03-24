package stud.ntnu.no.fullstack_project.logging;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ApplicationLoggingAspect {

  @Around(
      "execution(* stud.ntnu.no.fullstack_project.controller..*(..)) || "
          + "execution(* stud.ntnu.no.fullstack_project.service..*(..))"
  )
  public Object logApplicationFlow(ProceedingJoinPoint joinPoint) throws Throwable {
    String method = joinPoint.getSignature().toShortString();
    String arguments = summarizeArgs(joinPoint.getArgs());
    long startTime = System.currentTimeMillis();

    log.debug("Entering {} args={}", method, arguments);

    try {
      Object result = joinPoint.proceed();
      long durationMs = System.currentTimeMillis() - startTime;
      log.debug("Exiting {} durationMs={} result={}", method, durationMs, summarizeResult(result));
      return result;
    } catch (Throwable throwable) {
      long durationMs = System.currentTimeMillis() - startTime;
      log.error("Failed {} durationMs={} error={}", method, durationMs, throwable.getMessage(), throwable);
      throw throwable;
    }
  }

  private String summarizeArgs(Object[] args) {
    if (args == null || args.length == 0) {
      return "[]";
    }

    return Arrays.stream(args)
        .map(this::summarizeValue)
        .toList()
        .toString();
  }

  private String summarizeResult(Object result) {
    if (result == null) {
      return "null";
    }

    return summarizeValue(result);
  }

  private String summarizeValue(Object value) {
    if (value == null) {
      return "null";
    }

    String className = value.getClass().getSimpleName();
    String stringValue = String.valueOf(value);

    if (stringValue.length() > 160) {
      stringValue = stringValue.substring(0, 157) + "...";
    }

    return className + "(" + stringValue + ")";
  }
}
