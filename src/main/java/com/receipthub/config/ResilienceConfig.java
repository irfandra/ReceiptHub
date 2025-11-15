package com.receipthub.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Configuration
public class ResilienceConfig {
    
    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .slidingWindowSize(10)
            .minimumNumberOfCalls(3)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .build();
        
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        
        registry.circuitBreaker("ocrService").getEventPublisher()
            .onStateTransition(event -> 
                log.warn("Circuit Breaker: {} → {}", 
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState())
            );
        
        return registry;
    }
}
