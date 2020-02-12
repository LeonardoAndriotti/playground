package br.com.playground.archunit;

import com.tngtech.archunit.PublicAPI;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.PublicAPI.Usage.ACCESS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "br.com.playground")
//para especificar quais pacotes ler para este teste ( @AnalyzeClasses (packages = {..}) )
public class ArchUnitTest {


    /*
     *  Verificações de camada
     *
     *  Validar quais pacotes podem ser chamados e por qual pacote pode chamar.
     * */

    @ArchTest
    private static final ArchRule LAYER_CHECKS = layeredArchitecture()
            .layer("API").definedBy("br.com.playground.api")
            .layer("APPLICATION").definedBy("br.com.playground.application")
            .layer("USECASE").definedBy("br.com.playground.usecase")

            .whereLayer("API").mayNotBeAccessedByAnyLayer()
            .whereLayer("APPLICATION").mayOnlyBeAccessedByLayers("API")
            .whereLayer("USECASE").mayOnlyBeAccessedByLayers("APPLICATION");


    /*
     * Proibindo anotaçãoes em pacotes
     * */
    @ArchTest
    private static final ArchRule NO_DEPRECATED_IN_USE_CASE_PACKAGE = noClasses().that()
            .areAnnotatedWith(Deprecated.class)
            .should()
            .resideInAPackage("br.com.playground..")
            .as("Deprecated annotation invalid,")
            .because("Deprecated classes are not allowed.");

    @ArchTest
    private static final ArchRule NO_CONTROLLER_IN_USE_CASE_PACKAGE = noClasses().that()
            .areAnnotatedWith(Controller.class)
            .or()
            .areAnnotatedWith(RestController.class)
            .should()
            .resideInAPackage("br.com.playground.usecase")
            .andShould()
            .resideInAPackage("br.com.playground.application")
            .as("Controller annotations are not allowed")
            .because("REST calls are not allowed in packages [application, usecase]");


    /*
     * Verificações de ciclo
     * */
    @ArchTest
    private static final ArchRule NO_CYCLIC_DEPENDENCIES = slices()
            .matching("com.playground.(*)..")
            .should()
            .beFreeOfCycles();


    /*
     * Verificar exceptions genéricas
     *
     * Throwable, Exception, RuntimeException
     * */

    @ArchTest
    @PublicAPI(usage = ACCESS)
    public static final ArchRule NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS = noClasses()
            .should(THROW_GENERIC_EXCEPTIONS)
            .as("Está classe não pode lançar exceções genericas.");


    /*
     *
     * Validações personalizadas
     * */

    private static final DescribedPredicate PACKAGE_PREDICATE = new PackagePredicate();
    private static final ArchCondition NO_CONTROLLER_CLASS_CONDITION = new NoControllerClassCondition();


    @ArchTest
    public static final ArchRule O_CLASS_ANNOTATED_CONTROLLER_IN_USE_CASE = classes().that(PACKAGE_PREDICATE).should(NO_CONTROLLER_CLASS_CONDITION);

}

/*
 * Responsavel pela regra do teste, condição para passar.
 * */
class NoControllerClassCondition extends ArchCondition {

    public NoControllerClassCondition() {
        super("not contain a method named foo");
    }


    @Override
    public void check(Object item, ConditionEvents events) {
        ((JavaClass) item).getAnnotations()
                .stream()
                .map(JavaAnnotation::getRawType)
                .filter(c -> c.getSimpleName().equals("Controller"))
                .forEach(c -> events
                        .add(SimpleConditionEvent
                                .violated(c, "class " + ((JavaClass) item).getSimpleName() + " contains a annotation controller")));
    }
}

/*
 *
 * Seleciona as classes que vão ser validadas.
 * */
class PackagePredicate extends DescribedPredicate {

    private static final String PACKAGE = "br.com.playground";

    public PackagePredicate() {
        super("resides in package " + PACKAGE);
    }

    @Override
    public boolean apply(Object javaClass) {
        return ((JavaClass) javaClass).getPackage().getRelativeName().startsWith("use");
    }
}
