package com.yeonieum.apigateway.config;

import com.yeonieum.apigateway.filter.JwtAuthorizationFilterFactory;
import com.yeonieum.apigateway.response.RoleMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Configuration
public class GatewayConfig {
    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private WebClient.Builder webClientBuilder;
//    @Bean
//    @RefreshScope
//    public RouteLocator productRouteLocator(RouteLocatorBuilder builder, JwtAuthorizationFilterFactory jwtAuthorizationFilterFactory) {
//        RouteLocatorBuilder.Builder routes = builder.routes();
//        try {
//            String url = discoveryClient.getInstances("productservice").stream().map(si -> si.getUri().toString()).findFirst().get();
//            fetchPermissions(url + "/productservice").forEach((path, role) -> {
//                System.out.println("Registering route: " + path + " with roles: " + role.getRoles());
//                routes.route(path, r -> r.path("/productservice" + path)
//                        .and()
//                        .method(role.getMethods())
//                        .filters(f -> f
//                                .dedupeResponseHeader("Access-Control-Allow-Origin","RETAIN_UNIQUE")
//                                .dedupeResponseHeader("Access-Control-Allow-Credentials","RETAIN_UNIQUE")
//                                .filter(jwtAuthorizationFilterFactory.apply(new JwtAuthorizationFilterFactory.Config(role.getRoles()))))
//                        .uri("lb://productservice"));
//            });
//        } catch (Exception e) {
//            // 일단 무시하고 라우터 구성
//            e.printStackTrace();
//        }
//
//
//        return routes.build();
//    }
//
//    @Bean
//    @RefreshScope
//    public RouteLocator memberRouteLocator(RouteLocatorBuilder builder, JwtAuthorizationFilterFactory jwtAuthorizationFilterFactory) {
//        RouteLocatorBuilder.Builder routes = builder.routes();
//        try {
//            String url = discoveryClient.getInstances("memberservice").stream().map(si -> si.getUri().toString()).findFirst().get();
//            fetchPermissions(url + "/memberservice").forEach((path, role) -> {
//                System.out.println("Registering route: " + path + " with roles: " + role.getRoles());
//                routes.route(path, r -> r.path("/memberservice" + path)
//                            .and()
//                            .method(role.getMethods())
//                            .filters(f -> f
//                                    .dedupeResponseHeader("Access-Control-Allow-Origin","RETAIN_UNIQUE")
//                                    .dedupeResponseHeader("Access-Control-Allow-Credentials","RETAIN_UNIQUE")
//                                    .filter(jwtAuthorizationFilterFactory.apply(new JwtAuthorizationFilterFactory.Config(role.getRoles())))
//                            )
//                            .uri("lb://memberservice"));
//            });
//        } catch (Exception e) {
//            // 일단 무시하고 라우터 구성
//            e.printStackTrace();
//        }
//
//
//        return routes.build();
//    }
//
//    @Bean
//    @RefreshScope
//    public RouteLocator orderRouteLocator(RouteLocatorBuilder builder, JwtAuthorizationFilterFactory jwtAuthorizationFilterFactory) {
//        RouteLocatorBuilder.Builder routes = builder.routes();
//        try {
//            String url = discoveryClient.getInstances("orderservice").stream().map(si -> si.getUri().toString()).findFirst().get();
//            fetchPermissions(url+"/orderservice").forEach((path, role) -> {
//                System.out.println("Registering route: " + path + " with roles: " + role.getRoles());
//
//                routes.route(path, r -> r.path("/orderservice" + path)
//                        //.and()
//                        //.method(role.getMethods())
//                        .filters(f -> f
//                                .dedupeResponseHeader("Access-Control-Allow-Origin","RETAIN_UNIQUE")
//                                .dedupeResponseHeader("Access-Control-Allow-Credentials","RETAIN_UNIQUE")
//                                .filter(jwtAuthorizationFilterFactory.apply(new JwtAuthorizationFilterFactory.Config(role.getRoles()))))
//                        .uri("lb://orderservice"));
//            });
//        } catch (Exception e) {
//            // 일단 무시하고 라우터 구성
//            e.printStackTrace();
//        }
//
//
//        return routes.build();
//    }

    @Bean
    @RefreshScope
    public RouteLocator productRouteLocator(RouteLocatorBuilder builder, JwtAuthorizationFilterFactory jwtAuthorizationFilterFactory) {
        return builder.routes()
                .route("memberservice_no_filter_route", r -> r.path(
                        "/memberservice/api/auth/**",
                                "/memberservice/api/permissions",
                                "/memberservice/api/permissions",
                                "/memberservice/access-token",
                                "/memberservice/api/auth/logout",
                                "/memberservice/api/member/join")
                        .uri("lb://memberservice"))
                .route("memberservice_route", r -> r.path("/memberservice/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE")
                                .filter(jwtAuthorizationFilterFactory.apply(new JwtAuthorizationFilterFactory.Config())))
                        .uri("lb://memberservice"))
                .route("orderservice_route", r -> r.path("/orderservice/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE")
                                .filter(jwtAuthorizationFilterFactory.apply(new JwtAuthorizationFilterFactory.Config())))
                        .uri("lb://orderservice"))
                .route("productservice_route", r -> r.path("/productservice/**")
                        .filters(f -> f
                                .dedupeResponseHeader("Access-Control-Allow-Origin", "RETAIN_UNIQUE")
                                .dedupeResponseHeader("Access-Control-Allow-Credentials", "RETAIN_UNIQUE")
                                .filter(jwtAuthorizationFilterFactory.apply(new JwtAuthorizationFilterFactory.Config())))
                        .uri("lb://productservice"))
                .build();
    }



    /**
     * 각 서비스에서 권한 정보를 가져오는 메소드
     * @param serviceUrl
     * @return
     */
    private Map<String, RoleMetadata> fetchPermissions(String serviceUrl) {
        WebClient webClient = webClientBuilder.build();
        Map<String, RoleMetadata> response = webClient.get()
                .uri(serviceUrl + "/api/permissions")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, RoleMetadata>>() {})
                .map(responseEntity -> responseEntity.getBody())
                .block();

        return response.entrySet()
                .stream()
                .sorted((entry1, entry2) -> Integer.compare(entry2.getKey().length(), entry1.getKey().length()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

}

