package org.uav.logic.audio;

import lombok.Getter;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.uav.utils.IOUtils.ioResourceToByteBuffer;

// https://raw.githubusercontent.com/LWJGL/lwjgl3/master/modules/samples/src/test/java/org/lwjgl/demo/stb/Vorbis.java
public class VorbisTrack implements AutoCloseable {
    private final ByteBuffer encodedAudio;

    private final long handle;

    @Getter
    private final int channels;
    @Getter
    private final int sampleRate;

    final int   samplesLength;
    final float samplesSec;

    private final AtomicInteger sampleIndex;

    public VorbisTrack(String filePath, AtomicInteger sampleIndex) {
        try {
            encodedAudio = ioResourceToByteBuffer(filePath, 256 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer error = stack.mallocInt(1);
            handle = stb_vorbis_open_memory(encodedAudio, error, null);
            if (handle == NULL) {
                throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
            }

            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            stb_vorbis_get_info(handle, info);
            this.channels = info.channels();
            this.sampleRate = info.sample_rate();
        }

        this.samplesLength = stb_vorbis_stream_length_in_samples(handle);
        this.samplesSec = stb_vorbis_stream_length_in_seconds(handle);

        this.sampleIndex = sampleIndex;
        sampleIndex.set(0);
    }

    @Override
    public void close() {
        stb_vorbis_close(handle);
    }

    void progressBy(int samples) {
        sampleIndex.set(sampleIndex.get() + samples);
    }

    void setSampleIndex(int sampleIndex) {
        this.sampleIndex.set(sampleIndex);
    }

    void rewind() {
        seek(0);
    }

    void skip(int direction) {
        seek(min(max(0, stb_vorbis_get_sample_offset(handle) + direction * sampleRate), samplesLength));
    }

    void skipTo(float offset0to1) {
        seek(round(samplesLength * offset0to1));
    }

    // called from audio thread
    synchronized int getSamples(ShortBuffer pcm) {
        return stb_vorbis_get_samples_short_interleaved(handle, channels, pcm);
    }

    // called from UI thread
    private synchronized void seek(int sampleIndex) {
        stb_vorbis_seek(handle, sampleIndex);
        setSampleIndex(sampleIndex);
    }
}
