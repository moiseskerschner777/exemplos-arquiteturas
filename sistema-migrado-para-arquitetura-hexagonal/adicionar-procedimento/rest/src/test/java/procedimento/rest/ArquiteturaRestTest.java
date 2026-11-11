package procedimento.rest;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArquiteturaRestTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("procedimento");

    @Test
    void nucleoNaoDependeDosAdaptadores() {
        noClasses().that().resideInAPackage("procedimento.core..")
                .should().dependOnClassesThat().resideInAnyPackage("procedimento.rest..", "procedimento.adaptador..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    void adaptadorDePersistenciaNaoDependeDoRest() {
        noClasses().that().resideInAPackage("procedimento.adaptador..")
                .should().dependOnClassesThat().resideInAPackage("procedimento.rest..")
                .allowEmptyShould(true)
                .check(classes);
    }

}