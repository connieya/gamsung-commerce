package com.loopers.support.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * local 프로필 기동 시 data-local.sql을 실행하여 시드 데이터를 삽입한다.
 * 커스텀 DataSource(datasource.mysql-jpa.main) 사용 시 Spring Boot 기본 DataSource 초기화가
 * 동작하지 않으므로, Hibernate 스키마 생성 이후 CommandLineRunner로 직접 실행한다.
 */
@Component
@Profile("local")
@Order(Integer.MAX_VALUE)
public class DataLocalLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLocalLoader.class);

    private final DataSource dataSource;

    public DataLocalLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        var resource = new ClassPathResource("data-local.sql");
        if (!resource.exists()) {
            log.debug("data-local.sql not found, skipping seed data.");
            return;
        }
        var populator = new ResourceDatabasePopulator();
        populator.addScript(resource);
        populator.execute(dataSource);
        log.info("Executed data-local.sql: seed data loaded.");
    }
}
