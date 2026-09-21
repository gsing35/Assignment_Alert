package com.assignment_alert.Assignment_Alert.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


// Reads from the HTTP Requst Header to isolate the token and see if it matches one in the db. 
// Controller will now use the authenticated user in SecurityContextHolder instead of using userId to know who they are dealing with
@Component
@RequiredArgsConstructor
public class TokenAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final ApiTokenService tokenService;

    // Isolates the token from the HTTP request and checks if it belongs to a user in the db via ApiToken records
    // HttpServletRequest request, the HTTP request that includes the HEADER and BEARER key value pairs, that class wraps up everything the client sends u
    // HttpServlet Response, what you send back to the client, status codes, adding response headers, writing jsons, it doesnt do anything here the mehtods just need it
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);      // Get the Value HEADER corresponds to (Authorization: Bearer secret_token_abc123)

        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length()).trim();        // Isolate just the token part

            tokenService.resolveUser(token).ifPresent(user -> {             // Check if the token matches one in the db when hashed
                var authentication = new UsernamePasswordAuthenticationToken(   // Spring security creates an obj to represent the authenticated user
                        new AuthenticatedUser(user.getUserId()),
                        null,           // No password or token needed bc it resolveUser() did that
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));          // Permissions they have
                SecurityContextHolder.getContext().setAuthentication(authentication);       // Logs user in, now the server knows that the user is autheicated and logged bc it was put into contextholder 
            });
        }

        chain.doFilter(request, response);              // Next request
    }
}
