package com.galaxy.score.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author hfr
 * @Date 2021/3/2 17:50
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    //前端跨域
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 拦截所有的请求
                //设置允许跨域请求的域名
                .allowedOrigins("*")
                //是否允许证书 不再默认开启
                .allowCredentials(false)
                //设置允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                //跨域允许时间
                .maxAge(3600);
    }
}
