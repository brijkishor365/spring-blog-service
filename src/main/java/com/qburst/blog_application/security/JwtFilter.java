package com.qburst.blog_application.security;

import java.io.IOException;

import com.qburst.blog_application.exception.auth.JwtAuthenticationException;
import com.qburst.blog_application.exception.base.JwtErrorType;
import com.qburst.blog_application.repository.BlacklistedTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.qburst.blog_application.service.jwt.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT authentication filter that processes each incoming request.
 * This filter extracts and validates JWT tokens from the Authorization header,
 * and sets up the security context if the token is valid.
 *
 * @author BrijKishor
 * @version 1.0
 * @since 2025-09-14
 */
@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public JwtFilter(JwtService jwtService,
                     UserDetailsService userDetailsService,
                     BlacklistedTokenRepository blacklistedTokenRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    /**
     * Processes each incoming request to handle JWT-based authentication.
     * This method extracts the JWT token from the Authorization header,
     * validates it, and sets up the security context if the token is valid.
     *
     * @param request     The HTTP request
     * @param response    The HTTP response
     * @param filterChain The filter chain for additional processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token is missing");
            return;
        }

        String token = authHeader.substring(7);

        // LOGOUT / BLACKLIST CHECK
        if (blacklistedTokenRepository.existsByToken(token)) {
            log.warn("Blocked request with blacklisted JWT");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token is blacklisted. Please log in again.");
            return;
        }

        try {
            String username = jwtService.extractUsername(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    // Add userID in log once authenticated
                    MDC.put("userId", "333");
                }
            }
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expired at {}", ex.getClaims().getExpiration());
            throw new JwtAuthenticationException(
                    JwtErrorType.EXPIRED,
                    "JWT expired",
                    ex
            );
        } catch (UnsupportedJwtException ex) {
            log.error("JWT unsupported", ex);
            throw new JwtAuthenticationException(
                    JwtErrorType.UNSUPPORTED,
                    "JWT unsupported",
                    ex
            );
        } catch (MalformedJwtException ex) {
            log.error("JWT malformed", ex);
            throw new JwtAuthenticationException(
                    JwtErrorType.MALFORMED,
                    "JWT malformed",
                    ex
            );
        } catch (SignatureException ex) {
            log.error("JWT signature invalid (token tampered)", ex);
            throw new JwtAuthenticationException(
                    JwtErrorType.SIGNATURE_INVALID,
                    "JWT signature invalid",
                    ex
            );
        } catch (IllegalArgumentException ex) {
            log.error("JWT token is empty or null", ex);
            throw new JwtAuthenticationException(
                    JwtErrorType.EMPTY,
                    "JWT token is empty or null",
                    ex
            );
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();

        // Exclude actuator paths from JWT validation logic
        return path.startsWith("/actuator") || path.startsWith("/manage");
    }
}
