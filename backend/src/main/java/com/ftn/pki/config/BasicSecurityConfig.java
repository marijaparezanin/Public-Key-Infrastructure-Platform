package com.ftn.pki.config;


import com.ftn.pki.security.JwtRoleConverter;
import com.ftn.pki.security.JwtUserFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class BasicSecurityConfig {

    @Autowired
    private JwtRoleConverter jwtRoleConverter;

    @Autowired
    private JwtUserFilter jwtUserFilter;


    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults());

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtRoleConverter)));

        http.authorizeHttpRequests((requests) -> requests
                .requestMatchers(HttpMethod.GET, "/api/crl/*/latest").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/create-ca").hasRole("admin")
                .requestMatchers(HttpMethod.GET, "/api/organizations").hasRole("admin")
                .requestMatchers(HttpMethod.POST, "/api/certificates/templates").hasRole("ca_user")
                .requestMatchers(HttpMethod.GET, "/api/certificates/templates/ca/*").hasAnyRole("admin","ee_user","ca_user")
                .requestMatchers(HttpMethod.GET, "/api/certificates/templates/*").hasRole("ca_user")
                .requestMatchers(HttpMethod.POST, "/api/certificates").hasAnyRole("ca_user","admin")
                .requestMatchers(HttpMethod.POST, "/api/certificates/ee").hasRole("ee_user")
                .requestMatchers(HttpMethod.GET, "/api/certificates/applicable-ca").hasAnyRole("ca_user","ee_user","admin")
                .requestMatchers(HttpMethod.GET, "/api/certificates/all").hasAnyRole("ca_user","ee_user","admin")
                .requestMatchers(HttpMethod.PUT, "/api/certificates/revoke").hasAnyRole("ca_user","ee_user","admin")
                .requestMatchers(HttpMethod.POST, "/api/certificates/download").hasAnyRole("ca_user","ee_user","admin")
                .requestMatchers(HttpMethod.POST, "/api/certificates/upload-csr").hasRole("ee_user")

                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtUserFilter, BasicAuthenticationFilter.class);
        return http.build();
    }
}
