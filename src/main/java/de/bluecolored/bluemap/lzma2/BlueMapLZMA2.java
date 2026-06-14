package de.bluecolored.bluemap.lzma2;

import de.bluecolored.bluemap.core.storage.compression.BufferedCompression;
import de.bluecolored.bluemap.core.storage.compression.Compression;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.lzma2.nativexz.NativeDecoderPool;
import de.bluecolored.bluemap.lzma2.nativexz.NativeEncoderPool;
import de.bluecolored.bluemap.lzma2.nativexz.NativeLZMA2Loader;
import de.bluecolored.bluemap.lzma2.nativexz.NativeXZInputStream;
import de.bluecolored.bluemap.lzma2.nativexz.NativeXZOutputStream;

import java.io.IOException;

@SuppressWarnings("unused")
public class BlueMapLZMA2 implements Runnable {

    private final NativeEncoderPool[] encoderPools = new NativeEncoderPool[10];
    private NativeDecoderPool decoderPool;

    @Override
    public void run() {
        if (NativeLZMA2Loader.isLoaded()) {
            System.out.println("[BlueMapLZMA2] Successfully loaded native LZMA2 library.");
            for (int i = 0; i <= 9; i++) {
                // Use 1 thread per encoder in pool to avoid over-subscription
                encoderPools[i] = new NativeEncoderPool(i, 1, 32);
            }
            decoderPool = new NativeDecoderPool(32);

            for (int i = 0; i <= 9; i++) {
                register(i, "lzma2-" + i);
            }

            // Register default lzma2 as lzma2-6
            register(6, "lzma2");
        } else {
            System.out.println("[BlueMapLZMA2] Failed to load native LZMA2 library. LZMA2 compression will not be available.");
            Throwable error = NativeLZMA2Loader.getLoadError();
            if (error != null) {
                System.out.println("[BlueMapLZMA2] Loading error: " + error.getMessage());
            }
        }
    }

    private void register(int preset, String name) {
        Compression.REGISTRY.register(new BufferedCompression(
                new Key("bluemap-lzma2", name),
                "xz",
                ".xz",
                out -> {
                    try {
                        return new NativeXZOutputStream(out, encoderPools[preset]);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                in -> {
                    try {
                        return new NativeXZInputStream(in, decoderPool);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));
    }

}
