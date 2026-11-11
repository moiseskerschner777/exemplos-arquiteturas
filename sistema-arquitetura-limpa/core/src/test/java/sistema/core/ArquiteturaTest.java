package sistema.core;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "sistema", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArquiteturaTest {

    @ArchTest
    static final ArchRule coreDependeApenasDeCoreEJava = classes()
            .that().resideInAPackage("sistema.core..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("sistema.core..", "java.lang..", "java.util..", "java.time..", "java.math..");

}