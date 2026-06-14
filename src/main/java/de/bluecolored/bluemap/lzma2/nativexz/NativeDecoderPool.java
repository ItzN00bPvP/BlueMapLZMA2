package de.bluecolored.bluemap.lzma2.nativexz;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class NativeDecoderPool {

    private final BlockingQueue<Long> pool;

    public NativeDecoderPool(int poolSize) {
        this.pool = new ArrayBlockingQueue<>(poolSize);
    }

    public long claim() throws IOException {
        Long handle = pool.poll();
        if (handle == null) {
            handle = NativeLZMA2.createDecoder();
            if (handle == 0) throw new IOException("Failed to create native LZMA2 decoder");
        } else {
            int status = NativeLZMA2.resetDecoder(handle);
            if (status != 0) throw new IOException("Failed to reset native LZMA2 decoder: " + status);
        }
        return handle;
    }

    public void release(long handle) {
        if (!pool.offer(handle)) {
            NativeLZMA2.free(handle);
        }
    }

}
