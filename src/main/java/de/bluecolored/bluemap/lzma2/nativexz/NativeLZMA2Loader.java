package de.bluecolored.bluemap.lzma2.nativexz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class NativeLZMA2Loader {

    private static boolean loaded = false;
    private static Throwable loadError = null;

    public static synchronized void load() {
        if (loaded) return;

        try {
            String os = System.getProperty("os.name").toLowerCase();
            String arch = System.getProperty("os.arch").toLowerCase();

            // Normalize OS and Arch
            if (os.contains("win")) os = "windows";
            else if (os.contains("mac")) os = "macos";
            else if (os.contains("linux")) os = "linux";

            if (arch.equals("amd64") || arch.equals("x86-64")) arch = "x86_64";

            String libName = "native_lzma";
            String extension = ".so";
            if (os.equals("windows")) extension = ".dll";
            else if (os.equals("macos")) extension = ".dylib";

            String resourceName = "/native/" + os + "-" + arch + "/lib" + libName + extension;

            try (InputStream in = NativeLZMA2Loader.class.getResourceAsStream(resourceName)) {
                if (in == null) {
                    // Fallback to simple /libname
                    resourceName = "/lib" + libName + extension;
                    try (InputStream in2 = NativeLZMA2Loader.class.getResourceAsStream(resourceName)) {
                        if (in2 == null) {
                            throw new IOException("Native library not found in resources: /native/" + os + "-" + arch + "/lib" + libName + extension);
                        }
                        loadFromStream(in2, extension);
                    }
                } else {
                    loadFromStream(in, extension);
                }
            }
            loaded = true;
        } catch (Throwable t) {
            loadError = t;
        }
    }

    private static void loadFromStream(InputStream in, String extension) throws IOException {
        File tempFile = Files.createTempFile("native_lzma", extension).toFile();
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        System.load(tempFile.getAbsolutePath());
    }

    public static boolean isLoaded() {
        load();
        return loaded;
    }

    public static Throwable getLoadError() {
        return loadError;
    }

}
