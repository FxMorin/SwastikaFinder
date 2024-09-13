package ca.fxco.swasticafinder.chunk;

import java.util.Random;

/**
 * SubChunk data is a 16x16x16 array of bytes
 */
public class SubChunk {

    private final boolean[] palette = new boolean[128];

    // 256 possible combinations per position
    private final byte[] data = new byte[4096];

    // Empty chunk
    public SubChunk() {}

    public SubChunk(Random random) {
        populate(random);
        createPalette();
    }

    public SubChunk(byte[] data) {
        setData(data);
        createPalette();
    }

    public boolean doesPaletteContain(byte blockId) {
        return palette[blockId];
    }

    public void setBlock(byte x, byte y, byte z, byte blockId) {
        data[(x & 15) + ((y & 15) << 4) + ((z & 15) << 8)] = blockId;
        palette[blockId] = true;
    }

    public byte getBlock(byte x, byte y, byte z) {
        return data[(x & 15) + ((y & 15) << 4) + ((z & 15) << 8)];
    }

    private void populate(Random random) {
        nextBytes(random, data);
    }

    private void setData(byte[] newData) {
        System.arraycopy(newData, 0, data, 0, newData.length);
    }

    private void clearPalette() {
        for (int i = 0; i < 256; i++) {
            palette[i] = false;
        }
    }

    private void createPalette() {
        for (byte d : data) {
            palette[d] = true;
        }
    }

    private void nextBytes(Random random, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) random.nextInt(20);
        }
        /*for (int i = 0, len = bytes.length; i < len;) {
            for (int rnd = random.nextInt(),
                 n = Math.min(len - i, Integer.SIZE / Byte.SIZE);
                 n-- > 0; rnd >>= Byte.SIZE
            ) {
                byte b = (byte) rnd;
                if (b < 0) { // Positive only
                    bytes[i++] = (byte) (b + 128);
                } else {
                    bytes[i++] = b;
                }
            }
        }*/
    }
}
