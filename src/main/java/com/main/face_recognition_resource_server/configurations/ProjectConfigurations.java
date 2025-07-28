package com.main.face_recognition_resource_server.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.face_recognition_resource_server.DTOS.attendance.AttendanceCacheDTO;
import com.main.face_recognition_resource_server.auth.JwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@Configuration
@EnableMethodSecurity()
@EnableAsync
@EnableScheduling
@EnableRetry
public class ProjectConfigurations {
    private final JwtAuthenticationConverter converter;
    @Value("${keySetURI}")
    private String keySetURI;

    @Value("${front-end-url}")
    private String FRONT_END_URL;

    public ProjectConfigurations(JwtAuthenticationConverter converter) {
        this.converter = converter;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public AuthenticationManager authenticationManager(JwtDecoder jwtDecoder) {
        JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        return new ProviderManager(jwtAuthenticationProvider);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(keySetURI).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwkSetUri(keySetURI).jwtAuthenticationConverter(converter)));
        http.cors(c -> {
            CorsConfigurationSource source = _ -> {
                CorsConfiguration cc = new CorsConfiguration();
                cc.setAllowCredentials(true);
                cc.setAllowedOrigins(List.of(FRONT_END_URL));
                cc.setAllowedHeaders(List.of("*"));
                cc.setAllowedMethods(List.of("*"));
                return cc;
            };
            c.configurationSource(source);
        });
        http.authorizeHttpRequests(c -> c.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
    public BlockingQueue<AttendanceCacheDTO> attendanceCacheQueue() {
        return new LinkedBlockingQueue<>();
    }
}
