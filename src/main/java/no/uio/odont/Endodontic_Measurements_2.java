package no.uio.odont;

import java.awt.Color;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.io.FileInfo;
import ij.plugin.PlugIn;
import no.uio.odont.model.MeasurementRoot;
import no.uio.odont.ui.MeasurementUI;
import no.uio.odont.util.AppConfig;
import no.uio.odont.util.DataStorage;

/**
 * Main entry point for Endodontic Measurements 2.0.
 * Orchestrates the measurement process.
 * 
 * @author Gerald Torgersen
 * @version 2.0
 * @date January 2026
 */
public class Endodontic_Measurements_2 implements PlugIn, MeasurementUI.ControlListener {
    private ImagePlus imp;
    private AppConfig config;
    private DataStorage storage;
    private MeasurementRoot currentRoot;
    private MeasurementUI ui;

    @Override
    public void run(String arg) {
        imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        config = new AppConfig();
        storage = new DataStorage();
        currentRoot = new MeasurementRoot(imp, config.getDecimalSeparator());

        // Ensure point tool is selected
        IJ.setTool(Toolbar.POINT);

        ui = new MeasurementUI("Endodontic Measurements 2.0", this, config);
    }

    @Override
    public void onSaveRequested() {
        if (!currentRoot.isFullyIdentified()) {
            IJ.error("Identification required", "Please select quadrant, tooth, and root before saving.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        FileInfo fi = imp.getOriginalFileInfo();
        String path = (fi != null) ? fi.directory + fi.fileName : imp.getTitle();
        char sep = config.getCsvSeparator();

        // Header info
        sb.append(path).append(sep);
        sb.append(storage.generateTimestamp()).append(sep).append(" ");
        sb.append(config.getOperator()).append(sep).append(" ");
        sb.append(currentRoot.toString(sep)); // contains unit; quadrant; tooth; root;

        // Qualitative observations
        sb.append(currentRoot.getQualitativeObservation("pAi")).append(sep).append(" ");
        String[] qNames = { "Apical voids", "Coronal voids", "Orifice plug", "Apical frac.",
                "Coronal frac.", "Apical perf.", "Coronal perf.", "Post",
                "Resto gap", "Caries", "Restoration", "Support" };
        for (String q : qNames) {
            sb.append(currentRoot.getQualitativeObservation(q)).append(sep).append(" ");
        }

        // Site coordinates
        String[] singleSites = { "Apex", "Apex GP", "Root canal deviation", "Canal entrance center",
                "Lesion periphery" };
        for (String s : singleSites) {
            sb.append(currentRoot.getSiteCoordinatesString(s, sep));
        }

        String[] mdSites = { "Lesion side", "Bone level", "CEJ", "Canal side 1mm", "Canal side 4mm" };
        for (String s : mdSites) {
            sb.append(currentRoot.getSiteCoordinatesString(s + "M", sep));
            sb.append(currentRoot.getSiteCoordinatesString(s + "D", sep));
        }

        sb.append(sep).append(" ").append(ui.getComments());

        storage.saveResults(currentRoot, config, sb.toString(), imp);
        if (config.isSaveScoredCopy()) {
            storage.saveScoredImageCopy(imp, currentRoot);
        }

        onResetRequested();
        IJ.showStatus("Measurements saved.");
    }

    public void onSaveAndCloseRequested() {
        onSaveRequested();
        if (imp != null) {
            imp.close();
        }
        ui.close();
    }

    @Override
    public void onResetRequested() {
        currentRoot = new MeasurementRoot(imp, config.getDecimalSeparator());
        ui.reset();
    }

    @Override
    public void onSiteSelected(String siteName, Color color) {
        Roi roi = imp.getRoi();
        if (roi == null || roi.getType() != Roi.POINT) {
            IJ.error("Please select a point on the image first.");
            return;
        }

        double x = roi.getBounds().getX() + roi.getBounds().getWidth() / 2.0;
        double y = roi.getBounds().getY() + roi.getBounds().getHeight() / 2.0;

        currentRoot.addSite(siteName, x, y, color);

        // Logic for reference arcs (Apex GP)
        if ("Apex GP".equals(siteName)) {
            currentRoot.drawReferenceArcs(siteName);
        }

        imp.deleteRoi(); // Clear ROI after recording
    }

    @Override
    public void onMissingSiteSelected(String siteName) {
        currentRoot.removeSite(siteName);
        if ("Apex GP".equals(siteName)) {
            currentRoot.removeReferenceArcs();
        }
    }

    @Override
    public void onQualitativeSelected(String key, String value) {
        currentRoot.setQualitativeObservation(key, value);
    }

    @Override
    public void onIdentificationChanged(int quadrant, String tooth, String root) {
        currentRoot.setQuadrantNumber(quadrant);
        currentRoot.setToothNumber(tooth);
        currentRoot.setRootName(root);
    }

    /**
     * Main method for debugging outside ImageJ.
     */
    public static void main(String[] args) {
        new ImageJ();
        // Setup for local debugging
        String pluginDir = System.getProperty("user.dir");
        File rootDir = new File(pluginDir).getParentFile().getParentFile();
        File testImageFile = new File(rootDir, "testimages/AI-patient/LI.tif");

        ImagePlus image = IJ.openImage(testImageFile.getAbsolutePath());
        if (image != null) {
            image.show();
            FileInfo fi = new FileInfo();
            fi.directory = testImageFile.getParent() + File.separator;
            fi.fileName = testImageFile.getName();
            image.setFileInfo(fi);
            new Endodontic_Measurements_2().run("");
        }
    }
}
