package com.terra.framework.crust.wrapper;

import com.terra.framework.crust.web.WebUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ContentCachingRequestWrapper extends HttpServletRequestWrapper {

    private byte[] payload = new byte[0];

    @Nullable
    private ServletInputStream inputStream;

    @Nullable
    private BufferedReader reader;

    private boolean cached = false;

    public ContentCachingRequestWrapper(HttpServletRequest request)
            throws IOException {
        super(request);
        if (WebUtil.isRawRequest(request)) {
            cached = true;
            payload = StreamUtils.copyToByteArray(request.getInputStream());
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (cached) {
            return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
        } else {
            return super.getReader();
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cached) {
            if (this.inputStream == null) {
                this.inputStream = new ContentCachingInputStream(payload);
            }
            return this.inputStream;
        } else {
            return super.getInputStream();
        }
    }

    public byte[] getContentAsByteArray() {
        return payload;
    }

    private class ContentCachingInputStream extends ServletInputStream {
        private ByteArrayInputStream bis;

        public ContentCachingInputStream(byte[] payload) throws IOException {
            bis = new ByteArrayInputStream(payload);
        }

        @Override
        public int read() throws IOException {
            return bis.read();
        }

        @Override
        public boolean isFinished() {
            return bis.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("not support");
        }
    }

}
