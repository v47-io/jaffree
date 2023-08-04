package com.github.kokorin.jaffree.ffmpeg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilterTest {
    @Test
    public void testGetValue() throws Exception {
        String expected = "[0:1][0:2]amerge";
        String actual = new GenericFilter()
                .addInputLink("0:1")
                .addInputLink("0:2")
                .setName("amerge")
                .getValue();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testGetValue2() throws Exception {
        // The following graph description will generate a red source with an opacity of 0.2,
        // with size "qcif" and a frame rate of 10 frames per second.
        String expected = "color=c=red@0.2:s=qcif:r=10";
        String actual = new GenericFilter()
                .setName("color")
                .addArgument("c", "red@0.2")
                .addArgument("s", "qcif")
                .addArgument("r", "10")
                .getValue();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testEscape() throws Exception {
        Assertions.assertEquals("\\\\", GenericFilter.escape("\\"));
        Assertions.assertEquals("\\\\\\'", GenericFilter.escape("'"));
        Assertions.assertEquals("\\\\:", GenericFilter.escape(":"));

        String text = "this is a 'string': may contain one, or more, special characters";
        String expected =
                "this is a \\\\\\'string\\\\\\'\\\\: may contain one\\, or more\\, special characters";
        Assertions.assertEquals(expected, GenericFilter.escape(text));
    }

}