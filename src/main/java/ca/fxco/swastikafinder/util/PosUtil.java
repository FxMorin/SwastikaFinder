package ca.fxco.swastikafinder.util;

public class PosUtil {

    // Region position
    public static int toInt(short x, short y, short z) {
        return x + (y << 8) + (z << 16);
    }

    // Region position
    public static short getX(int s) {
        return (short) (s & 255);
    }

    // Region position
    public static short getY(int s) {
        return (short) ((s >> 8) & 255);
    }

    // Region position
    public static short getZ(int s) {
        return (short) ((s >> 16) & 255);
    }

    // Short only works since we only use bytes 0-15 large
    public static short toShort(byte x, byte y, byte z) {
        return (short) (x + (y << 4) + (z << 8));
    }

    // Short only works since we only use bytes 0-15 large
    public static byte getX(short s) {
        return (byte) (s & 15);
    }

    // Short only works since we only use bytes 0-15 large
    public static byte getY(short s) {
        return (byte) ((s >> 4) & 15);
    }

    // Short only works since we only use bytes 0-15 large
    public static byte getZ(short s) {
        return (byte) ((s >> 8) & 15);
    }
}
