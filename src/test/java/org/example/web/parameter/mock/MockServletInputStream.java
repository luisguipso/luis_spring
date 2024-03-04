package org.example.web.parameter.mock;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MockServletInputStream extends ServletInputStream {
    private final InputStream stream;

    public MockServletInputStream(String input) {
        stream = new ByteArrayInputStream(input.getBytes());
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener listener) {
        // Implement if needed
    }
}
