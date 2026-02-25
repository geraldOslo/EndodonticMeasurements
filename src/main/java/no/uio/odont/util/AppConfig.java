package no.uio.odont.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.text.DecimalFormatSymbols;

import ij.IJ;

/**
 * Handles application configuration loading and provides access to settings.
 * 
 * @author Gerald Torgersen
 * @version 2.0
 * @date January 2026
 */
public class AppConfig {
    private String operator;
    private char decimalSeparator;
    private char csvSeparator;
    private String measurementStore; // "top" or "local"
    private boolean saveScoredCopy;

    private static final String DEFAULT_STORAGE_MODE = "top";
    private static final String CONFIG_FILE_NAME = "Endodontic_Measurements.cfg";

    public AppConfig() {
        setDefaults();
        loadConfig();
    }

    private void setDefaults() {
        this.operator = System.getProperty("user.name", "Unknown");
        this.decimalSeparator = new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();
        updateCsvSeparator();
        this.measurementStore = DEFAULT_STORAGE_MODE;
        this.saveScoredCopy = true;
    }

    private void loadConfig() {
        // Try to find config in several locations:
        // 1. Current directory (useful for development)
        // 2. ImageJ plugins directory / Endodontic_Measurements_2.0
        // 3. ImageJ plugins directory / Endodontic_Measurements (legacy)

        File configFile = findConfigFile();
        if (configFile != null && configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    parseLine(line);
                }
            } catch (IOException e) {
                IJ.log("Error reading config: " + e.getMessage());
            }
        }
    }

    private File findConfigFile() {
        // 1. Check relative to current working directory
        File local = new File(CONFIG_FILE_NAME);
        if (local.exists())
            return local;

        // 2. Check in plugins directory (V2 location)
        String pluginsDir = IJ.getDirectory("plugins");
        if (pluginsDir != null) {
            File v2Config = Paths.get(pluginsDir, "Endodontic_Measurements_2.0", CONFIG_FILE_NAME).toFile();
            if (v2Config.exists())
                return v2Config;

            // 3. Check legacy location
            File legacyConfig = Paths.get(pluginsDir, "Endodontic_Measurements", CONFIG_FILE_NAME).toFile();
            if (legacyConfig.exists())
                return legacyConfig;

            // 4. Check base plugins directory
            File baseConfig = Paths.get(pluginsDir, CONFIG_FILE_NAME).toFile();
            if (baseConfig.exists())
                return baseConfig;
        }

        return null;
    }

    private void parseLine(String line) {
        String[] parts = line.split(":", 2);
        if (parts.length < 2)
            return;

        String key = parts[0].trim().toLowerCase();
        String value = parts[1].trim();

        switch (key) {
            case "operator":
                if (!value.isEmpty())
                    this.operator = value;
                break;
            case "decimal-separator":
                if (!value.isEmpty()) {
                    this.decimalSeparator = value.charAt(0);
                    updateCsvSeparator();
                }
                break;
            case "measurement_store":
                if (!value.isEmpty())
                    this.measurementStore = value.toLowerCase();
                break;
            case "save_scored_image_copy":
                this.saveScoredCopy = Boolean.parseBoolean(value);
                break;
        }
    }

    private void updateCsvSeparator() {
        this.csvSeparator = (this.decimalSeparator == ',') ? ';' : ',';
    }

    public String getOperator() {
        return operator;
    }

    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    public char getCsvSeparator() {
        return csvSeparator;
    }

    public String getMeasurementStore() {
        return measurementStore;
    }

    public boolean isSaveScoredCopy() {
        return saveScoredCopy;
    }

    public boolean isLocalStore() {
        return "local".equalsIgnoreCase(measurementStore);
    }
}
