package stud.ntnu.no.fullstack_project.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    long startTime = System.currentTimeMillis();
    String queryString = request.getQueryString();
    String path = queryString == null ? request.getRequestURI() : request.getRequestURI() + "?" + queryString;

    log.info("Incoming request {} {} from ip={} userAgent={}",
        request.getMethod(),
        path,
        request.getRemoteAddr(),
        request.getHeader("User-Agent"));

    try {
      filterChain.doFilter(request, response);
    } finally {
      long durationMs = System.currentTimeMillis() - startTime;
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      String user = authentication != null ? authentication.getName() : "anonymous";

      log.info("Completed request {} {} status={} durationMs={} user={}",
          request.getMethod(),
          path,
          response.getStatus(),
          durationMs,
          user);
    }
  }
}
