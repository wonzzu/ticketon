package com.ticketing.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .info(info())
                .components(new Components().addSecuritySchemes(SCHEME_NAME, bearerAuth))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
    }

    private Info info() {
        return new Info()
                .title("Ticketon API")
                .description("""
                        콘서트·공연 예매 서비스 API

                        - 모든 응답은 BaseResponse 로 감싸집니다. `{ success, code, message, data }`
                        - 응답 코드(code)는 BaseResponseStatus enum 에 정의되어 있습니다.
                        - 인증이 필요한 API 는 로그인 후 우측 상단 Authorize 에 accessToken 을 입력하세요.
                        """)
                .version("v1");
    }
}
