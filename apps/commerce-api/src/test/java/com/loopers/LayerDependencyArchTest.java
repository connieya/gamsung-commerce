package com.loopers;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class LayerDependencyArchTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.loopers");
    }

    @Test
    @DisplayName("레이어 의존성: domain은 application, interfaces, infrastructure를 참조할 수 없다")
    void domain_layer_should_not_depend_on_other_layers() {
        Architectures.layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Domain").definedBy("com.loopers.domain..")
                .layer("Application").definedBy("com.loopers.application..")
                .layer("Infrastructure").definedBy("com.loopers.infrastructure..")
                .layer("Interfaces").definedBy("com.loopers.interfaces..")
                .layer("Support").definedBy("com.loopers.support..")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Interfaces")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Interfaces")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Interfaces").mayNotBeAccessedByAnyLayer()
                .whereLayer("Support").mayOnlyBeAccessedByLayers("Domain", "Application", "Infrastructure", "Interfaces")
                .check(classes);
    }

    @Test
    @DisplayName("application 레이어(Facade)는 Repository를 직접 의존할 수 없다 — Service를 통해 호출해야 한다")
    void application_layer_should_not_depend_on_repository() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.loopers.application..")
                .should().dependOnClassesThat()
                .haveSimpleNameEndingWith("Repository");

        rule.check(classes);
    }
}
