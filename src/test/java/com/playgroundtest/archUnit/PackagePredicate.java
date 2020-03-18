package com.playgroundtest.archUnit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

public class PackagePredicate extends DescribedPredicate {

    private static final String PACKAGE = "com.playgroundtest";

    public PackagePredicate() {
        super("resides in package " + PACKAGE);
    }

    @Override
    public boolean apply(Object javaClass) {
        return ((JavaClass) javaClass).getPackage().getRelativeName().startsWith("use");
    }
}
