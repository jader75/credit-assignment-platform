package br.com.srm.credit.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private static final String BASE_PACKAGE = "br.com.srm.credit";

    @Test
    void domainShouldNotDependOnInfrastructure() {
        var importedClasses = new ClassFileImporter().importPackages(BASE_PACKAGE);

        var rule = noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..infrastructure..");

        rule.check(importedClasses);
    }
}