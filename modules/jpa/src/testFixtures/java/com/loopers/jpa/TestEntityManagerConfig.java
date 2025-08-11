package com.loopers.jpa;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestEntityManagerConfig {
    @Bean
    public TestEntityManager testEntityManager(EntityManagerFactory factory) {
        return new TestEntityManager(factory);
    }
}
