package com.github.kokorin.jaffree.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class HttpServerTest {
    @Test
    public void autoPortNumber() throws IOException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);
        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            server.bind(address);
            address = (InetSocketAddress) server.getLocalAddress();
            Assertions.assertNotEquals(0, address.getPort());
        }
    }
}