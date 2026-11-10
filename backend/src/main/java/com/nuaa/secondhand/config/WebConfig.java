package com.nuaa.secondhand.config;

import com.nuaa.secondhand.utils.LoginInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Web 配置：跨域 + 登录拦截器 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/code",
                        "/goods/list",
                        "/goods/detail/{id}",
                        "/goods/hot",
                        "/goods/nearby",
                        "/goods/search/hot",
                        "/uploads/**",
                        "/doc.html",
                        "/webjars/**",
                        "/v2/**",
                        "/error"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传图片静态访问（绝对路径，避免相对路径受 Tomcat work 目录影响）
        String uploadDir = System.getProperty("user.dir").replace("\\", "/") + "/uploads/";
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadDir);
    }
}
