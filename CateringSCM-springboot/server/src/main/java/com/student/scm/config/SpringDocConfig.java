package com.student.scm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@OpenAPIDefinition(
//        info = @Info(title = "项目API文档",version = "1.0",description = "SpringBoot项目接口文档"))
public class SpringDocConfig {
    @Bean
    public OpenAPI scmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("餐饮供应链管理系统 API 接口文档")
                        .description("基于 Spring Boot + Vue 的轻量级 ERP 项目")
                        .version("v1.0.0")
                )
                .components(new Components()
                        .addSecuritySchemes("token", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("token")
                        ))
                .addSecurityItem(new SecurityRequirement().addList("token"));
    }
}
