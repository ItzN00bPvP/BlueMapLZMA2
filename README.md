# BlueMap LZMA2
An addon for BlueMap that adds LZMA2 (XZ) compression support for tile storage.

## Features & Trade-offs
- **High Compression:** Reduces storage usage by approximately **50%** compared to standard gzip compression methods.
- **Compute Intensive:** Compression takes nearly **2x the time** compared to default methods.
- **High Memory Footprint:** Memory usage increases significantly with higher compression levels and parallel rendering threads.
- **On-the-fly Recompression:** Serving tiles is more compute-intensive because the server needs to recompress data on-the-fly to a browser-compatible format (like Gzip or Brotli) since browsers do not natively support LZMA2.
- **Native Implementation:** This addon uses a bundled native LZMA2 library. It is required for this addon to function; there is no pure-Java fallback/version like v1.0.

## Usage
1. Download the [bluemap-lzma2.jar](https://github.com/BlueMap-Minecraft/BlueMapLZMA2/releases).
2. Place the jar file into the `config/packs` folder next to your BlueMap config files.
3. Change the `compression` in your storage-config (`storages/file.conf` or `storages/sql.conf`) to:
   - `"bluemap-lzma2:lzma2"` (Default level: 6)
   - OR `"bluemap-lzma2:lzma2-<level>"` (Where level is from 0 to 9)
4. Reload BlueMap.

## Memory Usage
LZMA2 compression is very memory-intensive. The memory footprint scales with both the **compression level** and the **number of parallel encoding tasks** (which usually matches your BlueMap render-thread count).

- **Compression Level:** Higher levels (especially 7-9) exponentially increase the memory required for the compression dictionary.
- **Parallelism:** BlueMap renders tiles in parallel. This addon maintains a pool of native encoders to handle these parallel requests. On a system with many cores, many encoders will be active simultaneously.

**Example:**
Using **Level 9** on a **32-thread** system can result in a memory footprint of over **5.5 GB** just for the native encoders.
Using **Level 6** (Default) on a **32-thread** system results in a footprint of roughly **4 GB** (~125 MB per thread). This level is highly recommended as it offers the best balance of compression and memory efficiency.


## Benchmarks
The following benchmarks were performed on a 10,000 x 10,000 area (10k²):

| Compression Level | Time (Seconds) | Time (Minutes) | Size |
|-------------------|----------------|----------------|------|
| LZMA2-1           | 1793s          | 29.9m          | 14G  |
| LZMA2-2           | 1870s          | 31.2m          | 14G  |
| LZMA2-3           | 2260s          | 37.7m          | 13G  |
| LZMA2-4           | 2217s          | 37.0m          | 14G  |
| LZMA2-5           | 3125s          | 52.1m          | 11G  |
| LZMA2-6           | 3189s          | 53.1m          | 9.6G |
| LZMA2-7           | 3448s          | 57.5m          | 9.6G |
| LZMA2-8           | 3254s          | 54.2m          | 9.6G |
| LZMA2-9           | 3336s          | 55.6m          | 9.6G |
| LZMA2-9 (v1 Java) | 5873s          | 97.9m          | 9.6G |
| **Gzip (default)**| **1860s**      | **31.0m**      | **19G**|


## Native Library Support
The native library is bundled within the jar for common platforms (currently Linux-x64). If you are using a different platform, you may need to provide a compatible `libnative_lzma` binary. If the native library cannot be loaded, LZMA2 compression will not be available.

---
This project was developed with the assistance of AI.

