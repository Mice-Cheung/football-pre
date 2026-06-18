package com.football.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI footballOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("足球预测数据平台 API")
                        .description("提供比赛信息、阵容、赔率、凯利指数、技战术等数据接口")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Football Prediction Team")
                                .email("dev@football-prediction.com")));
    }
}
