package pl.dawidmacek.gdxpcm.decoders;

import com.badlogic.gdx.files.FileHandle;
import pl.dawidmacek.gdxpcm.helpers.BytesUtils;
import pl.dawidmacek.gdxpcm.helpers.SampleFrame;
import pl.dawidmacek.gdxpcm.streams.OggInputStream;


public class OggDecoder extends AudioDecoder {

    private OggInputStream input;

    public OggDecoder(FileHandle file) {
        super(file);
        input = new OggInputStream(file.read());
        setup();
    }

    @Override
    public SampleFrame readNextFrame() {
        if (input.atEnd())
            return null;


        byte[] buffer = new byte[getBufferSize()];
        input.read(buffer);
        short[] shortSamples = BytesUtils.bytesToShorts(buffer, !isBigEndian());

        renderedSeconds += secondsPerBuffer;

        return new SampleFrame(shortSamples, getBufferSize() / 2, !isBigEndian());
    }

    @Override
    public int getFrequency() {
        return input.getSampleRate();
    }

    @Override
    public int getChannels() {
        return input.getChannels();
    }

    @Override
    protected int getBufferSize() {
        return 4092;
    }

    @Override
    public boolean isBigEndian() {
        return input.isBigEndian();
    }

    @Override
    public void reset() {
        super.reset();
        if (input != null)
            input.close();
        input = new OggInputStream(file.read());
    }

    @Override
    public void dispose() {
        input.close();
        input = null;
    }
}
