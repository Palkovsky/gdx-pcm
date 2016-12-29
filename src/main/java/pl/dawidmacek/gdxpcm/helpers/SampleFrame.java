package pl.dawidmacek.gdxpcm.helpers;

public class SampleFrame {

    private short[] data;
    private int length;
    private boolean littleEndian;

    public SampleFrame(short[] data, int length, boolean littleEndian) {
        this.data = data;
        this.length = length;
        this.littleEndian = littleEndian;
    }

    public short[] getData() {
        return data;
    }

    public int getLength() {
        return length;
    }
}
