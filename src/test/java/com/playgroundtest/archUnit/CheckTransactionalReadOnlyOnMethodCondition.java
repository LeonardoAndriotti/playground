package com.playgroundtest.archUnit;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.transaction.annotation.Transactional;


public class CheckTransactionalReadOnlyOnMethodCondition extends ArchCondition<JavaMethod> {

    public CheckTransactionalReadOnlyOnMethodCondition() {
        super("Check transactional readOnly on method condition:");
    }

    @Override
    public void check(JavaMethod javaMethod, ConditionEvents conditionEvents) {
        if (!javaMethod.getAnnotationOfType(Transactional.class).readOnly()) {
            conditionEvents.add(SimpleConditionEvent.violated(javaMethod,
                    javaMethod.getFullName() + "have no readonly on transactional annotation"));
        }
    }
}
