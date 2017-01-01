package pl.dawidmacek.gdxpcm.decoders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;
import pl.dawidmacek.gdxpcm.helpers.BytesUtils;
import pl.dawidmacek.gdxpcm.helpers.SampleFrame;
import pl.dawidmacek.gdxpcm.streams.WavInputStream;

import java.io.IOException;


public class WAVDecoder extends AudioDecoder {

    private WavInputStream wavStream;

    public WAVDecoder(FileHandle file) {
        super(file);
        wavStream = new WavInputStream(file);
        setup();
    }

    public SampleFrame readNextFrame() {
        if (wavStream.getDataRemaining() <= 0)
            return null;

        byte[] buffer = new byte[getBufferSize()];
        try {
            wavStream.read(buffer);
            short[] shortSamples = BytesUtils.bytesToShorts(buffer, !isBigEndian());
            renderedSeconds += secondsPerBuffer;
            return new SampleFrame(shortSamples, getBufferSize() / 2, !isBigEndian());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean skipFrame() {
        if (wavStream.getDataRemaining() <= 0)
            return false;
        try {
            wavStream.read(new byte[getBufferSize()]);
        } catch (IOException e) {
            return false;
        }
        renderedSeconds += secondsPerBuffer;
        return true;
    }

    public int getFrequency() {
        return wavStream.getSampleRate();
    }

    public int getChannels() {
        return wavStream.getChannels();
    }


    @Override
    protected int getBufferSize() {
        return 4096 * 10;
    }


    public boolean isBigEndian() {
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        if (wavStream != null) {
            StreamUtils.closeQuietly(wavStream);
        }
        wavStream = new WavInputStream(file);
    }

    @Override
    public void dispose() {
        StreamUtils.closeQuietly(wavStream);
    }
}
