package com.github.games647.fastlogin.core.hooks;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultPasswordGeneratorTest {

    @Test
    public void testPasswordLength() {
        PasswordGenerator<String> generator = new DefaultPasswordGenerator<>();
        assertEquals(generator.getRandomPassword("A").length(), 8);
    }
}
