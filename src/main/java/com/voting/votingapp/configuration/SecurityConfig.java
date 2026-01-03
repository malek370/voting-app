package com.voting.votingapp.configuration;

import com.voting.votingapp.Services.JwtService;
import com.voting.votingapp.Services.UserDetailsImp;
import com.voting.votingapp.filter.JwtAuthentificationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsImp UserDetailsImp;
    private final JwtAuthentificationFilter jwtAuthentificationFilter;

    public SecurityConfig(UserDetailsImp userDetailsImp, JwtAuthentificationFilter jwtAuthentificationFilter) {
        UserDetailsImp = userDetailsImp;
        this.jwtAuthentificationFilter = jwtAuthentificationFilter;
    }


    @Bean
    public SecurityFilterChain SecFilterChain(HttpSecurity http, UserDetailsImp userDetailsImp) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                    req->req.requestMatchers("/api/user/**").permitAll()
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().permitAll()
                ).userDetailsService(userDetailsImp)
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthentificationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
    }
}
