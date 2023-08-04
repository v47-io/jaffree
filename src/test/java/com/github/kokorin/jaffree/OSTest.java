package com.github.kokorin.jaffree;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OSTest {
    @Test
    public void osDetected() {
        int count = 0;

        if (OS.IS_LINUX) {
            count++;
        }
        if (OS.IS_MAC) {
            count++;
        }
        if (OS.IS_WINDOWS) {
            count++;
        }

        Assertions.assertEquals(1, count, "Exactly one property is true: " + OS.OS_NAME);
    }
}