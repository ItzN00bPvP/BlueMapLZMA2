package de.bluecolored.bluemap.lzma2.nativexz;

import java.io.IOException;
import java.io.InputStream;

public class NativeXZInputStream extends InputStream {

    private final InputStream in;
    private final NativeDecoderPool pool;
    private final long handle;
    private final byte[] inBuffer = new byte[64 * 1024];
    private int inPos = 0;
    private int inLen = 0;
    private final byte[] outBuffer = new byte[64 * 1024];
    private int outPos = 0;
    private int outLen = 0;
    private boolean eof = false;
    private boolean closed = false;

    public NativeXZInputStream(InputStream in, NativeDecoderPool pool) throws IOException {
        this.in = in;
        this.pool = pool;
        this.handle = pool != null ? pool.claim() : NativeLZMA2.createDecoder();
        if (this.handle == 0) {
            throw new IOException("Failed to create native LZMA2 decoder");
        }
    }

    @Override
    public int read() throws IOException {
        if (closed) throw new IOException("Stream closed");
        if (eof && outPos >= outLen) return -1;

        if (outPos >= outLen) {
            if (fillBuffer() == -1) return -1;
        }

        return outBuffer[outPos++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (closed) throw new IOException("Stream closed");
        if (eof && outPos >= outLen) return -1;

        if (outPos < outLen) {
            int n = Math.min(len, outLen - outPos);
            System.arraycopy(outBuffer, outPos, b, off, n);
            outPos += n;
            return n;
        }

        // internal buffer is empty, if len is large enough, decode directly into b
        if (len >= 4096) {
            if (inPos >= inLen) {
                inLen = in.read(inBuffer);
                inPos = 0;
                if (inLen == -1) {
                    inLen = 0;
                }
            }

            long result = NativeLZMA2.process(handle, inBuffer, inPos, inLen - inPos, b, off, len, inLen == 0 ? 3 : 0);
            int status = (int) (result >> 56) & 0xFF;
            int consumed = (int) (result >> 28) & 0x0FFFFFFF;
            int produced = (int) result & 0x0FFFFFFF;

            if (status < 0 || status > 10) throw new IOException("Native LZMA2 error: " + status);

            inPos += consumed;
            if (status == 1) { // LZMA_STREAM_END
                eof = true;
            }

            if (produced > 0) return produced;
            if (status == 1 || inLen == 0) return -1;

            // if nothing was produced but not EOF, fall back to normal fillBuffer and read
        }

        if (fillBuffer() == -1) return -1;

        int n = Math.min(len, outLen - outPos);
        System.arraycopy(outBuffer, outPos, b, off, n);
        outPos += n;
        return n;
    }

    private int fillBuffer() throws IOException {
        while (outPos >= outLen) {
            if (inPos >= inLen) {
                inLen = in.read(inBuffer);
                inPos = 0;
                if (inLen == -1) {
                    inLen = 0;
                }
            }

            long result = NativeLZMA2.process(handle, inBuffer, inPos, inLen - inPos, outBuffer, 0, outBuffer.length, inLen == 0 ? 3 : 0);
            int status = (int) (result >> 56) & 0xFF;
            int consumed = (int) (result >> 28) & 0x0FFFFFFF;
            int produced = (int) result & 0x0FFFFFFF;

            if (status < 0 || status > 10) throw new IOException("Native LZMA2 error: " + status);

            inPos += consumed;
            outPos = 0;
            outLen = produced;

            if (status == 1) { // LZMA_STREAM_END
                eof = true;
                if (outLen == 0) return -1;
                break;
            }

            if (produced == 0 && inLen == 0) {
                 return -1;
            }
        }
        return outLen;
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        if (pool != null) {
            pool.release(handle);
        } else {
            NativeLZMA2.free(handle);
        }
        in.close();
        closed = true;
    }

}
