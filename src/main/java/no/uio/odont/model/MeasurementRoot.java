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

    // Historic ROIs from previously saved roots — shown persistently on the image
    private final Overlay baseOverlay;

    private int quadrantNumber = -1;
    private String toothNumber = "-1";
    private String rootName = "-1";
    private String imageType = "-1";

    // Configuration for arc diameters (in mm)
    private static final double NEAR_DISTANCE = 1.0;
    private static final double FAR_DISTANCE = 4.0;

    /**
     * Constructs a new MeasurementRoot.
     *
     * @param imp                 The image being measured.
     * @param decimalFormatSymbol The symbol to use for decimal separation.
     * @param baseOverlay         Historic ROIs from previously saved roots to keep
     *                            visible. Pass an empty Overlay for a clean start.
     */
    public MeasurementRoot(ImagePlus imp, char decimalFormatSymbol, Overlay baseOverlay) {
        this.imp = imp;
        this.calibration = imp.getCalibration();
        this.baseOverlay = (baseOverlay != null) ? baseOverlay : new Overlay();

        // Initialize formatter based on calibration units
        String pattern = calibration.getUnit().equals("pixels") ? "####" : "0.00";
        this.formatter = new DecimalFormat(pattern);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(decimalFormatSymbol);
        this.formatter.setDecimalFormatSymbols(symbols);

        // Apply the base overlay immediately so historic markers stay visible
        refreshOverlay();
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

    public void setImageType(String type) {
        this.imageType = type;
    }

    public boolean isFullyIdentified() {
        return quadrantNumber > 0 && !"-1".equals(toothNumber) && !"-1".equals(rootName) && !"-1".equals(imageType);
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
     *
     * This is public so the controller can call it after imp.deleteRoi() as a
     * defensive measure — in some ImageJ versions deleteRoi() can affect the overlay.
     */
    public void refreshOverlay() {
        Overlay overlay = new Overlay();

        // Prevent the Point Tool from picking up overlay markers as selections.
        // Without this, clicking near a registered-site PointRoi removes it from
        // the overlay and makes it the active selection, causing it to "disappear".
        overlay.selectable(false);

        // Show historic markers from previously saved roots first (behind current ones)
        for (int i = 0; i < baseOverlay.size(); i++) {
            overlay.add(baseOverlay.get(i));
        }

        // Add all measurement sites
        for (Map.Entry<String, MeasurementSite> entry : sites.entrySet()) {
            String name = entry.getKey();
            MeasurementSite site = entry.getValue();

            Roi roi = site.toRoi();
            roi.setName(name);
            roi.setStrokeColor(site.getColor());

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
                "%s" + csvSeparator + "%s" + csvSeparator + "%d" + csvSeparator + "%s" + csvSeparator + "%s"
                        + csvSeparator,
                imageType, calibration.getUnit(), quadrantNumber, toothNumber, rootName);
    }

    /**
     * Copies all current measurement sites (but not reference arcs) into the
     * supplied overlay. Called before reset so the saved markers persist visually.
     */
    public void copySitesToOverlay(Overlay target) {
        for (Map.Entry<String, MeasurementSite> entry : sites.entrySet()) {
            Roi roi = entry.getValue().toRoi();
            roi.setName(entry.getKey());
            roi.setStrokeColor(entry.getValue().getColor());
            target.add(roi);
        }
    }

    /**
     * @return the Map of sites
     */
    public Map<String, MeasurementSite> getSites() {
        return sites;
    }
}
