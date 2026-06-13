package de.bluecolored.bluemap.lzma2;

import de.bluecolored.bluemap.core.storage.compression.BufferedCompression;
import de.bluecolored.bluemap.core.storage.compression.Compression;
import de.bluecolored.bluemap.core.util.Key;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.IOException;

@SuppressWarnings("unused")
public class BlueMapLZMA2 implements Runnable {

    @Override
    public void run() {
        for (int i = 0; i <= 9; i++) {
            final int preset = i;
            Compression.REGISTRY.register(new BufferedCompression(
                    new Key("bluemap-lzma2", "lzma2-" + preset),
                    "xz",
                    ".xz",
                    out -> {
                        try {
                            return new XZOutputStream(out, new LZMA2Options(preset));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    in -> {
                        try {
                            return new XZInputStream(in);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            ));
        }

        // Register default lzma2 as lzma2-6
        Compression.REGISTRY.register(new BufferedCompression(
                new Key("bluemap-lzma2", "lzma2"),
                "xz",
                ".xz",
                out -> {
                    try {
                        return new XZOutputStream(out, new LZMA2Options(6));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                in -> {
                    try {
                        return new XZInputStream(in);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));
    }

}
