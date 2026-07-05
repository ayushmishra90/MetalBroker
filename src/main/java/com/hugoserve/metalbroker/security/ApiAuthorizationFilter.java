package com.hugoserve.metalbroker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
//import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;


@Component
@RequiredArgsConstructor
public class ApiAuthorizationFilter extends OncePerRequestFilter {

//    private final NotNullValidator notNullValidator;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Allow preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Public APIs
        if (path.startsWith("/api/v1/auth")
                || path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        // Not logged in OR token expired
        if (auth == null
                || auth instanceof AnonymousAuthenticationToken
                || !auth.isAuthenticated()) {

            write(response, "LOGIN_REQUIRED");
            return;
        }

        // Admin-only
        if (path.startsWith("/api/v1/db/admin")) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                write(response, "ADMIN_REQUIRED");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void write(HttpServletResponse response, String code)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        response.getWriter().write("""
        {
          "success": false,
          "code": "%s"
        }
        """.formatted(code));
    }
}
