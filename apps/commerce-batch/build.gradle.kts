dependencies {
    // add-ons
    implementation(project(":modules:jpa"))
    implementation(project(":supports:jackson"))
    implementation(project(":supports:logging"))

    implementation("org.springframework.boot:spring-boot-starter-web")

    // batch
    implementation("org.springframework.boot:spring-boot-starter-batch")


    // test-fixtures
    testImplementation(testFixtures(project(":modules:jpa")))
}
