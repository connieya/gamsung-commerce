plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // Kafka
    api("org.springframework.kafka:spring-kafka")

    testFixturesCompileOnly("org.projectlombok:lombok")
    testFixturesAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:kafka")

    testFixturesImplementation("org.testcontainers:kafka")
}
