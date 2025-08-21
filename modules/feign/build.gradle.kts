plugins {
    `java-library`
    `java-test-fixtures`
}


dependencies {
    // feign
    api("org.springframework.cloud:spring-cloud-starter-openfeign")
    api("io.github.resilience4j:resilience4j-spring-boot3")


    // test-fixtures
    testFixturesImplementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    testFixturesImplementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
}
