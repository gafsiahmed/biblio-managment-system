package com.bibliotheque.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationSuccessHandler successHandler, AccessDeniedHandler accessDeniedHandler) throws Exception {
    http
      .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/verify-email", "/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll()
        .requestMatchers("/login", "/register").anonymous()
        .requestMatchers("/loans/pending", "/loans/overdue").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.POST, "/loans/*/approve", "/loans/*/return").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers("/loans/**").authenticated()
        .requestMatchers(HttpMethod.POST, "/resources/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.PUT, "/resources/**").hasAnyRole("LIBRARIAN", "ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/resources/**").hasRole("ADMIN")
        .requestMatchers("/users/**").hasRole("ADMIN")
        .requestMatchers("/user/**").authenticated()
        .requestMatchers("/librarian/**").hasRole("LIBRARIAN")
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
      )
      .formLogin(form -> form
        .loginPage("/login")
        .loginProcessingUrl("/login")
        .defaultSuccessUrl("/", true)
        .successHandler(successHandler)
        .failureUrl("/login?error")
        .permitAll()
      )
      .logout(logout -> logout
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
        .logoutSuccessUrl("/")
        .deleteCookies("JSESSIONID")
        .invalidateHttpSession(true)
        .permitAll()
      )
      .exceptionHandling(ex -> ex
        .accessDeniedHandler(accessDeniedHandler)
      )
      .sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        .sessionFixation().migrateSession()
      );
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationSuccessHandler authenticationSuccessHandler() {
    return (request, response, authentication) -> {
      String target = roleRedirect(authentication);
      redirect(response, target);
    };
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> redirect(response, "/access-denied");
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  private String roleRedirect(Authentication authentication) {
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    for (GrantedAuthority a : authorities) {
      String r = a.getAuthority();
      if ("ROLE_ADMIN".equals(r)) return "/admin";
      if ("ROLE_LIBRARIAN".equals(r)) return "/librarian";
      if ("ROLE_USER".equals(r)) return "/user";
    }
    return "/";
  }

  private void redirect(jakarta.servlet.http.HttpServletResponse response, String target) throws IOException {
    response.sendRedirect(target);
  }
}
