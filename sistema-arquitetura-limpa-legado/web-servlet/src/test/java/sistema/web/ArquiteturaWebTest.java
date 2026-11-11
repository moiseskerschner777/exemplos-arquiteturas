package sistema.web;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "sistema", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArquiteturaWebTest {

    @ArchTest
    static final ArchRule servletsNaoConhecemCasosDeUso = noClasses()
            .that().haveSimpleNameEndingWith("Servlet")
            .should().dependOnClassesThat().resideInAPackage("sistema.core.casodeuso..");

    @ArchTest
    static final ArchRule servletsNaoConhecemAdaptadores = noClasses()
            .that().haveSimpleNameEndingWith("Servlet")
            .should().dependOnClassesThat().resideInAPackage("sistema.adaptador..");

}