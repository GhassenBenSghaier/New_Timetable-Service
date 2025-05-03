package tn.esprit.new_timetableservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Ensure stateless
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // ProgramController endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/program").hasAuthority("ADD_PROGRAM")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/program/**").hasAuthority("EDIT_PROGRAM")
                        .requestMatchers(HttpMethod.GET, "/api/v1/program/**").hasAuthority("VIEW_PROGRAM")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/program/**").hasAuthority("DELETE_PROGRAM")
                        // TimetableController endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/timetable/capacity").hasAuthority("VIEW_CAPACITY")
                        .requestMatchers(HttpMethod.POST, "/api/v1/timetable/generate").hasAuthority("GENERATE_TIMETABLE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/timetable/**").hasAuthority("VIEW_TIMETABLE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/timetable/**").hasAuthority("EDIT_TIMETABLE")
                        // ClassController endpoints
                        .requestMatchers("/api/v1/classes/**").hasAuthority("MANAGE_CLASSES")
                        // ClassroomController endpoints
                        .requestMatchers("/api/v1/classrooms/**").hasAuthority("MANAGE_CLASSROOMS")
                        // LevelController endpoints
                        .requestMatchers("/api/v1/levels/**").hasAuthority("MANAGE_LEVELS")
                        // SchoolController endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/schools").hasAnyAuthority("MANAGE_SCHOOLS", "MANAGE_CLASSROOMS", "MANAGE_TIME_SLOTS", "MANAGE_CLASSES")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/schools/**").hasAuthority("MANAGE_SCHOOLS")
                        .requestMatchers(HttpMethod.GET, "/api/v1/schools/**").hasAnyAuthority("MANAGE_SCHOOLS", "MANAGE_CLASSROOMS", "MANAGE_TIME_SLOTS", "MANAGE_CLASSES")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/schools/**").hasAuthority("MANAGE_SCHOOLS")
                        // SpecialtyController endpoints
                        .requestMatchers("/api/v1/specialties/**").hasAuthority("MANAGE_SPECIALTIES")
                        // SubjectController endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/subjects/**").hasAnyAuthority("MANAGE_SUBJECTS", "MANAGE_CLASSROOMS", "MANAGE_TIME_SLOTS", "MANAGE_CLASSES")
                        .requestMatchers("/api/v1/subjects/**").hasAuthority("MANAGE_SUBJECTS")
                        // TimeSlotController endpoints
                        .requestMatchers("/api/v1/timeslots/**").hasAuthority("MANAGE_TIME_SLOTS")
                        // TeacherController endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/teachers").hasAuthority("ADD_USER_TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/teachers/**").hasAuthority("EDIT_USER_TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/teachers/**").hasAuthority("DELETE_USER_TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/teachers/**").hasAuthority("EDIT_USER_TEACHER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/teachers/*/programs/*").hasAuthority("EDIT_USER_TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/teachers/*/programs/*").hasAuthority("EDIT_USER_TEACHER")
                        // Public endpoints
                        .requestMatchers("/new-timetable-service/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter();
    }

    public static class JwtAuthorizationFilter extends OncePerRequestFilter {
        private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

        @Value("${jwt.secret}")
        private String secretKeyBase64;

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            logger.debug("Received headers: {}", Collections.list(request.getHeaderNames()));
            String header = request.getHeader("Authorization");
            logger.debug("Authorization header: {}", header);

            String path = request.getRequestURI();
            logger.debug("Request URI: {}", path);

            if (header == null || !header.startsWith("Bearer ")) {
                logger.warn("No Bearer token found in Authorization header");
                chain.doFilter(request, response);
                return;
            }

            String token = header.replace("Bearer ", "");
            try {
                byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                List<String> permissions = claims.get("permissions", List.class) != null ?
                        claims.get("permissions", List.class) : Collections.emptyList();

                logger.debug("JWT Validation - Username: {}, Role: {}, Permissions: {}", username, role, permissions);

                if (username != null && role != null) {
                    var authorities = permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

                    var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authenticated user: {}, authorities: {}", username, authorities);
                }
            } catch (Exception e) {
                logger.error("JWT Validation Failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }

            chain.doFilter(request, response);
        }
    }
}