package org.uav.audio;

import org.joml.Vector3f;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.openal.SOFTDirectChannels.AL_DIRECT_CHANNELS_SOFT;
import static org.lwjgl.system.MemoryUtil.*;

public class AudioRenderer implements AutoCloseable {
    private static final int BUFFER_SIZE = 1024 * 8;

    private final VorbisTrack track;

    private final int format;

    private final int source;
    private final IntBuffer buffers;

    private final ShortBuffer pcm;

    long bufferOffset; // offset of last processed buffer
    long offset; // bufferOffset + offset of current buffer
    long lastOffset; // last offset update

    public AudioRenderer(VorbisTrack track) {
        this.track = track;

        switch (track.getChannels()) {
            case 1:
                this.format = AL_FORMAT_MONO16;
                break;
            case 2:
                this.format = AL_FORMAT_STEREO16;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported number of channels: " + track.getChannels());
        }

        this.pcm = memAllocShort(BUFFER_SIZE);
        source = alGenSources();
        alSourcei(source, AL_DIRECT_CHANNELS_SOFT, AL_TRUE);

        buffers = memAllocInt(2);
        alGenBuffers(buffers);
    }

    public void setPosition(Vector3f position) {
        alSource3f(source, AL_POSITION, position.x, position.y, position.z);
    }
    public void setPitch(float pitch) {
        alSourcef(source, AL_PITCH, pitch);
    }

    public void setVelocity(Vector3f velocity) {
        alSource3f(source, AL_VELOCITY, velocity.x, velocity.y, velocity.z);
    }

    public void setGain(float gain) {
        alSourcef(source, AL_GAIN, gain);
    }

    @Override
    public void close() {
        alDeleteBuffers(buffers);
        alDeleteSources(source);

        memFree(buffers);
        memFree(pcm);

        alcSetThreadContext(NULL);
    }

    private int stream(int buffer) {
        int samples = 0;

        while (samples < BUFFER_SIZE) {
            pcm.position(samples);
            int samplesPerChannel = track.getSamples(pcm);
            if (samplesPerChannel == 0) {
                break;
            }

            samples += samplesPerChannel * track.getChannels();
        }

        if (samples != 0) {
            pcm.position(0);
            pcm.limit(samples);
            alBufferData(buffer, format, pcm, track.getSampleRate());
            pcm.limit(BUFFER_SIZE);
        }

        return samples;
    }

    public boolean play() {
        for (int i = 0; i < buffers.limit(); i++) {
            if (stream(buffers.get(i)) == 0) {
                return false;
            }
        }

        alSourceQueueBuffers(source, buffers);
        alSourcePlay(source);

        return true;
    }

    public boolean update(boolean loop) {
        int processed = alGetSourcei(source, AL_BUFFERS_PROCESSED);

        for (int i = 0; i < processed; i++) {
            bufferOffset += BUFFER_SIZE / track.getChannels();

            int buffer = alSourceUnqueueBuffers(source);

            if (stream(buffer) == 0) {
                boolean shouldExit = true;

                if (loop) {
                    track.rewind();
                    lastOffset = offset = bufferOffset = 0;
                    shouldExit = stream(buffer) == 0;
                }

                if (shouldExit) {
                    return false;
                }
            }
            alSourceQueueBuffers(source, buffer);
        }

        if (processed == 2) {
            alSourcePlay(source);
        }

        return true;
    }
}
