package pl.dawidmacek.pcmgdx.decoders;

import com.badlogic.gdx.files.FileHandle;
import com.sun.media.sound.AiffFileReader;
import pl.dawidmacek.pcmgdx.helpers.BytesUtils;
import pl.dawidmacek.pcmgdx.helpers.SampleFrame;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;


public class AiffDecoder extends AudioDecoder {

    private AudioInputStream inputStream;
    private AiffFileReader aiffReader;

    public AiffDecoder(FileHandle file) {
        super(file);
        aiffReader = new AiffFileReader();
        try {
            inputStream = aiffReader.getAudioInputStream(file.file());
            setup();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SampleFrame readNextFrame() {
        try {
            if (inputStream.available() <= 0)
                return null;

            byte[] buffer = new byte[getBufferSize()];
            inputStream.read(buffer);
            short[] shortSamples = BytesUtils.bytesToShorts(buffer, !isBigEndian());
            renderedSeconds += secondsPerBuffer;
            return new SampleFrame(shortSamples, getBufferSize() / 2, !isBigEndian());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getFrequency() {
        return (int) inputStream.getFormat().getSampleRate();
    }

    @Override
    public int getChannels() {
        return inputStream.getFormat().getChannels();
    }

    @Override
    protected int getBufferSize() {
        return 4096;
    }

    @Override
    public boolean isBigEndian() {
        return inputStream.getFormat().isBigEndian();
    }

    @Override
    public void reset() {
        super.reset();
        try {
            if (inputStream != null)
                inputStream.close();
            inputStream = aiffReader.getAudioInputStream(file.file());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }

    }
}
