package com.example.checkers.domain;

import java.util.function.Predicate;

public interface SideChecker {

    static boolean isSide(Side t, Predicate<Side> condition) {
        return condition.test(t);
    }
}
