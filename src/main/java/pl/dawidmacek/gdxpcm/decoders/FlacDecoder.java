package pl.dawidmacek.gdxpcm.decoders;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.frame.Frame;
import org.kc7bfi.jflac.util.ByteData;
import pl.dawidmacek.gdxpcm.helpers.BytesUtils;
import pl.dawidmacek.gdxpcm.helpers.SampleFrame;

import java.io.IOException;
import java.io.InputStream;



public class FlacDecoder extends AudioDecoder {

    private FLACDecoder decoder;
    private int bufferSize;
    private InputStream inputStream;

    public FlacDecoder(FileHandle file) {
        super(file);
        inputStream = file.read();
        decoder = new FLACDecoder(inputStream);
        try {
            decoder.readMetadata();
            Frame frame = decoder.readNextFrame();
            ByteData buff = new ByteData(frame.header.bitsPerSample);
            System.out.println(frame.header.bitsPerSample);
            ByteData decoded = decoder.decodeFrame(frame, buff);
            bufferSize = decoded.getLen();
            reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setup();
    }

    @Override
    public SampleFrame readNextFrame() {

        Frame frame = null;
        try {
            frame = decoder.readNextFrame();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(frame == null)
            return null;

        ByteData buff = new ByteData(frame.header.bitsPerSample);
        ByteData decoded = decoder.decodeFrame(frame, buff);

        byte[] audio = decoded.getData();
        short[] shortSamples = BytesUtils.bytesToShorts(audio, !isBigEndian());

        renderedSeconds += secondsPerBuffer;

        return new SampleFrame(shortSamples, decoded.getLen()/2, !isBigEndian());
    }


    @Override
    public int getFrequency() {
        return decoder.getStreamInfo().getSampleRate();
    }

    @Override
    public int getChannels() {
        return decoder.getStreamInfo().getChannels();
    }

    @Override
    protected int getBufferSize() {
        return bufferSize;
    }

    @Override
    public boolean isBigEndian() {
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        try {
            decoder = new FLACDecoder(file.read());
            decoder.readMetadata();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        decoder = null;
        StreamUtils.closeQuietly(inputStream);
    }

}
