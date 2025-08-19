plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-redis")

    // Lombok
    testFixturesCompileOnly("org.projectlombok:lombok")
    testFixturesAnnotationProcessor("org.projectlombok:lombok")

    testFixturesImplementation("com.redis:testcontainers-redis")
}
