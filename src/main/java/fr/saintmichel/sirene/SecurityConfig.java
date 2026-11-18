package fr.saintmichel.sirene;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API REST sans session ni cookie : chaque requête porte son jeton, le CSRF ne s'applique pas
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Documentation publique
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Lecture : lecteur ou admin
                        .requestMatchers(HttpMethod.GET, "/entreprises/**").hasAnyRole("lecteur", "admin")
                        // Modification et suppression : admin uniquement
                        .requestMatchers(HttpMethod.PUT, "/entreprises/**").hasRole("admin")
                        .requestMatchers(HttpMethod.DELETE, "/entreprises/**").hasRole("admin")
                        .anyRequest().authenticated())
                // Exige un en-tête "Authorization: Bearer <JWT>" et vérifie sa signature RS256
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRoles())));
        return http.build();
    }

    /**
     * Keycloak place les rôles du realm dans le jeton sous {"realm_access": {"roles": [...]}}.
     * Spring attend des autorités "ROLE_xxx" : on fait la traduction.
     */
    private JwtAuthenticationConverter keycloakRoles() {
        Converter<Jwt, Collection<GrantedAuthority>> roles = jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> liste)) {
                return List.of();
            }
            return liste.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        };
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }
}
