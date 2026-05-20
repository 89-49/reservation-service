package org.pgsg.reservation.infrastructure.config;

import org.pgsg.common.domain.OutboxRepository;
import org.pgsg.common.event.OutboxService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class OutboxConfig {

    @Bean
    @Primary
    public OutboxService outboxService(
            OutboxRepository outboxRepository,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        return new OutboxService(outboxRepository, kafkaTemplate);
    }
}