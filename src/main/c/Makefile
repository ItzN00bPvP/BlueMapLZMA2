CC=gcc
CFLAGS=-fPIC -O3 -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux
# Statically link liblzma
LIBS=-Wl,-Bstatic -llzma -Wl,-Bdynamic -lpthread
TARGET=libnative_lzma.so

all: $(TARGET)

$(TARGET): native_lzma.c
	$(CC) $(CFLAGS) -shared -o $(TARGET) native_lzma.c $(LIBS)

clean:
	rm -f $(TARGET)
