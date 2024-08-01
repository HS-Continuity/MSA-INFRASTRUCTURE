package com.yeonieum.apigateway.filter;

import com.yeonieum.apigateway.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthorizationFilterFactory extends AbstractGatewayFilterFactory<JwtAuthorizationFilterFactory.Config> {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private Environment env;

    public JwtAuthorizationFilterFactory(Environment env) {
        super(Config.class);
        this.env = env;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 토큰 추출
             ServerHttpRequest request = exchange.getRequest();
        //     if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
        //         return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
        //     }
        //     String authorization = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
        //     String jwt = authorization.replace("Bearer", "");

        //     // 토큰 유효성 검증
        //     if (!jwtUtils.validateToken(jwt)) {
        //         if(jwtUtils.isTokenExpired(jwt)) {
        //             return handleTokenRefresh(exchange, chain, config);
        //         } else {
        //             return onError(exchange, "jwt is not valid", HttpStatus.UNAUTHORIZED);
        //         }
        //     }

        //     // 인가
        //     if(jwtUtils.getRole(jwt).equals(config.getRole())) {
        //         return chain.filter(exchange);
        //     } else {
        //         return onError(exchange, "Role is not valid", HttpStatus.FORBIDDEN);
        //     }
        // };
            return chain.filter(exchange);
    }

    private Mono<Void> handleTokenRefresh(ServerWebExchange exchange, GatewayFilterChain chain, Config config) {
        // 요청에서 쿠키에서 리프레시 토큰 추출
        String refreshToken = exchange.getRequest().getCookies().getFirst("refreshToken").getValue();

        // 리프레시 토큰을 추출, 새로운 액세스 토큰 발급 요청
        return webClientBuilder.build()
                .post()
                .uri("lb://memberservice/refresh-token")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue("{\"refreshToken\":\"" + refreshToken + "\"}")
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(newAccessToken -> {
                    // 새로운 액세스 토큰으로 필터체인에 재요청
                    ServerHttpRequest newRequest = exchange.getRequest().mutate()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                            .build();
                    return chain.filter(exchange.mutate().request(newRequest).build());
                })
                .onErrorResume(e -> onError(exchange, "토큰 발급 불가", HttpStatus.UNAUTHORIZED));
    }


    public static class Config {
        private String role;
        public Config(String role) {
            this.role = role;
        }
        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        return response.setComplete();
    }
}
