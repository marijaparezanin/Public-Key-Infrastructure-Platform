package com.ftn.pki.security;


import com.ftn.pki.models.User;
import com.ftn.pki.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtUserFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

            String email = String.valueOf(token.getTokenAttributes().get("email"));
            User user = this.userService.findUserByEmail(email);

            if (user == null) {
                String keycloakId = String.valueOf(token.getTokenAttributes().get("sub"));
                String firstname = String.valueOf(token.getTokenAttributes().get("given_name"));
                String lastname = String.valueOf(token.getTokenAttributes().get("family_name"));
                String organization = String.valueOf(token.getTokenAttributes().get("organization"));
                this.userService.save(new User(null, keycloakId, email, firstname, lastname, organization));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to save user");
        }

        filterChain.doFilter(request, response);
    }
}

