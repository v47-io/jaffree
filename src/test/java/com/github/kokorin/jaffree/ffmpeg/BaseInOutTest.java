package com.github.kokorin.jaffree.ffmpeg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaseInOutTest {
    @Test
    public void testFormatDuration() {
        Assertions.assertEquals("123.456", BaseInOut.formatDuration(123_456));
        Assertions.assertEquals("123.056", BaseInOut.formatDuration(123_056));
        Assertions.assertEquals("123.050", BaseInOut.formatDuration(123_050));
        Assertions.assertEquals("123.000", BaseInOut.formatDuration(123_000));

        Assertions.assertEquals("-123.456", BaseInOut.formatDuration(-123_456));
        Assertions.assertEquals("-123.056", BaseInOut.formatDuration(-123_056));
        Assertions.assertEquals("-123.050", BaseInOut.formatDuration(-123_050));
        Assertions.assertEquals("-123.000", BaseInOut.formatDuration(-123_000));

        Assertions.assertEquals("1000.000", BaseInOut.formatDuration(1_000_000));
    }

}