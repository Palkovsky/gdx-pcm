package pl.dawidmacek.pcmgdx.helpers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


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
}
