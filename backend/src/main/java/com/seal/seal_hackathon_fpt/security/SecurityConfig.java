package com.seal.seal_hackathon_fpt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 0. Cấu hình CORS để Frontend có thể gọi API
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 1. Tắt CSRF vì dùng JWT
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Nếu chưa đăng nhập thì trả 401 thay vì HTML
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // 3. Không dùng session, mỗi request tự mang JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Phân quyền API
                .authorizeHttpRequests(auth -> auth
                        // Cho phép preflight request từ FE
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // API public cho Guest Dashboard
                        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()

                        // API auth công khai
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/forgot-password"
                        ).permitAll()

                        // Swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Các API còn lại phải có token
                        .anyRequest().authenticated()
                )

                // 5. Provider + JWT Filter
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =========================================
    // HÀM ĐỊNH NGHĨA QUY TẮC CORS CHO TRÌNH DUYỆT
    // =========================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép mọi nguồn (domain frontend) gọi vào backend (Dùng "*" để dễ test ở localhost)
        configuration.setAllowedOrigins(List.of("*"));

        // Cho phép các phương thức HTTP
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Cho phép mọi loại header (cực kỳ quan trọng để Frontend gửi được Header Authorization chứa Token)
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}