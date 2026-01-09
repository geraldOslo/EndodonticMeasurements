package no.uio.odont.model;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import ij.gui.PointRoi;
import ij.measure.Calibration;

/**
 * Represents a specific measurement site on an image.
 * Stores coordinates, visual properties, and handles coordinate transformation
 * based on calibration.
 */
public class MeasurementSite {
    private final Point2D.Double coordinates;
    private final Color color;
    private final DecimalFormat formatter;
    private final Calibration calibration;

    /**
     * Constructs a new MeasurementSite.
     *
     * @param x           The x-coordinate in pixels.
     * @param y           The y-coordinate in pixels.
     * @param color       The display color for this site.
     * @param formatter   The decimal formatter for output.
     * @param calibration The image calibration for unit conversion.
     */
    public MeasurementSite(double x, double y, Color color, DecimalFormat formatter, Calibration calibration) {
        this.coordinates = new Point2D.Double(x, y);
        this.color = color;
        this.formatter = formatter;
        this.calibration = calibration;
    }

    public PointRoi toRoi() {
        return new PointRoi(coordinates.x, coordinates.y);
    }

    public double getX() {
        return coordinates.x;
    }

    public double getY() {
        return coordinates.y;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Converts the site's coordinates to calibrated units and returns them as a
     * string.
     *
     * @param csvSeparator The separator to use between coordinates.
     * @return A string in the format "x<sep> y<sep> ".
     */
    public String toCalibratedString(char csvSeparator) {
        double calX = calibration.getX(coordinates.x);
        double calY = calibration.getY(coordinates.y);
        return formatter.format(calX) + csvSeparator + " " + formatter.format(calY) + csvSeparator + " ";
    }

    /**
     * Static helper for missing site representation in the output string.
     *
     * @param csvSeparator The separator to use.
     * @return String representation for a missing site.
     */
    public static String getMissingString(char csvSeparator) {
        return "X" + csvSeparator + "X" + csvSeparator;
    }
}
