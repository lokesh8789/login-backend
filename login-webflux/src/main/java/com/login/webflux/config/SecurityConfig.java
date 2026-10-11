package com.login.webflux.config;

import com.login.webflux.security.*;
import com.login.webflux.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ServerAuthenticationEntryPoint authenticationEntryPoint;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository;

    // --> Security Context Repository Way (header -> authenticated)
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http,
                                              JwtSecurityContextRepository securityContextRepository) {

        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/login")
                        .permitAll()
                        .anyExchange()
                        .authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .securityContextRepository(securityContextRepository)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository)
                        .authenticationSuccessHandler(oAuth2AuthenticationSuccessHandler)
                        .authenticationFailureHandler(new RedirectServerAuthenticationFailureHandler(Constants.FRONTEND_OAUTH_FAILURE_URL)))
                .build();
    }

    //--> AuthenticationConverter(header->unAuth) + AuthenticationManager(unAuth->auth) way to handle Jwt Authentication
    //--> To Use this, un-comment JwtAuthenticationConverter and JwtAuthenticationManager's @Component part.
    /*@Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http,
                                              ReactiveAuthenticationManager authenticationManager,
                                              ServerAuthenticationConverter serverAuthenticationConverter) {

        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(serverAuthenticationConverter);

        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/login")
                        .permitAll()
                        .anyExchange()
                        .authenticated()
                )
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authenticationManager(authenticationManager)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .build();
    }*/


    //--> Custom JwtAuthenticationFilter Way To Handle Jwt Authentication
    /*@Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http, JwtTokenUtil jwtTokenUtil, ReactiveUserDetailsService userDetailsService) {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService);

        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/login")
                        .permitAll()
                        .anyExchange()
                        .authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .build();
    }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
