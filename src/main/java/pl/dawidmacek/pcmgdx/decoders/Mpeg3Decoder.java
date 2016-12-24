package pl.dawidmacek.pcmgdx.decoders;

import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import pl.dawidmacek.pcmgdx.helpers.SampleFrame;


public class Mpeg3Decoder extends AudioDecoder {

    private Bitstream bitstream;
    private Decoder decoder;

    private int frequency, chanel, bufferSize;

    public Mpeg3Decoder(FileHandle file) {
        super(file);

        InputStream inputStream = file.read();

        bitstream = new Bitstream(inputStream);
        decoder = new Decoder();
        try {
            Header nextFrame = bitstream.readFrame();
            if (nextFrame != null) {
                decoder.decodeFrame(nextFrame, bitstream);
                frequency = decoder.getOutputFrequency();
                chanel = decoder.getOutputChannels();
                bufferSize = decoder.getOutputBlockSize();

                bitstream = new Bitstream(inputStream);
                decoder = new Decoder();
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
            Header frameHeader = bitstream.readFrame();

            if (decoder == null || bitstream == null || frameHeader == null)
                return null;

            SampleBuffer sampleBuffer = null;
            try {
                sampleBuffer = ((SampleBuffer) decoder.decodeFrame(frameHeader, bitstream));
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            if (sampleBuffer == null)
                return null;

            short[] samples = sampleBuffer.getBuffer();
            bitstream.closeFrame();
            totalLength += sampleBuffer.getBufferLength();

            renderedSeconds += secondsPerBuffer;

            return new SampleFrame(samples, totalLength, !isBigEndian());

        } catch (BitstreamException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
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

}
