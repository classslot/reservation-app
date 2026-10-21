package com.example.reservation.config;

import com.example.reservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 認証・認可の設定。セッション(Cookie)認証で、権限チェックはサーバー側で行う。
 * 学習用のためCSRFトークンは無効化し、SameSite Cookieで代替している（README参照）。
 */
@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /** 無効（退会）アカウントは disabled としてログイン不可にする */
    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return loginId -> users.findByLoginId(loginId)
                .map(u -> org.springframework.security.core.userdetails.User.withUsername(u.getLoginId())
                        .password(u.getPasswordHash())
                        .roles(u.getRole().name())
                        .disabled(!u.isActive())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository repository) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .securityContext(c -> c.securityContextRepository(repository))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        // スタッフ専用
                        .requestMatchers("/api/students", "/api/students/**").hasRole("STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/slots").hasRole("STAFF")
                        .requestMatchers(HttpMethod.PATCH, "/api/slots/*").hasRole("STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/reservations").hasRole("STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/reservations/*/cancel").hasRole("STAFF")
                        // 会員専用
                        .requestMatchers("/api/me/reservations", "/api/me/reservations/**").hasRole("MEMBER")
                        .requestMatchers(HttpMethod.POST, "/api/reservations").hasRole("MEMBER")
                        // それ以外はログイン済みであること
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> writeError(res, 401, "ログインが必要です"))
                        .accessDeniedHandler((req, res, ex) -> writeError(res, 403, "この操作を行う権限がありません")));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String[] origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(origins));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private static void writeError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
