package com.cmy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class spingConfig implements WebMvcConfigurer {
    //跨域请求
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //设置允许跨域请求的路径，/**是所有
                //当**Credentials为true时，**Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
                .allowedOrigins("*")
                //.allowCredentials(true) //是否允许证书 不再默认开启
                //设置允许的方法
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600)   .allowedHeaders("*"); }
}
