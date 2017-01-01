package pl.dawidmacek.gdxpcm.helpers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


public class BytesUtils {

    public static byte[] shortsToBytes(short[] shorts, boolean littleEndian) {
        ByteBuffer buffer = ByteBuffer.allocate(shorts.length * 2);
        buffer.order((littleEndian) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        buffer.asShortBuffer().put(shorts);
        return buffer.array();
    }

    public static short[] bytesToShorts(byte[] bytes, boolean littleEndian) {
        short[] shorts = new short[bytes.length / 2];
        if (!littleEndian) {
            ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shorts);
        } else {
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        }
        return shorts;
    }

    public static short[] concat(short[] first, short[]... rest) {
        int totalLength = first.length;
        for (short[] array : rest) {
            totalLength += array.length;
        }
        short[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (short[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static byte[] concat(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
