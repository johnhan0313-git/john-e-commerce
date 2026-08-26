package com.john.ecommerce.architecture;

import com.john.ecommerce.module.trade.entity.Order;
import com.john.ecommerce.module.trade.mapper.OrderMapper;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.john.ecommerce");
    }

    @Test
    void onlyTradeMaySetOrderStatus() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackages("..module.trade..")
                .should().callMethod(Order.class, "setStatus", Integer.class)
                .because("Order.status is owned by trade; other BCs must use OrderLifecyclePort");
        rule.check(classes);
    }

    @Test
    void controllersMustNotDependOnMappers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..mapper..")
                .because("Controllers map to Application/Service only");
        rule.check(classes);
    }

    @Test
    void paymentMustNotMutateOrdersViaOrderMapper() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..module.payment..")
                .should().callMethod(OrderMapper.class, "updateById", Object.class)
                .because("payment must not update orders via OrderMapper");
        rule.check(classes);
    }
}
