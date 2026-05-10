package com.movie.config;

import com.movie.util.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    private final String ADMIN_AUTH="ADMIN";
    private final String USER_AUTH="USER";

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    PasswordEncoder getMyPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity httpSecurity,MyAuthorityProvider myAuthorityProvider) throws Exception{
            httpSecurity.csrf(csrf->csrf.disable())
                    .authenticationProvider(myAuthorityProvider)
                    .authorizeHttpRequests(auth->
                            // FIRST: public endpoints
                        auth.requestMatchers("/auth/**").permitAll()
                            .requestMatchers("/user/add", "/user/login").permitAll()
                            // ROLE BASED
                            .requestMatchers("/admin/**").hasRole("ADMIN")
                            .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                            .requestMatchers("/**").permitAll()
                            .anyRequest().authenticated()
                );
        // Jwt security
        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            return httpSecurity.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }
}
