package pl.dawidmacek.pcmgdx.decoders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.IOException;

import pl.dawidmacek.pcmgdx.helpers.BytesUtils;
import pl.dawidmacek.pcmgdx.helpers.SampleFrame;
import pl.dawidmacek.pcmgdx.streams.WavInputStream;


public class WAVDecoder extends AudioDecoder {

    private WavInputStream inputStream;

    public WAVDecoder(FileHandle file) {
        super(file);
        inputStream = new WavInputStream(file);
        setup();
    }

    public SampleFrame readNextFrame() {
        if (inputStream.getDataRemaining() <= 0)
            return null;

        byte[] buffer = new byte[getBufferSize()];
        try {
            inputStream.read(buffer);
            short[] shortSamples = BytesUtils.bytesToShorts(buffer, !isBigEndian());
            renderedSeconds += secondsPerBuffer;
            return new SampleFrame(shortSamples, getBufferSize() / 2, !isBigEndian());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getFrequency() {
        return inputStream.getSampleRate();
    }

    public int getChannels() {
        return inputStream.getChannels();
    }


    @Override
    protected int getBufferSize() {
        return 4096;
    }


    public boolean isBigEndian() {
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        if (inputStream != null) {
            StreamUtils.closeQuietly(inputStream);
        }
        inputStream = new WavInputStream(file);
    }
}
