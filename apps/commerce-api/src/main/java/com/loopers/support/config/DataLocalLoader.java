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
 * local 프로필 기동 시 시드 대상 테이블을 비운 뒤 data-local.sql로 시드를 삽입한다.
 * ddl-auto 변경 없이, 재기동만으로 "데이터 전부 날리고 시드만 넣기"가 가능하다.
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
        runIfExists("data-local-clean.sql", "Seed tables cleaned.");
        runIfExists("data-local.sql", "Seed data loaded.");
    }

    private void runIfExists(String scriptName, String successMessage) {
        var resource = new ClassPathResource(scriptName);
        if (!resource.exists()) {
            log.debug("{} not found, skipping.", scriptName);
            return;
        }
        var populator = new ResourceDatabasePopulator();
        populator.addScript(resource);
        populator.execute(dataSource);
        log.info("Executed {}: {}", scriptName, successMessage);
    }
}
