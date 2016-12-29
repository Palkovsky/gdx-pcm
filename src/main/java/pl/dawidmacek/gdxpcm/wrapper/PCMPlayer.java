package pl.dawidmacek.gdxpcm.wrapper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.sourceforge.jaad.aac.AACException;
import pl.dawidmacek.gdxpcm.decoders.*;
import pl.dawidmacek.gdxpcm.helpers.SampleFrame;


public class PCMPlayer {

    public enum PlayerState {PLAYING, PAUSED, FINISHED, SEEKING, DISPOSED}

    public enum FileType {MP3, FLAC, WAV, OGG, M4A, AAC}

    private AudioDecoder decoder;
    private AudioDevice device;
    private Thread playbackThread;

    private PlayerState state;
    private PlayerState preSeekState;
    private PCMPlayerFrameListener frameListener;
    private PCMPlayerStateChangeListener stateListener;

    private float volume;
    private float seekTarget;
    private boolean quietMode; //used when you want only to get your PCM frames.
    private boolean shouldReset;

    public PCMPlayer(FileHandle file) {
        this(file, false, findFileType(file.extension()));
    }

    public PCMPlayer(FileHandle file, boolean quietMode) {
        this(file, quietMode, findFileType(file.extension()));
    }

    public PCMPlayer(FileHandle file, boolean quietMode, FileType fileType) throws GdxRuntimeException {
        this.quietMode = quietMode;
        this.volume = 1;

        this.decoder = findDecoder(file, fileType);

        if (this.decoder == null || this.decoder.getFrequency() == 0 || this.decoder.getChannels() == 0)
            throw new GdxRuntimeException("Audio file couldn't be played.");

        if (!this.quietMode)
            this.device = Gdx.audio.newAudioDevice(this.decoder.getFrequency(), this.decoder.getChannels() == 1);

        this.playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!Thread.currentThread().isInterrupted()) {

                    if (decoder != null) {

                        if (shouldReset) {
                            decoder.reset();
                            shouldReset = false;
                        }

                        if (state == PlayerState.SEEKING) {

                            decoder.setPosition(seekTarget);
                            seekTarget = 0;
                            setState(preSeekState);
                            preSeekState = null;

                        } else if (state == PlayerState.PLAYING) {

                            SampleFrame frame = decoder.nextFrame();

                            if (frame == null) {
                                setState(PlayerState.FINISHED);
                            } else {
                                if (frameListener != null)
                                    frameListener.onNewFrame(frame);
                                if (!PCMPlayer.this.quietMode) {
                                    try {
                                        device.writeSamples(frame.getData(), 0, frame.getLength());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                }
                            }
                        }
                    }

                }
            }
        });
        playbackThread.start();
    }


    public void dispose() {
        playbackThread.interrupt();
        if (device != null)
            device.dispose();
        if (decoder != null)
            decoder.dispose();
        stateListener = null;
        frameListener = null;
        decoder = null;
        device = null;
        setState(PlayerState.DISPOSED);
    }

    private void setState(PlayerState targetState) {
        if (state != PlayerState.DISPOSED && targetState != state) {
            if (stateListener != null)
                stateListener.onStateChange(state, targetState);
            this.state = targetState;
        }
    }

    public boolean isLooping() {
        return decoder.isLooping();
    }

    public void setLooping(boolean looping) {
        decoder.setLooping(looping);
    }

    public float getProgress() {
        return decoder.getPosition();
    }

    public void setProgress(float seconds) {
        preSeekState = state;
        setState(PlayerState.SEEKING);
        seekTarget = seconds;
    }

    public void reset() {
        shouldReset = true;
        seekTarget = 0;
        preSeekState = null;
    }

    public void pause() {
        setState(PlayerState.PAUSED);
    }

    public void unpause() {
        if (state == PlayerState.PAUSED)
            setState(PlayerState.PLAYING);
    }

    public void play() {
        if (state != PlayerState.PLAYING && state != PlayerState.SEEKING) {
            setState(PlayerState.PLAYING);
        }
        if (state == PlayerState.SEEKING)
            preSeekState = PlayerState.PLAYING;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
        device.setVolume(volume);
    }

    public PlayerState getState() {
        return state;
    }

    public void setFrameListener(PCMPlayerFrameListener frameListener) {
        this.frameListener = frameListener;
    }

    public void setStateListener(PCMPlayerStateChangeListener stateListener) {
        this.stateListener = stateListener;
    }

    private static AudioDecoder findDecoder(FileHandle file, FileType fileType) throws GdxRuntimeException {
        switch (fileType) {
            case MP3:
                return new Mpeg3Decoder(file);
            case FLAC:
                return new FlacDecoder(file);
            case OGG:
                return new OggDecoder(file);
            case M4A:
                try {
                    return new M4ADecoder(file);
                } catch (Exception e) {
                    throw new GdxRuntimeException("Incompatible m4a file.");
                }
            case AAC:
                return new AACDecoder(file);
            case WAV:
                return new WAVDecoder(file);
        }

        throw new GdxRuntimeException("Incompatible audio file.");
    }

    private static FileType findFileType(String ext) {
        if (ext.equals("mp3"))
            return FileType.MP3;
        if (ext.equals("flac"))
            return FileType.FLAC;
        if (ext.equals("ogg"))
            return FileType.OGG;
        if (ext.equals("m4a"))
            return FileType.M4A;
        if (ext.equals("aac"))
            return FileType.AAC;
        if (ext.equals("wav"))
            return FileType.WAV;
        return null;
    }

    public interface PCMPlayerStateChangeListener {
        void onStateChange(PlayerState previousState, PlayerState currentState);
    }

    public interface PCMPlayerFrameListener {
        void onNewFrame(SampleFrame frame);
    }

}
