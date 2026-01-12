package no.uio.odont.model;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.Calibration;

/**
 * Represents a single tooth/root being measured.
 * Manages all sites and qualitative observations for a specific root.
 * Uses ImageJ Overlay for visualization instead of RoiManager.
 * 
 * @author Gerald Torgersen
 * @version 2.0
 * @date January 2026
 */
public class MeasurementRoot {
    private final ImagePlus imp;
    private final Calibration calibration;
    private final DecimalFormat formatter;
    private final Map<String, MeasurementSite> sites = new LinkedHashMap<>();
    private final Map<String, String> qualitativeObservations = new LinkedHashMap<>();

    // Store reference arcs separately so we can clear them easily
    private final Map<String, Roi> referenceRois = new LinkedHashMap<>();

    private int quadrantNumber = -1;
    private String toothNumber = "-1";
    private String rootName = "-1";

    // Configuration for arc diameters (in mm)
    private static final double NEAR_DISTANCE = 1.0;
    private static final double FAR_DISTANCE = 4.0;

    /**
     * Constructs a new MeasurementRoot.
     *
     * @param imp                 The image being measured.
     * @param decimalFormatSymbol The symbol to use for decimal separation.
     */
    public MeasurementRoot(ImagePlus imp, char decimalFormatSymbol) {
        this.imp = imp;
        this.calibration = imp.getCalibration();

        // Initialize formatter based on calibration units
        String pattern = calibration.getUnit().equals("pixels") ? "####" : "0.00";
        this.formatter = new DecimalFormat(pattern);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(decimalFormatSymbol);
        this.formatter.setDecimalFormatSymbols(symbols);

        // Ensure any existing overlay is cleared or managed?
        // For V2.0 start fresh
        imp.setOverlay(null);
    }

    public void setQuadrantNumber(int number) {
        this.quadrantNumber = number;
    }

    public void setToothNumber(String number) {
        this.toothNumber = number;
    }

    public void setRootName(String name) {
        this.rootName = name;
    }

    public boolean isFullyIdentified() {
        return quadrantNumber > 0 && !"-1".equals(toothNumber) && !"-1".equals(rootName);
    }

    public void addSite(String name, double x, double y, Color color) {
        MeasurementSite site = new MeasurementSite(x, y, color, formatter, calibration);
        sites.put(name, site);
        refreshOverlay();
    }

    public void removeSite(String name) {
        sites.remove(name);
        refreshOverlay();
    }

    public void setQualitativeObservation(String key, String value) {
        qualitativeObservations.put(key, value);
    }

    public String getQualitativeObservation(String key) {
        return qualitativeObservations.getOrDefault(key, "NS");
    }

    /**
     * Rebuilds the overlay with all current sites and reference ROIs.
     */
    private void refreshOverlay() {
        Overlay overlay = new Overlay();

        // Add all measurement sites
        for (Map.Entry<String, MeasurementSite> entry : sites.entrySet()) {
            String name = entry.getKey();
            MeasurementSite site = entry.getValue();

            Roi roi = site.toRoi();
            roi.setName(name);
            roi.setStrokeColor(site.getColor());

            // Add to overlay
            overlay.add(roi);
        }

        // Add all reference arcs
        for (Roi roi : referenceRois.values()) {
            overlay.add(roi);
        }

        imp.setOverlay(overlay);
    }

    public void drawReferenceArcs(String siteName) {
        removeReferenceArcs(); // Clear existing

        MeasurementSite site = sites.get(siteName);
        if (site == null)
            return;

        double x = site.getX();
        double y = site.getY();

        addReferenceArc("near", x, y, NEAR_DISTANCE);
        addReferenceArc("far", x, y, FAR_DISTANCE);

        refreshOverlay();
    }

    public void removeReferenceArcs() {
        referenceRois.clear();
        refreshOverlay();
    }

    private void addReferenceArc(String name, double x, double y, double distanceMm) {
        // Convert distance in mm to pixels
        double rawDistance = distanceMm / calibration.pixelWidth;
        double diameter = rawDistance * 2;

        Roi arc = new OvalRoi(x - rawDistance, y - rawDistance, diameter, diameter);
        arc.setName(name);
        arc.setStrokeColor(Color.RED);

        referenceRois.put(name, arc);
    }

    public String getSiteCoordinatesString(String siteName, char csvSeparator) {
        return Optional.ofNullable(sites.get(siteName))
                .map(site -> site.toCalibratedString(csvSeparator))
                .orElse(MeasurementSite.getMissingString(csvSeparator));
    }

    public String toString(char csvSeparator) {
        return String.format(
                "%s" + csvSeparator + " %d" + csvSeparator + " %s" + csvSeparator + " %s" + csvSeparator + " ",
                calibration.getUnit(), quadrantNumber, toothNumber, rootName);
    }

    /**
     * @return the Map of sites
     */
    public Map<String, MeasurementSite> getSites() {
        return sites;
    }
}
