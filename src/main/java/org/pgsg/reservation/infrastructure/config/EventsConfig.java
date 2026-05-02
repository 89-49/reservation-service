package org.pgsg.reservation.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.pgsg.common.event.Events;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
public class EventsConfig {

    private final ApplicationEventPublisher eventPublisher;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Bean
    public Events events() {
        return new Events();
    }
}