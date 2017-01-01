package pl.dawidmacek.gdxpcm.decoders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import pl.dawidmacek.gdxpcm.helpers.BytesUtils;
import pl.dawidmacek.gdxpcm.helpers.SampleFrame;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;


public class AACDecoder extends AudioDecoder {

    private Decoder decoder;
    private ADTSDemultiplexer adts;
    private SampleBuffer sampleBuffer;
    private InputStream inputStream;


    private int bufferSize;

    public AACDecoder(FileHandle file) {
        super(file);
        try {

            inputStream = file.read();

            adts = new ADTSDemultiplexer(inputStream);
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
    public boolean skipFrame() {

        try {
            byte[] nextFrame = adts.readNextFrame();
            if(nextFrame == null)
                return false;
            else {
                renderedSeconds += secondsPerBuffer;
                return true;
            }
        } catch (IOException e) {
            return false;
        }

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

    @Override
    public void dispose() {
        StreamUtils.closeQuietly(inputStream);
    }
}
