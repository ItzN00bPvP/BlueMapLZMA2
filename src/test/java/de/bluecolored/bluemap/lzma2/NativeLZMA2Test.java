package de.bluecolored.bluemap.lzma2;

import de.bluecolored.bluemap.lzma2.nativexz.NativeLZMA2Loader;
import de.bluecolored.bluemap.lzma2.nativexz.NativeXZInputStream;
import de.bluecolored.bluemap.lzma2.nativexz.NativeXZOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

public class NativeLZMA2Test {

    @Test
    public void testNativeCompression() throws IOException {
        Assertions.assertTrue(NativeLZMA2Loader.isLoaded(), "Native library should be loaded: " + NativeLZMA2Loader.getLoadError());

        byte[] original = new byte[1024 * 1024];
        new Random(42).nextBytes(original);

        // Compress with native
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (NativeXZOutputStream xzOut = new NativeXZOutputStream(out, 6, 0)) { // Auto threads
            xzOut.write(original);
        }
        byte[] compressed = out.toByteArray();

        // Decompress with native
        ByteArrayInputStream in = new ByteArrayInputStream(compressed);
        ByteArrayOutputStream decompressedOut = new ByteArrayOutputStream();
        try (NativeXZInputStream xzIn = new NativeXZInputStream(in, null)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = xzIn.read(buffer)) != -1) {
                decompressedOut.write(buffer, 0, read);
            }
        }
        byte[] decompressed = decompressedOut.toByteArray();

        Assertions.assertArrayEquals(original, decompressed);
    }

}
