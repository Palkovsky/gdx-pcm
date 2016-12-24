package pl.dawidmacek.pcmgdx.decoders;

import com.badlogic.gdx.files.FileHandle;
import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Track;
import pl.dawidmacek.pcmgdx.helpers.BytesUtils;
import pl.dawidmacek.pcmgdx.helpers.SampleFrame;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class M4ADecoder extends AudioDecoder {

    private Track track;
    private Decoder decoder;
    private SampleBuffer sampleBuffer;

    private int channels, bufferSize;

    public M4ADecoder(FileHandle file) throws AACException {
        super(file);
        try {
            MP4Container mp4Container = new MP4Container(new RandomAccessFile(file.path(), "r"));

            List<Track> tracks = mp4Container.getMovie().getTracks(AudioTrack.AudioCodec.AAC);

            if (tracks.isEmpty())
                throw new AACException("movie does not contain any AAC track");

            track = tracks.get(0);
            decoder = new Decoder(track.getDecoderSpecificInfo());

            sampleBuffer = new SampleBuffer();
            sampleBuffer.setBigEndian(true);

            Frame nextFrame = track.readNextFrame();
            decoder.decodeFrame(nextFrame.getData(), sampleBuffer);

            channels = sampleBuffer.getChannels();
            bufferSize = sampleBuffer.getData().length;

            reset();
            setup();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SampleFrame readNextFrame() {

        try {

            Frame nextFrame = track.readNextFrame();

            if (nextFrame != null) {
                byte[] bytes = nextFrame.getData();
                decoder.decodeFrame(bytes, sampleBuffer);
                byte[] audio = sampleBuffer.getData();
                short[] shortSamples = BytesUtils.bytesToShorts(audio, !isBigEndian());

                renderedSeconds += secondsPerBuffer;

                return new SampleFrame(shortSamples, shortSamples.length, !isBigEndian());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public int getFrequency() {
        return decoder.getConfig().getSampleFrequency().getFrequency();
    }

    @Override
    public int getChannels() {
        return channels;
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
            MP4Container mp4Container = new MP4Container(file.read());
            track = mp4Container.getMovie().getTracks().get(0);
            sampleBuffer = new SampleBuffer();
            sampleBuffer.setBigEndian(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

