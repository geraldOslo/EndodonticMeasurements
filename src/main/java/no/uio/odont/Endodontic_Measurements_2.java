package no.uio.odont;

import java.awt.Color;
import java.io.File;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
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
    private Overlay historicOverlay;

    @Override
    public void run(String arg) {
        imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        config = new AppConfig();
        storage = new DataStorage();
        historicOverlay = new Overlay();
        historicOverlay.selectable(false);
        currentRoot = new MeasurementRoot(imp, config.getDecimalSeparator(), historicOverlay);

        // Ensure point tool is selected
        IJ.setTool(Toolbar.POINT);

        ui = new MeasurementUI("Endodontic Measurements 2.0", this, config);
    }

    @Override
    public void onSaveRequested() {
        if (!currentRoot.isFullyIdentified()) {
            IJ.error("Identification required", "Please select quadrant, tooth, root, and image type before saving.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        FileInfo fi = imp.getOriginalFileInfo();
        String path = (fi != null) ? fi.directory + fi.fileName : imp.getTitle();
        char sep = config.getCsvSeparator();

        // Header info: filepath,timestamp,operator,image type,unit,quadrant,tooth,root
        sb.append(path).append(sep);
        sb.append(storage.generateTimestamp()).append(sep);
        sb.append(config.getOperator()).append(sep);
        sb.append(currentRoot.toString(sep)); // Contains: image_type, unit, quadrant, tooth, root (all separated)

        // Qualitative observations
        // PAI,Ap voids,Cor voids,Orifice plug,Ap file fract,Cor file fract,Ap perf,Cor
        // perf,Post,Restoration gap,Caries,Restoration,Support/load
        sb.append(currentRoot.getQualitativeObservation("pAi")).append(sep);
        String[] qNames = { "Apical voids", "Coronal voids", "Orifice plug", "Apical file fracture",
                "Coronal file fracture", "Apical perforation", "Coronal perforation", "Post",
                "Restoration gap", "Caries", "Restoration", "Support/load" };
        for (String q : qNames) {
            sb.append(currentRoot.getQualitativeObservation(q)).append(sep);
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

        sb.append(ui.getComments());

        storage.saveResults(currentRoot, config, sb.toString(), imp);
        if (config.isSaveScoredCopy()) {
            storage.saveScoredImageCopy(imp, currentRoot);
        }

        // Snapshot current sites into the historic overlay so they stay visible
        // on the image after the current root is reset.
        currentRoot.copySitesToOverlay(historicOverlay);

        onResetRequested();
        IJ.showStatus("Measurements saved.");
    }

    @Override
    public void onSaveAndCloseRequested() {
        onSaveRequested();
        if (imp != null) {
            imp.close();
        }
        ui.close();
    }

    @Override
    public void onResetRequested() {
        currentRoot = new MeasurementRoot(imp, config.getDecimalSeparator(), historicOverlay);
        ui.reset();
    }

    @Override
    public void onSiteSelected(String siteName, Color color) {
        Roi roi = imp.getRoi();
        if (roi == null || roi.getType() != Roi.POINT) {
            IJ.error("Please select a point on the image first.");
            return;
        }

        double x = roi.getFloatPolygon().xpoints[0];
        double y = roi.getFloatPolygon().ypoints[0];

        currentRoot.addSite(siteName, x, y, color);

        // Logic for reference arcs (Apex GP)
        if ("Apex GP".equals(siteName)) {
            currentRoot.drawReferenceArcs(siteName);
        }

        imp.deleteRoi(); // Clear ROI after recording
        // Defensive refresh: some ImageJ versions clear the overlay when deleteRoi()
        // triggers a repaint. Re-applying ensures markers are always visible.
        currentRoot.refreshOverlay();
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
    public void onIdentificationChanged(int quadrant, String tooth, String root, String imageType) {
        currentRoot.setQuadrantNumber(quadrant);
        currentRoot.setToothNumber(tooth);
        currentRoot.setRootName(root);
        currentRoot.setImageType(imageType);
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
