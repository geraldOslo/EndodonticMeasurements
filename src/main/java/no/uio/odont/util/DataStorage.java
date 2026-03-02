package no.uio.odont.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.awt.Color;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.process.ImageProcessor;
import no.uio.odont.model.MeasurementRoot;

/**
 * Handles saving of measurement results and modified images.
 * 
 * @author Gerald Torgersen
 * @version 2.0
 * @date January 2026
 */
public class DataStorage {
    private static final String GLOBAL_RESULT_FILENAME = "Measurements.csv";

    /**
     * Saves the measurement results to a CSV file.
     *
     * @param root    The MeasurementRoot containing data to be saved.
     * @param config  The application configuration.
     * @param content The data string to save.
     */
    public void saveResults(MeasurementRoot root, AppConfig config, String content, ImagePlus imp) {
        Path filePath = resolveSavePath(imp, config);

        if (filePath == null) {
            IJ.error("Could not determine save path for results.");
            return;
        }

        File file = filePath.toFile();
        ensureDirectoryExists(file.getParentFile());

        // Append content to file
        IJ.append(content, file.getAbsolutePath());
    }

    /**
     * Resolves the path where results should be saved based on configuration.
     */
    private Path resolveSavePath(ImagePlus imp, AppConfig config) {
        FileInfo fi = imp.getOriginalFileInfo();
        String directory = (fi != null && fi.directory != null) ? fi.directory : System.getProperty("user.dir");
        String fileName = (fi != null && fi.fileName != null) ? fi.fileName : imp.getTitle();

        if (config.isLocalStore()) {
            // Local store: same folder as image, same name with .csv extension
            int dotIndex = fileName.lastIndexOf('.');
            String csvName = (dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName) + ".csv";
            return Paths.get(directory, csvName);
        } else {
            // Global store: folder above image folder, fixed filename
            Path dirPath = Paths.get(directory);
            Path parentDir = dirPath.getParent();
            if (parentDir != null) {
                return parentDir.resolve(GLOBAL_RESULT_FILENAME);
            }
            return dirPath.resolve(GLOBAL_RESULT_FILENAME);
        }
    }

    private void ensureDirectoryExists(File directory) {
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Saves a copy of the image with all accumulated markers burnt in.
     * Called only at "Save and close" so all measured roots appear together.
     *
     * @param imp            The source image.
     * @param historicOverlay The overlay containing every saved site across all roots.
     */
    public void saveScoredImageCopy(ImagePlus imp, Overlay historicOverlay) {
        ImagePlus copy = imp.duplicate();

        // Convert to RGB so colored markers are visible in standard image viewers
        new ij.process.ImageConverter(copy).convertToRGB();

        ImageProcessor ip = copy.getProcessor();

        for (int i = 0; i < historicOverlay.size(); i++) {
            Roi roi = historicOverlay.get(i);
            if (!(roi instanceof PointRoi)) continue;

            Color color = roi.getStrokeColor();
            if (color == null) color = Color.RED;

            int x = Math.round(roi.getFloatPolygon().xpoints[0]);
            int y = Math.round(roi.getFloatPolygon().ypoints[0]);

            ip.setColor(color);
            ip.drawDot(x, y);
            drawRing(ip, x, y, 5);
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        String originalName = imp.getTitle();
        String newName = "Measured-" + timestamp + "-" + originalName;

        // Replace extension with .png
        int dotIndex = newName.lastIndexOf('.');
        if (dotIndex > 0) {
            newName = newName.substring(0, dotIndex) + ".png";
        } else {
            newName += ".png";
        }

        FileInfo fi = imp.getOriginalFileInfo();
        String directory = (fi != null && fi.directory != null) ? fi.directory : System.getProperty("user.dir");

        IJ.saveAs(copy, "PNG", Paths.get(directory, newName).toString());
    }

    /** Draws a ring (circle outline) of the given radius around (x, y). */
    private void drawRing(ImageProcessor ip, int x, int y, int radius) {
        ip.drawOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    public String generateTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
