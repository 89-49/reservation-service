package org.pgsg.reservation.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.pgsg.config.security.CustomAccessDeniedHandler;
import org.pgsg.config.security.CustomAuthenticationEntryPoint;
import org.pgsg.config.security.LoginFilter;
import org.pgsg.config.security.SecurityConfigImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize 사용을 위해 필요
@RequiredArgsConstructor
@Import({SecurityConfigImpl.class})
public class ReservationSecurityConfig {

    private final LoginFilter loginFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 공용 API 및 모니터링 허용
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/actuator/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        // 예약 서비스의 핵심 API (Gateway에서 인증된 사용자만 접근)
                        .requestMatchers("/api/v1/reservations/**").authenticated()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // LoginFilter를 인증 필터 앞에 배치
                .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)

                // 커스텀 예외 처리
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint(authenticationEntryPoint);
                    c.accessDeniedHandler(accessDeniedHandler);
                });

        return http.build();
    }
}