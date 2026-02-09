package com.retail.rewards;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardsApplicationMainMethodTest {

    @Test
    void hasPublicStaticMainMethod() throws NoSuchMethodException {
        Method main = RewardsApplication.class.getMethod("main", String[].class);
        int mods = main.getModifiers();
        assertTrue(Modifier.isPublic(mods), "main method should be public");
        assertTrue(Modifier.isStatic(mods), "main method should be static");
    }
}
