package com.url.filters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.url.models.MyUserDetailsService;
import com.url.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtils jwtUtils;
    private final MyUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtFilter(JwtUtils jwtUtils, MyUserDetailsService userDetailsService,
                     RedisTemplate<String, String> redisTemplate) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        logger.debug("Incoming request path: {}", path);

        if (path.startsWith("/public")) {
            logger.debug("Accessing PUBLIC URL, skipping authentication");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.info("Token",token);

            // Check if token is blacklisted
            try {
                if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
                    logger.warn("Token is blacklisted / expired");
                    sendUnauthorized(response, "User not logged in - Requires Signin");
                    return;
                }
            } catch (Exception e) {
                logger.error("Redis error while checking token blacklist", e);
                sendUnauthorized(response, "Redis server is down");
                return;
            }

            // Extract username from token
            String username;
            try {
                username = jwtUtils.extractUsername(token);
            } catch (Exception e) {
                logger.error("Failed to extract username from token", e);
                sendUnauthorized(response, "Your session has expired. Please log in again.");
                return;
            }

            // Authenticate user if not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userDetailsService.loadUserByUsername(username);
                if (jwtUtils.isTokenValid(token, user.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("User authenticated: {}", username);
                } else {
                    logger.warn("Invalid token for user: {}", username);
                }
            }
        } else {
            logger.debug("No Bearer token found in Authorization header");
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}
