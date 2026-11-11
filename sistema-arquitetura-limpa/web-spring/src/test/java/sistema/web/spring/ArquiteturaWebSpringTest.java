package sistema.web.spring;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArquiteturaWebSpringTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("sistema.web.spring");

    @Test
    void controladoresDependemSoDasPortasDeEntrada() {
        noClasses().that().haveSimpleNameEndingWith("Controller")
                .should().dependOnClassesThat().resideInAnyPackage("sistema.core.casodeuso..", "sistema.adaptador..",
                        "sistema.core.porta.out..")
                .check(classes);
    }

}