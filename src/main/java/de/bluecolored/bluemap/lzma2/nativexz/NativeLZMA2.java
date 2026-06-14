package de.bluecolored.bluemap.lzma2.nativexz;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NativeLZMA2 {

    static {
        NativeLZMA2Loader.load();
    }

    /**
     * Creates a new LZMA2 encoder.
     * @param preset The compression preset (0-9).
     * @param threads The number of threads to use (1 for single-threaded, 0 for automatic).
     * @return A handle to the encoder.
     */
    public static native long createEncoder(int preset, int threads);

    /**
     * Creates a new LZMA2 decoder.
     * @return A handle to the decoder.
     */
    public static native long createDecoder();

    /**
     * Resets an existing LZMA2 encoder for a new stream.
     * @param handle The handle to the encoder.
     * @param preset The compression preset (0-9).
     * @param threads The number of threads to use.
     * @return The status code from lzma_stream_encoder.
     */
    public static native int resetEncoder(long handle, int preset, int threads);

    /**
     * Resets an existing LZMA2 decoder for a new stream.
     * @param handle The handle to the decoder.
     * @return The status code from lzma_stream_decoder.
     */
    public static native int resetDecoder(long handle);

    /**
     * Processes data through the encoder/decoder.
     * @param handle The handle to the encoder/decoder.
     * @param input The input buffer.
     * @param inputOffset The offset in the input buffer.
     * @param inputLen The length of data to process.
     * @param output The output buffer.
     * @param outputOffset The offset in the output buffer.
     * @param outputLen The maximum length of data to write.
     * @param action The action to perform (0 = run, 1 = sync_flush, 2 = full_flush, 3 = finish).
     * @return A packed long containing [status (8 bits) | consumed (28 bits) | produced (28 bits)].
     */
    public static native long process(long handle, byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset, int outputLen, int action);

    /**
     * Frees the encoder/decoder.
     * @param handle The handle to the encoder/decoder.
     */
    public static native void free(long handle);

}
