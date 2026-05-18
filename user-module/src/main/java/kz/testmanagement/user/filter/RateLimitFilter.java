package kz.testmanagement.user.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SECONDS = 60;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/tests/generate")) {
            String key = request.getRemoteAddr() + ":" + request.getHeader("Authorization");
            Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket());
            long now = Instant.now().getEpochSecond();
            if (now - bucket.windowStartedAt >= WINDOW_SECONDS) {
                bucket.windowStartedAt = now;
                bucket.count = 0;
            }
            bucket.count++;
            if (bucket.count > MAX_REQUESTS) {
                response.sendError(429, "AI сұраныстарының лимиті: минутына 10");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private static class Bucket {
        long windowStartedAt = Instant.now().getEpochSecond();
        int count;
    }
}
