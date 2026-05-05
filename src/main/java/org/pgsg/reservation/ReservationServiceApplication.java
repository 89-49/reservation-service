package org.pgsg.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = {"org.pgsg.reservation", "org.pgsg.common"})
@EntityScan(basePackages = {
        "org.pgsg.reservation",
        "org.pgsg.common.domain"
})
@EnableJpaRepositories(basePackages = {
        "org.pgsg.reservation",
        "org.pgsg.common.domain"
})
public class ReservationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}