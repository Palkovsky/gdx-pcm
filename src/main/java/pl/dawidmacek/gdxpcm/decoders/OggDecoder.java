package pl.dawidmacek.gdxpcm.decoders;

import com.badlogic.gdx.files.FileHandle;
import pl.dawidmacek.gdxpcm.helpers.BytesUtils;
import pl.dawidmacek.gdxpcm.helpers.SampleFrame;
import pl.dawidmacek.gdxpcm.streams.OggInputStream;

import java.io.IOException;


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
        buffer = null;

        renderedSeconds += secondsPerBuffer;

        return new SampleFrame(shortSamples, shortSamples.length, !isBigEndian());
    }

    @Override
    public boolean skipFrame() {
        if (input.atEnd())
            return false;
        input.read(new byte[getBufferSize()]);
        renderedSeconds += secondsPerBuffer;
        return true;
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
        return 4096 * 10;
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
    }
}
