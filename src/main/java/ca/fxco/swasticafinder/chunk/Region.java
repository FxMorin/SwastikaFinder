package ca.fxco.swasticafinder.chunk;

import ca.fxco.swasticafinder.util.PosUtil;

import java.util.Random;

/**
 * Region data is a 16x16x16 array of SubChunks
 */
public class Region {

    // 256 possible combinations per position
    private final SubChunk[] data = new SubChunk[4096];
    private final Random random = new Random();

    public Region() {}

    public Region(long seed) {
        random.setSeed(seed);
    }

    public void populate() {
        for (int i = 0; i < data.length; i++) {
            data[i] = new SubChunk(random);
        }
    }

    public void setData(SubChunk[] newData) {
        System.arraycopy(newData, 0, data, 0, newData.length);
    }

    public void setChunk(byte x, byte y, byte z, SubChunk chunk) {
        data[(x & 15) + ((y & 15) << 4) + ((z & 15) << 8)] = chunk;
    }

    public SubChunk getChunk(byte x, byte y, byte z) {
        return data[(x & 15) + ((y & 15) << 4) + ((z & 15) << 8)];
    }

    public byte getBlock(short x, short y, short z) {
        return getChunk((byte) (x >> 4), (byte) (y >> 4), (byte) (z >> 4))
                .getBlock((byte) (x & 15), (byte) (y & 15), (byte) (z & 15));
    }

    public byte getBlock(int pos) {
        return getBlock(PosUtil.getX(pos), PosUtil.getY(pos), PosUtil.getZ(pos));
    }

    public void setBlock(short x, short y, short z, byte blockId) {
        getChunk((byte) (x >> 4), (byte) (y >> 4), (byte) (z >> 4))
                .setBlock((byte) (x & 15), (byte) (y & 15), (byte) (z & 15), blockId);
    }
}
