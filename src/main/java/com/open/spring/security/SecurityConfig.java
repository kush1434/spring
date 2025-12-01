package com.open.spring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
/*
* To enable HTTP Security in Spring
*/


/*
 * THIS FILE IS IMPORTANT
 *
 * you can configure which http requests need to be authenticated or not
 * for example, you can change the /authenticate to "authenticated()" or "permitAll()"
 * --> obviously, you want to set it to permitAll() so anyone can login. it doesn't make sense
 *     to have to login first before authenticating!
 *
 * another example is /mvc/person/create/** which i changed to permitAll() so anyone can make an account.
 * it doesn't make sense to have to login to make your account!
 * additionally, this file is important for security configuration, please do not remove it
 */


@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // Inject the RateLimitFilter for SecurityConfig
    @Autowired
    private RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtRequestFilter jwtRequestFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                // JWT related configuration
                .csrf(csrf -> csrf.disable())
                // .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) OBSOLETE, OVERWRITTEN BY BELOW
                .authorizeHttpRequests(auth -> auth


                        // API ------------------------------------------------------------------------------
                        .requestMatchers(HttpMethod.POST, "/authenticate").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/analytics/**").permitAll()  
                        .requestMatchers(HttpMethod.POST, "/api/person/**").permitAll()          
                        .requestMatchers(HttpMethod.GET,"/api/person/{id}/balance").permitAll() // Allow unauthenticated access to this endpoint
                        .requestMatchers(HttpMethod.GET, "/api/person/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/people/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/assets/upload").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/assets/upload/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/assets/uploads").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/person/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/person/**").hasAuthority("ROLE_ADMIN")
                       
                        .requestMatchers(HttpMethod.GET, "/api/plant/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/plant/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/plant/**").permitAll()
                       
                        .requestMatchers(HttpMethod.POST, "/api/synergy/grades/requests").hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/synergy/**").hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN")
                       
                       
                        .requestMatchers(HttpMethod.DELETE, "/api/synergy/saigai/").hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/calendar/add").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/calendar/add_event").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/calendar/edit/{id}").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/calendar/delete/{id}").permitAll()
                   
                        .requestMatchers(HttpMethod.GET,"/api/train/**").authenticated()
                       
                )
                .cors(Customizer.withDefaults())
                .headers(headers -> headers
                        .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Credentials", "true"))
                        .addHeaderWriter(
                                new StaticHeadersWriter("Access-Control-Allow-ExposedHeaders", "*", "Authorization"))
                        .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Headers", "Content-Type",
                                "Authorization", "x-csrf-token"))
                        .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-MaxAge", "600"))
                        .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Methods", "POST", "GET",
                                "DELETE", "OPTIONS", "HEAD")))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))


                // Session related configuration
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}