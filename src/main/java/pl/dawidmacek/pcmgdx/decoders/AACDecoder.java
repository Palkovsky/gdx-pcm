package pl.dawidmacek.pcmgdx.decoders;

import com.badlogic.gdx.files.FileHandle;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import pl.dawidmacek.pcmgdx.helpers.BytesUtils;
import pl.dawidmacek.pcmgdx.helpers.SampleFrame;

import java.io.EOFException;
import java.io.IOException;


public class AACDecoder extends AudioDecoder {

    private Decoder decoder;
    private ADTSDemultiplexer adts;
    private SampleBuffer sampleBuffer;

    private int bufferSize;

    public AACDecoder(FileHandle file) {
        super(file);
        try {
            adts = new ADTSDemultiplexer(file.read());
            decoder = new Decoder(adts.getDecoderSpecificInfo());

            sampleBuffer = new SampleBuffer();
            sampleBuffer.setBigEndian(true);

            byte[] nextFrame = adts.readNextFrame();
            decoder.decodeFrame(nextFrame, sampleBuffer);
            bufferSize = sampleBuffer.getData().length;

            reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setup();
    }

    @Override
    public SampleFrame readNextFrame() {
        try {
            byte[] nextFrame = adts.readNextFrame();

            if (nextFrame != null) {
                decoder.decodeFrame(nextFrame, sampleBuffer);
                byte[] audio = sampleBuffer.getData();
                short[] shortSamples = BytesUtils.bytesToShorts(audio, !isBigEndian());

                renderedSeconds += secondsPerBuffer;

                return new SampleFrame(shortSamples, shortSamples.length, !isBigEndian());
            }

        } catch (EOFException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public int getFrequency() {
        return adts.getSampleFrequency();
    }

    @Override
    public int getChannels() {
        return adts.getChannelCount();
    }

    @Override
    protected int getBufferSize() {
        return bufferSize;
    }

    @Override
    public boolean isBigEndian() {
        return sampleBuffer.isBigEndian();
    }

    @Override
    public void reset() {
        super.reset();
        try {
            adts = new ADTSDemultiplexer(file.read());
            decoder = new Decoder(adts.getDecoderSpecificInfo());
            sampleBuffer = new SampleBuffer();
            sampleBuffer.setBigEndian(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
