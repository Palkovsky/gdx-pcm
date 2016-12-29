package pl.dawidmacek.gdxpcm.decoders;

import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import pl.dawidmacek.gdxpcm.helpers.SampleFrame;


public class Mpeg3Decoder extends AudioDecoder {

    private Bitstream bitstream;
    private Decoder decoder;
    private InputStream inputStream;

    private int frequency, chanel, bufferSize;

    public Mpeg3Decoder(FileHandle file) {
        super(file);

        inputStream = file.read();

        bitstream = new Bitstream(inputStream);
        decoder = new Decoder();
        try {
            Header nextFrame = bitstream.readFrame();
            if (nextFrame != null) {
                decoder.decodeFrame(nextFrame, bitstream);
                frequency = decoder.getOutputFrequency();
                chanel = decoder.getOutputChannels();
                bufferSize = decoder.getOutputBlockSize();

                reset();
            }
        } catch (BitstreamException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        setup();
    }

    @Override
    public SampleFrame readNextFrame() {
        try {
            int totalLength = 0;

            if (decoder == null || bitstream == null)
                return null;

            Header frameHeader = bitstream.readFrame();

            SampleBuffer sampleBuffer = null;
            try {
                sampleBuffer = ((SampleBuffer) decoder.decodeFrame(frameHeader, bitstream));
            } catch (Exception e) {
                return null;
            }
            if (sampleBuffer == null)
                return null;

            if (bitstream == null)
                return null;
            bitstream.closeFrame();

            short[] samples = sampleBuffer.getBuffer();
            totalLength += sampleBuffer.getBufferLength();

            renderedSeconds += secondsPerBuffer;

            return new SampleFrame(samples, totalLength, !isBigEndian());

        } catch (BitstreamException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getFrequency() {
        return frequency;
    }

    @Override
    public int getChannels() {
        return chanel;
    }


    @Override
    protected int getBufferSize() {
        return bufferSize * 2;
    }


    @Override
    public boolean isBigEndian() {
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        bitstream = new Bitstream(file.read());
        decoder = new Decoder();
    }

    @Override
    public void dispose() {
        try {
            bitstream.close();
        } catch (BitstreamException e) {
        }
        StreamUtils.closeQuietly(inputStream);
        bitstream = null;
        decoder = null;

    }

}
