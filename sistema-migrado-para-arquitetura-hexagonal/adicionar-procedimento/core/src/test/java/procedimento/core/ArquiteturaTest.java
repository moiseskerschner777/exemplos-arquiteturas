package procedimento.core;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class ArquiteturaTest {

    private final JavaClasses nucleo = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("procedimento.core");

    @Test
    void nucleoDependeApenasDeleMesmoEDaBibliotecaPadrao() {
        classes().that().resideInAPackage("procedimento.core..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("procedimento.core..", "java.lang..", "java.util..", "java.time..", "java.math..")
                .allowEmptyShould(true)
                .check(nucleo);
    }

}