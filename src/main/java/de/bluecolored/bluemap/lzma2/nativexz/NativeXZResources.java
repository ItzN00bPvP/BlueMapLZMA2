package de.bluecolored.bluemap.lzma2.nativexz;

public class NativeXZResources {

    private static final ThreadLocal<NativeXZResources> THREAD_LOCAL = ThreadLocal.withInitial(NativeXZResources::new);

    public final byte[] outBuffer = new byte[128 * 1024];

    public static NativeXZResources get() {
        return THREAD_LOCAL.get();
    }

}
