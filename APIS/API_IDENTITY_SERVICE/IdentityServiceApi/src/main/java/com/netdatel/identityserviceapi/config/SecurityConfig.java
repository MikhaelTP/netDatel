package com.netdatel.identityserviceapi.config;

import com.netdatel.identityserviceapi.security.jwt.JwtAuthenticationEntryPoint;
import com.netdatel.identityserviceapi.security.jwt.JwtAuthenticationFilter;
import com.netdatel.identityserviceapi.security.jwt.JwtService;
import com.netdatel.identityserviceapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
// @RequiredArgsConstructor
public class SecurityConfig {

    // private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder; // Inyectado desde EncodingConfig

    // Cambiar el constructor para no incluir JwtAuthenticationFilter
    public SecurityConfig(
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            UserDetailsService userDetailsService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    // Crear un bean para JwtAuthenticationFilter
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, (UserService) userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints - CORREGIR LAS RUTAS
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()  // CAMBIAR DE /auth/** a /api/auth/**
                        .requestMatchers("/api/test/**").permitAll()  // TEMPORAL para testing
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // TEMPORALMENTE: Permitir todo para debuggear
                        /*
                                .requestMatchers("/users/**").permitAll()  // <-- TEMPORAL PARA TESTING
                        .requestMatchers("/roles/**").permitAll()   // <-- TEMPORAL PARA TESTING
                        .requestMatchers("/permissions/**").permitAll() // <-- TEMPORAL PARA TES

                         */


                        // Secured endpoints - CORREGIR LAS RUTAS
                        .requestMatchers("/api/users/**").hasAuthority("ROLE_SUPER_ADMIN")
                        .requestMatchers("/api/roles/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_CLIENT_ADMIN")
                        .requestMatchers("/api/permissions/**").hasAuthority("ROLE_SUPER_ADMIN")
                        .requestMatchers("/api/audit/**").hasAnyRole("SUPER_ADMIN", "CLIENT_ADMIN")
                        .anyRequest().authenticated()


                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://localhost:5173", "http://localhost:5173")); // Más seguro que "*"
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Content-Disposition", "Authorization"));
        configuration.setAllowCredentials(true); // Importante para JWT

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}