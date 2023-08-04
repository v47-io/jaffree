package com.github.kokorin.jaffree.nut;

import org.apache.commons.io.output.ClosedOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class NutOutputStreamTest {
    private NutOutputStream closed = new NutOutputStream(new ClosedOutputStream());

    @Test
    public void writeValue() {
        assertThrows(IOException.class, () -> {
            closed.writeValue(42);
            closed.flush();
        });
    }

    @Test
    public void writeSignedValue() {
        assertThrows(IOException.class, () -> {
            closed.writeSignedValue(42);
            closed.flush();
        });
    }

    @Test
    public void writeLong() {
        assertThrows(IOException.class, () -> {
            closed.writeLong(42);
            closed.flush();
        });
    }

    @Test
    public void writeInt() {
        assertThrows(IOException.class, () -> {
            closed.writeInt(42);
            closed.flush();
        });
    }

    @Test
    public void writeByte() {
        assertThrows(IOException.class, () -> {
            closed.writeByte(42);
            closed.flush();
        });
    }

    @Test
    public void writeVariablesString() {
        assertThrows(IOException.class, () -> {
            closed.writeVariablesString("42");
            closed.flush();
        });
    }

    @Test
    public void writeVariableBytes() {
        assertThrows(IOException.class, () -> {
            closed.writeVariableBytes(new byte[] {42});
            closed.flush();
        });
    }

    @Test
    public void writeTimestamp() {
        assertThrows(IOException.class, () -> {
            closed.writeTimestamp(42, new Timestamp(4, 2));
            closed.flush();
        });
    }

    @Test
    public void writeCString() {
        assertThrows(IOException.class, () -> {
            closed.writeCString("42");
            closed.flush();
        });
    }

    @Test
    public void writeBytes() {
        assertThrows(IOException.class, () -> {
            closed.writeBytes(new byte[] {42});
            closed.flush();
        });
    }

    @Test
    public void writeCrc32() {
        assertThrows(IOException.class, () -> {
            closed.writeCrc32();
            closed.flush();
        });
    }

    @Test
    public void getPosition() {
        Assertions.assertEquals(0, closed.getPosition());
    }

    @Test
    public void flush() throws Exception {
        assertThrows(IOException.class, () -> closed.flush());
    }
}