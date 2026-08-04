package com.fellowlodge.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MustChangePasswordFilter mustChangePasswordFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector)
            throws Exception {
        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);
        boolean devProfile = environment.acceptsProfiles(Profiles.of("dev"));
        boolean prodProfile = environment.acceptsProfiles(Profiles.of("prod"));

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                })
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> {
                    // Public browsing (guest portal without auth)
                    auth.requestMatchers(mvc.pattern("/api/public/**")).permitAll()
                            // Version metadata for the desktop update checker
                            .requestMatchers(mvc.pattern("/api/version")).permitAll()
                            // Room availability for the guest portal booking widget
                            .requestMatchers(mvc.pattern("/api/rooms/available")).permitAll()
                            // Restaurant menu & categories are public (guest portal browsing)
                            .requestMatchers(mvc.pattern("/api/restaurant/categories/**")).permitAll()
                            .requestMatchers(mvc.pattern("/api/restaurant/menu/**")).permitAll()
                            // Authentication endpoints
                            .requestMatchers(mvc.pattern("/api/auth/**")).permitAll()
                            // Uploaded files (served both at root and under /api
                            // because the portal resolves relative paths against
                            // its API base URL)
                            .requestMatchers(mvc.pattern("/uploads/**")).permitAll()
                            .requestMatchers(mvc.pattern("/api/uploads/**")).permitAll()
                            // Actuator health
                            .requestMatchers(mvc.pattern("/actuator/health")).permitAll()
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    // OpenAPI / Swagger: development convenience, not exposed in production
                    if (!prodProfile) {
                        auth.requestMatchers(mvc.pattern("/v3/api-docs/**")).permitAll()
                                .requestMatchers(mvc.pattern("/swagger-ui/**")).permitAll()
                                .requestMatchers(mvc.pattern("/swagger-ui.html")).permitAll();
                    }
                    // H2 console is a development-only tool and must never be reachable in production
                    if (devProfile) {
                        auth.requestMatchers(mvc.pattern("/h2-console/**")).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(mustChangePasswordFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
