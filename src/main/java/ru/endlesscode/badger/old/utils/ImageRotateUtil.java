package ru.endlesscode.badger.old.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageRotateUtil {
    @Nullable
    private static ImageInformation readImageInformation(File imageFile) throws IOException, MetadataException, ImageProcessingException {
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
        if (!metadata.containsDirectoryOfType(ExifIFD0Directory.class)) {
            return null;
        }

        Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);

        int orientation = 1;
        try {
            if (directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            }
        } catch (MetadataException e) {
            System.out.println(e.getMessage());
        }

        int width = jpegDirectory.getImageWidth();
        int height = jpegDirectory.getImageHeight();

        return new ImageInformation(orientation, width, height);
    }

    private static AffineTransform getExifTransformation(ImageInformation info) {
        AffineTransform t = new AffineTransform();

        switch (info.orientation) {
            case 1:
                break;
            case 2: // Flip X
                t.scale(-1.0, 1.0);
                t.translate(-info.width, 0);
                break;
            case 3: // PI rotation
                t.translate(info.width, info.height);
                t.rotate(Math.PI);
                break;
            case 4: // Flip Y
                t.scale(1.0, -1.0);
                t.translate(0, -info.height);
                break;
            case 5: // - PI/2 and Flip X
                t.rotate(-Math.PI / 2);
                t.scale(-1.0, 1.0);
                break;
            case 6: // -PI/2 and -width
                t.translate(info.height, 0);
                t.rotate(Math.PI / 2);
                break;
            case 7: // PI/2 and Flip
                t.scale(-1.0, 1.0);
                t.translate(-info.height, 0);
                t.translate(0, info.width);
                t.rotate(3 * Math.PI / 2);
                break;
            case 8: // PI / 2
                t.translate(0, info.width);
                t.rotate(3 * Math.PI / 2);
                break;
        }

        return t;
    }

    public static BufferedImage orientatedImage(File imageFile) throws IOException, MetadataException, ImageProcessingException {
        ImageInformation info = readImageInformation(imageFile);

        BufferedImage image = ImageIO.read(imageFile);
        if (info == null) {
            return image;
        }

        // Resize image
        if (info.orientation <= 4) {
            if (image.getHeight() > 1024) {
                image = Utils.resizeImage(image, 1024.0f / image.getHeight());
                info.scale(image.getWidth(), image.getHeight());
            }
        } else if (image.getWidth() > 1024) {
            image = Utils.resizeImage(image, 1024.0f / image.getWidth());
            info.scale(image.getWidth(), image.getHeight());
        }

        return transformImage(image, getExifTransformation(info));
    }

    private static BufferedImage transformImage(BufferedImage image, AffineTransform transform) {
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

        Rectangle bounds = op.getBounds2D(image).getBounds();
        BufferedImage destinationImage = new BufferedImage(bounds.width, bounds.height, image.getType());
        Graphics2D g = destinationImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
        op.filter(image, destinationImage);
        g.dispose();

        return destinationImage;
    }

    /**
     * Rotates an image. Actually rotates a new copy of the image.
     *
     * @param image The image to be rotated
     * @param angle The angle in degrees
     * @return The rotated image
     */
    public static BufferedImage rotateImage(BufferedImage image, double angle) {
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int width = image.getWidth(), height = image.getHeight();
        int newWidth = (int) Math.floor(width * cos + height * sin), newHeight = (int) Math.floor(height * cos + width * sin);

        BufferedImage result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setColor(Color.WHITE);
        g.fill(new Rectangle(result.getWidth(), result.getHeight()));
        g.translate((newWidth - width) / 2, (newHeight - height) / 2);
        g.rotate(angle, width / 2, height / 2);
        g.drawRenderedImage(image, null);
        g.dispose();

        return result;
    }

    public static class ImageInformation {
        private final int orientation;
        private int width;
        private int height;

        public ImageInformation(int orientation, int width, int height) {
            this.orientation = orientation;
            this.width = width;
            this.height = height;
        }

        public void scale(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return String.format("%dx%d,%d", this.width, this.height, this.orientation);
        }
    }
}