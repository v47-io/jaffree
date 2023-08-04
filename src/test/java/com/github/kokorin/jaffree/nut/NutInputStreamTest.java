package com.github.kokorin.jaffree.nut;

import org.apache.commons.io.input.ClosedInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class NutInputStreamTest {
    private final NutInputStream closed = new NutInputStream(new ClosedInputStream());
    private final NutInputStream tooShortToReadValue = new NutInputStream(
            new ByteArrayInputStream(new byte[] {(byte) 0xFF})
    );
    private final NutInputStream tooShortToReadVarBytes = new NutInputStream(
            new ByteArrayInputStream(new byte[] {(byte) 0x4, (byte) 42})
    );

    @Test
    public void getPosition() {
        Assertions.assertEquals(0, closed.getPosition());
    }

    @Test
    public void readValueFromClosed() {
        assertThrows(EOFException.class, closed::readValue);
    }

    @Test
    public void readValueFromTooShort() {
        assertThrows(EOFException.class, tooShortToReadValue::readValue);
    }

    @Test
    public void readSignedValueFromClosed() {
        assertThrows(EOFException.class, closed::readSignedValue);
    }

    @Test
    public void readSignedValueFromTooShort() {
        assertThrows(EOFException.class, tooShortToReadValue::readSignedValue);
    }

    @Test
    public void readLongFromClosed() {
        assertThrows(EOFException.class, closed::readLong);
    }

    @Test
    public void readLongFromTooShort() {
        assertThrows(EOFException.class, tooShortToReadValue::readLong);
    }

    @Test
    public void readIntFromClosed() {
        assertThrows(EOFException.class, closed::readInt);
    }

    @Test
    public void readIntFromTooShort() {
        assertThrows(EOFException.class, tooShortToReadValue::readInt);
    }

    @Test
    public void readByteFromClosed() {
        assertThrows(EOFException.class, closed::readByte);
    }

    @Test
    public void readVariableStringFromClosed() {
        assertThrows(EOFException.class, closed::readVariableString);
    }

    @Test
    public void readVariableStringFromTooShort() {
        assertThrows(EOFException.class, tooShortToReadValue::readVariableString);
    }

    @Test
    public void readVariableStringFromTooShort2() {
        assertThrows(EOFException.class, tooShortToReadVarBytes::readVariableString);
    }

    @Test
    public void readCStringFromClosed() {
        assertThrows(EOFException.class, closed::readCString);
    }

    @Test
    public void readCStringFromTooShort() {
        assertThrows(EOFException.class, closed::readCString);
    }

    @Test
    public void readVariableBytesFromClosed() {
        assertThrows(EOFException.class, closed::readVariableBytes);
    }

    @Test
    public void readVariableBytesFromTooShort() {
        assertThrows(EOFException.class, tooShortToReadValue::readVariableBytes);
    }

    @Test
    public void readVariableBytesFromTooShort2() {
        assertThrows(EOFException.class, tooShortToReadVarBytes::readVariableBytes);
    }

    @Test
    public void readTimestampFromClosed() {
        assertThrows(EOFException.class, () -> closed.readTimestamp(4));
    }

    @Test
    public void readTimestampFromTooShort() {
        assertThrows(EOFException.class, () -> tooShortToReadValue.readTimestamp(42));
    }

    @Test
    public void checkNextByte() throws IOException {
        closed.checkNextByte();
    }

    @Test
    public void hasMoreData() throws IOException {
        Assertions.assertFalse(closed.hasMoreData());
        Assertions.assertTrue(tooShortToReadValue.hasMoreData());
        Assertions.assertTrue(tooShortToReadVarBytes.hasMoreData());
    }


    @Test
    public void readBytesFromClosed() {
        assertThrows(EOFException.class, () -> closed.readBytes(42));
    }


    @Test
    public void readBytesFromTooShort() {
        assertThrows(EOFException.class, () -> tooShortToReadValue.readBytes(42));
    }

    @Test
    public void readBytesFromTooShort2() {
        assertThrows(EOFException.class, () -> tooShortToReadVarBytes.readBytes(42));
    }

    @Test
    public void skipBytesFromClosed() {
        assertThrows(EOFException.class, () -> closed.skipBytes(42));
    }

    @Test
    public void skipBytesFromTooShort() {
        assertThrows(EOFException.class, () -> tooShortToReadValue.skipBytes(42));
    }

    @Test
    public void skipBytesFromTooShort2() {
        assertThrows(EOFException.class, () -> tooShortToReadVarBytes.skipBytes(42));
    }
}