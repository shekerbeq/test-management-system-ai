package kz.testmanagement.user.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.testmanagement.user.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

    private final AuditService auditService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
        if (request.getRequestURI().startsWith("/api/") && !"GET".equalsIgnoreCase(request.getMethod())) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication == null ? "anonymous" : authentication.getName();
            auditService.record(email, request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        }
    }
}
