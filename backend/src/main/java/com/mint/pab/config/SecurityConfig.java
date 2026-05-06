package com.mint.pab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF(前后端分离不需要)
            .csrf().disable()
            // 禁用表单登录(使用 JWT)
            .formLogin().disable()
            // 禁用 HTTP Basic 认证
            .httpBasic().disable()
            // 禁用默认登出
            .logout().disable()
            // 禁用 session(使用 JWT 无状态认证)
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置请求授权
            .authorizeRequests()
                // 允许所有请求(认证由 AuthInterceptor 处理)
                .anyRequest().permitAll();
        return http.build();
    }

}
