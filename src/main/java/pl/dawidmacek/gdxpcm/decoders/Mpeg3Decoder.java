package pl.dawidmacek.gdxpcm.decoders;

import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;

import javazoom.jl.decoder.*;
import pl.dawidmacek.gdxpcm.helpers.BytesUtils;
import pl.dawidmacek.gdxpcm.helpers.SampleFrame;


public class Mpeg3Decoder extends AudioDecoder {

    private Bitstream bitstream;
    private Decoder decoder;
    private InputStream inputStream;

    private int frequency, chanel, buffSize;

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
                buffSize = decoder.getOutputBlockSize();

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

            if (decoder == null || bitstream == null)
                return null;

            int totalLength = 0;
            short[] outputSamples = new short[0];
            int numFrames = getBufferSize() / buffSize;

            for (int i = 0; i < numFrames; i++) {
                Header frameHeader = bitstream.readFrame();

                SampleBuffer sampleBuffer;
                try {
                    sampleBuffer = ((SampleBuffer) decoder.decodeFrame(frameHeader, bitstream));
                } catch (Exception e) {
                    return null;
                }
                if (sampleBuffer == null)
                    return null;

                bitstream.closeFrame();

                short[] samples = sampleBuffer.getBuffer().clone();
                int length = sampleBuffer.getBufferLength();
                totalLength += length;
                outputSamples = BytesUtils.concat(outputSamples, samples);
            }

            renderedSeconds += secondsPerBuffer;

            return new SampleFrame(outputSamples.clone(), totalLength, !isBigEndian());

        } catch (BitstreamException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean skipFrame() {
        if (decoder == null || bitstream == null)
            return false;

        try {
            if (bitstream.readFrame() == null)
                return false;
            bitstream.closeFrame();
        } catch (BitstreamException e) {
            return false;
        }

        renderedSeconds += secondsPerBuffer;
        return true;
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
        return buffSize;
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
    }

}
