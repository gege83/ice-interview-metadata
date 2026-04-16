package com.ice.metadata

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers(
                        "/", // home
                        "/public/**",
                        "/actuator/**",
                    ).permitAll()
                    .anyRequest().authenticated()                         // All others require auth
            }
            .httpBasic(Customizer.withDefaults())

        return http.build()
    }

    // creating test user
    @Bean
    fun userDetailsService(): UserDetailsService {
        val encoder = passwordEncoder()
        val user = User.withUsername("user")
            .password(encoder.encode("password"))
            .roles("USER")
            .build()

        val artist1 = User.withUsername("artist1")
            .password(encoder.encode("password"))
            .roles("ARTIST")
            .build()
        val artist2 = User.withUsername("artist2")
            .password(encoder.encode("password"))
            .roles("ARTIST")
            .build()

        return InMemoryUserDetailsManager(user, artist1, artist2)
    }

}