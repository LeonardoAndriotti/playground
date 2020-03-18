package com.playgroundtest.archUnit;

import com.tngtech.archunit.PublicAPI;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

import static com.tngtech.archunit.PublicAPI.Usage.ACCESS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.playgroundtest", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchUnitTest {

    private static final DescribedPredicate PACKAGE_PREDICATE = new PackagePredicate();
    private static final ArchCondition NO_CONTROLLER_CLASS_CONDITION = new NoControllerClassCondition();


    @ArchTest
    private static final ArchRule LAYER_CHECKS = layeredArchitecture()
            .layer("API").definedBy("com.playgroundtest.api")
            .layer("APPLICATION").definedBy("com.playgroundtest.application")
            .layer("DAO").definedBy("com.playgroundtest.dao")

            .whereLayer("API").mayNotBeAccessedByAnyLayer()
            .whereLayer("APPLICATION").mayOnlyBeAccessedByLayers("API")
            .whereLayer("DAO").mayOnlyBeAccessedByLayers("APPLICATION", "API");

    @ArchTest
    private static final ArchRule NO_DEPRECATED_IN_USE_CASE_PACKAGE = noClasses().that()
            .areAnnotatedWith(Deprecated.class)
            .should()
            .resideInAPackage("com.playgroundtest..")
            .as("Deprecated annotation invalid,")
            .because("Deprecated classes are not allowed.");

    @ArchTest
    private static final ArchRule NO_CONTROLLER_IN_USE_CASE_PACKAGE = noClasses().that()
            .areAnnotatedWith(Controller.class)
            .or()
            .areAnnotatedWith(RestController.class)
            .should()
            .resideInAPackage("com.playgroundtest.dao")
            .andShould()
            .resideInAPackage("com.playgroundtest.application")
            .as("Controller annotations are not allowed")
            .because("REST calls are not allowed in packages [application, usecase]");

    @ArchTest
    private static final ArchRule NO_CYCLIC_DEPENDENCIES = slices()
            .matching("com.playgroundtest.(*)..")
            .should()
            .beFreeOfCycles();

    @ArchTest
    @PublicAPI(usage = ACCESS)
    public static final ArchRule NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS = noClasses()
            .should(THROW_GENERIC_EXCEPTIONS)
            .as("This class cannot throw general exceptions.");

    @ArchTest
    public static final ArchRule O_CLASS_ANNOTATED_CONTROLLER_IN_USE_CASE = classes().that(PACKAGE_PREDICATE).should(NO_CONTROLLER_CLASS_CONDITION);

    @ArchTest
    public static final ArchRule VALIDADE_TRANSACTION_READ_ONLY = methods()
            .that()
            .areAnnotatedWith(Transactional.class)
            .and()
            .haveFullNameMatching(".*(get|find|count).*")
            .should(new CheckTransactionalReadOnlyOnMethodCondition())
            .andShould()
            .beDeclaredInClassesThat()
            .resideInAPackage("com.playgroundtest.dao..");

    @ArchTest
    public static final ArchRule NO_CLASS_ANNOTATION_WITH_AUTOWIRED = fields()
            .should()
            .notBeAnnotatedWith(Autowired.class);
}
