package com.terra.framework.crust.wrapper;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ContentCachingResponseWrapper extends HttpServletResponseWrapper {

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_XML = "application/xml";
    private static final String APPLICATION_TEXT = "application/text";

    private ByteArrayOutputStream buffer = null;
    private ServletOutputStream out = null;
    private PrintWriter writer = null;

    private boolean cached = false;

    public ContentCachingResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        if (shouldCache()) {
            cached = true;
            buffer = new ByteArrayOutputStream();
            out = new ContentCachingOutputStream(buffer);
            writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (cached) {
            return out;
        } else {
            return super.getOutputStream();
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (cached) {
            return writer;
        } else {
            return super.getWriter();
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        if (cached) {
            if (out != null) {
                out.flush();
            }
            if (writer != null) {
                writer.flush();
            }
        } else {
            super.flushBuffer();
        }
    }

    @Override
    public void reset() {
        if (cached) {
            buffer.reset();
        } else {
            super.reset();
        }
    }

    public byte[] getContentAsByteArray() throws IOException {
        flushBuffer();
        if (cached) {
            return buffer.toByteArray();
        } else {
            return new byte[0];
        }
    }


    public byte[] getBufferContentAsByteArray() throws IOException {
        if (cached) {
            return buffer.toByteArray();
        } else {
            return new byte[0];
        }
    }

    private boolean shouldCache() {
        String contentType = getContentType();
        return contentType == null || isApplicationTextRequest(contentType) || isApplicationJsonRequest(contentType) || isApplicationXmlRequest(contentType);
    }

    private boolean isApplicationJsonRequest(String contentType) {
        return contentType.contains(APPLICATION_JSON);
    }

    private boolean isApplicationXmlRequest(String contentType) {
        return contentType.contains(APPLICATION_XML);
    }

    private boolean isApplicationTextRequest(String contentType) {
        return contentType.contains(APPLICATION_TEXT);
    }

    private class ContentCachingOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream bos = null;

        public ContentCachingOutputStream(ByteArrayOutputStream stream) throws IOException {
            bos = stream;
        }

        @Override
        public void write(int b) throws IOException {
            bos.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            throw new UnsupportedOperationException("not support");
        }
    }

}
