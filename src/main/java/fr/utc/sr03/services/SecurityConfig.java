package fr.utc.sr03.services;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Configuration de la chaîne de sécurité HTTP pour Spring Security.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Désactivation du CSRF pour simplifier les appels API.
                .csrf(AbstractHttpConfigurer::disable)

                // Configuration des règles d’accès.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // API publique.
                        .requestMatchers("/message/**").permitAll() // Envoi de messages par un chemin public.
                        .requestMatchers("/login", "/css/**").permitAll() // Login et CSS publics.
                        .anyRequest().authenticated() // Tout le reste nécessite une connexion (/home notamment)
                )

                // Configuration du formulaire de login.
                .formLogin(form -> form
                        .loginPage("/login") // Page de login custom.
                        .loginProcessingUrl("/login") // POST vers cette URL déclenche la connexion.
                        .defaultSuccessUrl("/home", true) // Redirection après succès.
                        .failureUrl("/login?error") // Redirection après échec.
                        .permitAll()
                )

                // Configuration du logout.
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                // Gestion des erreurs pour les routes API (retour JSON au lieu de redirection HTML).
                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType("application/json");
                                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                                },
                                new AntPathRequestMatcher("/api/**")
                        )
                )

                // Injection du service utilisateur personnalisé.
                .userDetailsService(userDetailsService)

                // Activation du CORS pour autoriser les requêtes front (React).
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    // Configuration des règles CORS (accès front depuis localhost:3000).
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000"); // Autorise les appels depuis le front React.
        configuration.addAllowedMethod("*"); // Toutes les méthodes HTTP (GET, POST, etc.).
        configuration.addAllowedHeader("*"); // Tous les headers.
        configuration.setAllowCredentials(true); // Cookies/session autorisés.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // S’applique à toutes les routes.
        return source;
    }

    // Bean pour l'encodage des mots de passe avec BCrypt.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
