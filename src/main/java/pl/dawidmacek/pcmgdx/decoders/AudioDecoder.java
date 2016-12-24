package pl.dawidmacek.pcmgdx.decoders;

import com.badlogic.gdx.files.FileHandle;
import pl.dawidmacek.pcmgdx.helpers.SampleFrame;


public abstract class AudioDecoder {

    protected FileHandle file;

    //Playback info
    private boolean looping;
    protected boolean seeking;
    protected float renderedSeconds, secondsPerBuffer, bytesPerSecond;
    private SampleFrame currentFrame;

    public AudioDecoder(FileHandle file) {
        this.file = file;
        this.seeking = false;
        this.looping = false;
    }

    protected void setup() {
        this.renderedSeconds = 0;
        int bytesPerSample = getSampleSize() / 8;
        this.bytesPerSecond = getFrequency() * bytesPerSample * getChannels();
        this.secondsPerBuffer = getBufferSize() / this.bytesPerSecond;
    }

    /**
     * Returns next available PCM frame.
     */
    protected abstract SampleFrame readNextFrame();

    public SampleFrame nextFrame() {
        this.currentFrame = readNextFrame();

        //Check for audio end. If looping is on, start it over again.
        if (this.looping && this.currentFrame == null) {
            reset();
            this.currentFrame = readNextFrame();
        }

        return this.currentFrame;
    }

    public abstract int getFrequency();

    public abstract int getChannels();

    public int getSampleSize() {
        return getChannels() * 8;
    }

    protected abstract int getBufferSize();

    public abstract boolean isBigEndian();

    /**
     * Resets stream to the beginning.
     */
    public void reset() {
        this.seeking = false;
        this.renderedSeconds = 0;
    }

    /**
     * Returns current playback position.
     */
    public float getPosition() {
        return this.renderedSeconds;
    }

    public boolean isSeeking() {
        return seeking;
    }

    /**
     * Sets current stream position.
     */
    public void setPosition(float position) {
        this.seeking = true;
        if (position <= renderedSeconds) {
            reset();
        }

        while (renderedSeconds < (position - secondsPerBuffer)) {
            if (readNextFrame() == null) { //seeked to value grater than audio duration
                break;
            }
        }
        this.seeking = false;
    }

    /**
     * Configures looping behaviour.
     */
    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }
}