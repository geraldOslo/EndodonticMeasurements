package no.uio.odont;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.io.FileInfo;
import java.awt.Color;
import java.io.File;
import java.util.Random;

/**
 * Automated test runner for Endodontic Measurements 2.0.
 * Simulates user interaction without manual clicking.
 */
public class AutomationTest {
    public static void main(String[] args) {
        // Initialize ImageJ
        new ImageJ();

        String pluginDir = System.getProperty("user.dir");
        File rootDir = new File(pluginDir).getParentFile().getParentFile(); // Back up from bin/ or
                                                                            // plugin/Endodontic_Measurements_2.0
        File testImagesDir = new File(rootDir, "testimages" + File.separator + "AI-patient");
        File[] files = testImagesDir.listFiles((d, name) -> name.endsWith(".tif"));

        if (files == null || files.length == 0) {
            System.err.println("Error: No test images found in " + testImagesDir.getAbsolutePath());
            System.exit(1);
        }

        System.out.println("=== Starting Automation Test for Endodontic Measurements 2.0 ===");
        Random rand = new Random();

        // Test with 3 different images
        int numTests = Math.min(3, files.length);
        for (int i = 0; i < numTests; i++) {
            File testFile = files[i];
            System.out.println("\n[Test " + (i + 1) + "] Processing image: " + testFile.getName());

            ImagePlus imp = IJ.openImage(testFile.getAbsolutePath());
            if (imp == null) {
                System.err.println("Failed to open image: " + testFile.getName());
                continue;
            }

            // Manually set FileInfo to ensure directory and filename are available for
            // saving logic
            FileInfo fi = new FileInfo();
            fi.directory = testFile.getParent() + File.separator;
            fi.fileName = testFile.getName();
            imp.setFileInfo(fi);
            imp.show();

            // Instantiate and run the plugin
            Endodontic_Measurements_2 plugin = new Endodontic_Measurements_2();
            plugin.run("");

            // 1. Identification
            int quadrant = rand.nextInt(4) + 1;
            String tooth = String.valueOf(rand.nextInt(8) + 1);
            String root = "M";
            String imageType = "Other";
            System.out.println("Simulating Identification: Q" + quadrant + ", T" + tooth + ", R" + root);
            plugin.onIdentificationChanged(quadrant, tooth, root, imageType);

            // 2. Site Selection (simulate clicking points)
            String[] siteNames = { "Apex", "Apex GP", "Root canal deviation", "Canal entrance center" };
            for (String siteName : siteNames) {
                // Pick a random location
                int x = rand.nextInt(imp.getWidth());
                int y = rand.nextInt(imp.getHeight());
                imp.setRoi(new PointRoi(x, y));

                IJ.log("Placing site [" + siteName + "] at coordinate (" + x + ", " + y + ")");
                imp.setRoi(new PointRoi(x, y));
                plugin.onSiteSelected(siteName, Color.BLUE);
            }

            // 3. Qualitative Observations
            System.out.println("Setting qualitative observations: PAI=3, Voids=Y");
            plugin.onQualitativeSelected("pAi", "3");
            plugin.onQualitativeSelected("Apical voids", "Y");

            // 4. Save results
            System.out.println("Requesting Save...");
            plugin.onSaveRequested();

            // Clean up
            imp.close();
            System.out.println("Test successful for " + testFile.getName());
        }

        System.out.println("\n=== Automation Test Completed Successfully ===");
        // Wait a bit for async file operations if any
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        System.exit(0);
    }
}
