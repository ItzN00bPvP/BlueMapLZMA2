package de.bluecolored.bluemap.lzma2.nativexz;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class NativeEncoderPool {

    private final int preset;
    private final int threads;
    private final BlockingQueue<Long> pool;

    public NativeEncoderPool(int preset, int threads, int poolSize) {
        this.preset = preset;
        this.threads = threads;
        this.pool = new ArrayBlockingQueue<>(poolSize);
    }

    public long claim() throws IOException {
        Long handle = pool.poll();
        if (handle == null) {
            handle = NativeLZMA2.createEncoder(preset, threads);
            if (handle == 0) throw new IOException("Failed to create native LZMA2 encoder");
        } else {
            int status = NativeLZMA2.resetEncoder(handle, preset, threads);
            if (status != 0) throw new IOException("Failed to reset native LZMA2 encoder: " + status);
        }
        return handle;
    }

    public void release(long handle) {
        if (!pool.offer(handle)) {
            NativeLZMA2.free(handle);
        }
    }

}
