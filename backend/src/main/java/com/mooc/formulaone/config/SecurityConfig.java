package com.mooc.formulaone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
/**
 * Centralise la configuration de sécurité HTTP et du hashage des mots de passe.
 */
public class SecurityConfig {

    @Bean
    /**
     * Définit la chaîne de filtres de sécurité utilisée par Spring Security.
     *
     * @param http objet de configuration HTTP fourni par Spring
     * @return la chaîne de filtres configurée
     * @throws Exception en cas d'échec de configuration
     */
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    /**
     * Fournit l'encodeur BCrypt utilisé pour stocker les mots de passe.
     *
     * @return encodeur de mots de passe BCrypt
     */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
