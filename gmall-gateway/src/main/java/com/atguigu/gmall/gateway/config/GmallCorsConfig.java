package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GmallCorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter(){
        //cors 跨域配置对象
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");//允许所以的域名  要用到cookie的时候不能这样设置
        configuration.addAllowedOrigin("http://127.0.0.1");//允许的域名
        configuration.addAllowedOrigin("http://localhost");//允许的域名
        configuration.setAllowCredentials(true);//是否能用cookie
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");//允许任何的头信息
        //配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        ///** 过滤所有请求
        configurationSource.registerCorsConfiguration("/**",configuration);
        //返回cors过滤器对象
        return new CorsWebFilter(configurationSource);
    }
}
