package com.ftn.pki.security;


import com.ftn.pki.models.organizations.Organization;
import com.ftn.pki.models.users.User;
import com.ftn.pki.models.users.UserRole;
import com.ftn.pki.services.organizations.OrganizationService;
import com.ftn.pki.services.users.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class JwtUserFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

            String email = String.valueOf(token.getTokenAttributes().get("email"));
            User user = userService.findUserByEmail(email);

            if (user == null) {
                String keycloakId = String.valueOf(token.getTokenAttributes().get("sub"));
                String firstname = String.valueOf(token.getTokenAttributes().get("given_name"));
                String lastname = String.valueOf(token.getTokenAttributes().get("family_name"));
                String organizationName = String.valueOf(token.getTokenAttributes().get("organization"));

                // --- Role parsing and priority ---
                List<UserRole> priority = List.of(UserRole.ROLE_admin, UserRole.ROLE_ca_user, UserRole.ROLE_ee_user);
                UserRole selectedRole = token.getAuthorities().stream()
                        .map(a -> {
                            try {
                                return UserRole.valueOf(a.getAuthority());
                            } catch (IllegalArgumentException e) {
                                return null; // Ignore unknown roles
                            }
                        })
                        .filter(Objects::nonNull)
                        .min(Comparator.comparingInt(priority::indexOf))
                        .orElse(UserRole.ROLE_ee_user);

                Organization organization = organizationService.findOrganizationByName(organizationName);

                if (organization == null) {
                    organization = organizationService.registerOrganization(organizationName);
                }

                this.userService.save(new User(null, keycloakId, email, firstname, lastname, organization, selectedRole));
            }
        } catch (Exception e) {
            System.out.println("Error in JWT user filter: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

