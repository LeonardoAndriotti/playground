package com.playgroundtest.archUnit;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public class NoControllerClassCondition extends ArchCondition {

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
