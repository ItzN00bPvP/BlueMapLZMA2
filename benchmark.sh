#!/bin/bash

# Configuration
CONFIG_FILE="config/storages/file.conf"
JAR_FILE="bluemap-5.20-cli.jar"
WEB_DIR="web/maps"
RESULTS_FILE="benchmark_results.md"

# Check if required files exist
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: $CONFIG_FILE not found!"
    exit 1
fi

if [ ! -f "$JAR_FILE" ]; then
    echo "Error: $JAR_FILE not found!"
    exit 1
fi

# Header for the table
echo "| Compression Level | Time (s) | Size |" > "$RESULTS_FILE"
echo "|-------------------|----------|------|" >> "$RESULTS_FILE"

echo "Starting benchmark..."
echo "Results will be saved to $RESULTS_FILE"

for i in {1..9}; do
    echo "------------------------------------------------"
    echo "Testing Compression Level: LZMA2-$i"
    
    # Update compression level in config
    # Matches 'compression: "bluemap-lzma2:lzma2-X"' and replaces X with $i
    sed -i "s/\(compression: \"bluemap-lzma2:lzma2-\)[0-9]\(\"\)/\1$i\2/" "$CONFIG_FILE"
    
    # Ensure fresh run by deleting web directory
    echo "Cleaning up $WEB_DIR..."
    rm -rf "$WEB_DIR"
    
    # Record start time
    START_TIME=$(date +%s)
    
    # Run BlueMap
    echo "Running BlueMap..."
    java -Xmx32G -jar "$JAR_FILE" -r
    
    # Record end time
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
    
    # Calculate size of web directory
    if [ -d "$WEB_DIR" ]; then
        SIZE=$(du -sh "$WEB_DIR" | cut -f1)
    else
        SIZE="N/A (Dir not created)"
    fi
    
    # Add to results table
    echo "| LZMA2-$i | ${DURATION}s | $SIZE |" >> "$RESULTS_FILE"
    
    echo "Level $i complete. Time: ${DURATION}s, Size: $SIZE"
done

echo "------------------------------------------------"
echo "Benchmark finished!"
cat "$RESULTS_FILE"
