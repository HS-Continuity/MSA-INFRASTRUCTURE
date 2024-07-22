package com.yeonieum.apigateway.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api")
public class SpringCloudGatewayController {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    // 동적 라우팅 정보 갱신
    @PostMapping("/refresh/routes")
    public Mono<Void> refresh() {
        this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        return Mono.empty();
    }
}
