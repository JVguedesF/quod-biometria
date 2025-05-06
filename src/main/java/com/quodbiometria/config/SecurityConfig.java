package com.quodbiometria.config;

import com.quodbiometria.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(UserService userService, JwtAuthenticationFilter jwtAuthFilter, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/auth/**", "/api/health", "/api/info").permitAll()

                        // Endpoints exclusivos para administradores
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Gerenciamento de usuários
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "MANAGER")

                        // Imagens biométricas
                        .requestMatchers("/api/biometric/images/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/biometric/images/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/biometric/images/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/biometric/images/**").authenticated()

                        // Estatísticas e relatórios
                        .requestMatchers(HttpMethod.GET, "/api/statistics/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/reports/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reports/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/reports/**").hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/reports/**").hasRole("ADMIN")

                        // Gerenciamento
                        .requestMatchers("/api/management/**").hasRole("MANAGER")

                        // Endpoints técnicos
                        .requestMatchers("/api/technical/**").hasAnyRole("ADMIN", "TECHNICIAN")

                        // Perfis
                        .requestMatchers(HttpMethod.GET, "/api/profiles/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/profiles/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/profiles/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/profiles/**").hasRole("ADMIN")

                        // Todos os outros endpoints requerem autenticação
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
