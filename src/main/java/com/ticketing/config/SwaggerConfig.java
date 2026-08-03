package com.ticketing.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    // 배포는 Nginx가 /api prefix를 벗겨 전달하므로 문서의 서버 주소에 /api를 붙여야 한다.
    // 비어 있으면(로컬) springdoc이 요청 기준으로 자동 설정한다.
    @Value("${springdoc.server-url:}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        OpenAPI openAPI = new OpenAPI()
                .info(info())
                .components(new Components().addSecuritySchemes(SCHEME_NAME, bearerAuth))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));

        if (!serverUrl.isBlank()) {
            openAPI.servers(List.of(new Server().url(serverUrl)));
        }

        return openAPI;
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
