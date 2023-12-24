package org.uav.utils;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.system.MemoryUtil.memSlice;

public final class IOUtils {

    public static BufferedImage loadImage(String path) {
        try {
            return  ImageIO.read(new File(path));
        } catch (IOException e) {

            throw new RuntimeException("Failed to load " + path);
        }
    }

    public static byte[] extractImageData(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Get the color model of the BufferedImage
        int numComponents = image.getColorModel().getNumComponents();
        boolean hasAlpha = image.getColorModel().hasAlpha();

        // Determine the byte size per pixel based on the color model
        int bytesPerPixel = numComponents;

        // Create a byte array to hold the image data
        byte[] imageData = new byte[width * height * bytesPerPixel];

        // Iterate through each pixel and store the pixel data in the byte array
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                // Extract individual color components (RGBA)
                int red = (pixel) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = (pixel >> 16) & 0xFF;
                int alpha = hasAlpha ? (pixel >> 24) & 0xFF : 255;

                // Calculate the starting index in the byte array for the current pixel
                int index = (y * width + x) * bytesPerPixel;

                // Store the color components in the byte array
                imageData[index] = (byte) blue;
                if(bytesPerPixel >= 2) imageData[index + 1] = (byte) green;
                if(bytesPerPixel >= 3) imageData[index + 2] = (byte) red;
                if (hasAlpha) imageData[index + 3] = (byte) alpha;
            }
        }
        return imageData;
    }

    // https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/util/IOUtil.java

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = resource.startsWith("http") ? null : Paths.get(resource);
        if (path != null && Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1) {
                    ;
                }
            }
        } else {
            try (
                    InputStream source = resource.startsWith("http")
                            ? new URL(resource).openStream()
                            : IOUtils.class.getClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                    }
                }
            }
        }

        buffer.flip();
        return memSlice(buffer);
    }
}
