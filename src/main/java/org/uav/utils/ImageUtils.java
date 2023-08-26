package org.uav.utils;

import java.awt.image.BufferedImage;

public class ImageUtils {
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
                imageData[index + 1] = (byte) green;
                imageData[index + 2] = (byte) red;
                if (hasAlpha) {
                    imageData[index + 3] = (byte) alpha;
                }
            }
        }
        return imageData;
    }
}
