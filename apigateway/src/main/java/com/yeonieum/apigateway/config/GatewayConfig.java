package com.yeonieum.apigateway.config;

import com.yeonieum.apigateway.filter.JwtAuthorizationFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;


@Configuration
@EnableScheduling
public class GatewayConfig {

    @Autowired
    private WebClient.Builder webClientBuilder;


    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtAuthorizationFilterFactory jwtAuthorizationFilterFactory) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        try {
            fetchPermissions("lb://memberservice")
                    .forEach((path, role) -> routes.route("memberservice", r -> r.path("/memberservice/**")
                            .filters(f -> f.filter(jwtAuthorizationFilterFactory.apply(new JwtAuthorizationFilterFactory.Config(role))))
                            .uri("lb://memberservice")));

            fetchPermissions("lb://productservice")
                    .forEach((path, role) -> routes.route("productservice", r -> r.path("/productservice/**")
                            .filters(f -> f.filter(jwtAuthorizationFilterFactory.apply(new JwtAuthorizationFilterFactory.Config(role))))
                            .uri("lb://productservice")));

            fetchPermissions("lb://orderservice")
                    .forEach((path, role) -> routes.route("orderservice", r -> r.path("/orderservice/**")
                            .filters(f -> f.filter(jwtAuthorizationFilterFactory.apply(new JwtAuthorizationFilterFactory.Config(role))))
                            .uri("lb://orderservice")));
        } catch (Exception e) {
            // 일단 무시하고 라우터 구성
            e.printStackTrace();
        }


        return routes.build();
    }

    /**
     * 각 서비스에서 권한 정보를 가져오는 메소드
     * @param serviceUrl
     * @return
     */
    private Map<String, String> fetchPermissions(String serviceUrl) {
        return webClientBuilder.build()
                .get()
                .uri(serviceUrl + "/permissions")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

}

