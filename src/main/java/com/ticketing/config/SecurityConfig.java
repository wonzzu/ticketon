package com.ticketing.config;

import com.ticketing.auth.jwt.JwtAccessDeniedHandler;
import com.ticketing.auth.jwt.JwtAuthenticationEntryPoint;
import com.ticketing.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private List<String> allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .authorizeHttpRequests(auth -> auth
                        // ── 공개 (로그인 불필요) ──
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui", "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/members/signup", "/sellers/signup").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/events", "/events/**",       // 공연 목록·상세·회차·리뷰 조회
                                "/venues", "/venues/**",       // 공연장 조회
                                "/schedules/**").permitAll()   // 좌석 조회

                        // ── 어드민 전용 ──
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/coupons").hasRole("ADMIN")            // 쿠폰 생성(발급 issue는 회원)
                        .requestMatchers(HttpMethod.POST, "/venues").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/venues/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/venues/**").hasRole("ADMIN")

                        // ── 셀러 전용 ──
                        .requestMatchers(HttpMethod.POST, "/events").hasRole("SELLER")          // 공연 등록
                        .requestMatchers(HttpMethod.PATCH, "/events/**").hasRole("SELLER")       // 공연 수정
                        .requestMatchers(HttpMethod.DELETE, "/events/**").hasRole("SELLER")       // 공연 삭제
                        .requestMatchers(HttpMethod.POST, "/events/*/schedules").hasRole("SELLER")  // 회차 등록
                        .requestMatchers(HttpMethod.POST, "/uploads/poster").hasRole("SELLER")    // 포스터 업로드
                        .requestMatchers("/sellers/me/**").hasRole("SELLER")                     // 셀러 대시보드

                        // ── 그 외 전부 로그인 필요 (예매·결제·대기열·마이페이지·쿠폰발급·리뷰작성) ──
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
