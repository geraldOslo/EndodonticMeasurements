# Endodontic Measurements 2.0

## Overview
The **Endodontic Measurements** plugin is a modernized tool for ImageJ designed to semi-automate the measurement of landmarks on dental radiographs. It allows observers to locate specific sites on a tooth/root and record their coordinates and qualitative observations (like PAI scores or clinical findings) directly into a structured CSV file for further analysis.

Version 2.0 is a complete overhaul of the original plugin, modernized with current programming standards, a modular architecture, and improved data management.

---

## Purpose and Objectives
Repeated measurements of sites, distances, and angles for evaluating endodontic outcomes can be time-consuming and error-prone. This plugin provides a streamlined interface that allows the observer to:
- Concentrate on locating the correct anatomical points.
- Automatically store coordinates via mouse clicks.
- Record qualitative observations using standardized radio buttons.
- Calculate geometric metrics like the **Schneider angle** and root canal diameters.

---

## Instructions for Use

### Setting up Measurements
1. Open a radiograph in ImageJ.
2. Select the **Point Tool** (the plugin ensures this is selected by default).
3. Identify the object by selecting the **Quadrant**, **Tooth**, and **Root** numbers in the identification panel.
4. Select an **Image Type** (Digital or Analog).

### Placing Markers
- Select a site button (e.g., "Apex").
- Click on the corresponding point on the X-ray image.
- The plugin will record the point and provide visual feedback.
- **Reference Circles**: When you place the **Apex GP** point, two circles (1mm and 4mm radius) are automatically drawn to guide you in placing the root canal diameter points.

### Sites Registered
| Site Name | Description |
|-----------|-------------|
| Apex | Anatomical apex |
| Apex GP | Apex of the gutta-percha filling |
| Root canal deviation | Point of maximum curvature |
| Canal entrance center | Center of the orifice |
| Lesion periphery | Outer edge of the periapical lesion |
| Mesial/Distal Sites | MD variants for Lesion side, Bone level, CEJ, and Canal sides (1mm/4mm) |

---

## Qualitative Observations
In addition to coordinates, the plugin stores up to 14 qualitative observations:
- **Periapical Index (PAI)**: Scored from 1 to 5 (or NS for Not Scored).
- **Clinical Findings**: Y/N/NS for voids, plugs, file fractures, perforations, posts, and restoration gaps.
- **Pathology**: Caries depth and restoration type.
- **Biomechanical**: Support/load status.

---

## Output and Data Storage

### 1) Measured Image Copy
The plugin creates a copy of the image with all measured sites and reference lines burnt in.
- **Filename**: `Measured-<timestamp>-<original_filename>.tif`

### 2) Result File (CSV)
Measurements are appended to a CSV file. Version 2.0 introduces a specific naming convention:
- **Local Storage**: Results are saved as `<original_filename>.csv` in the same folder as the image.
- **Global Storage**: Results are saved to `Measurements.csv` in the directory **above** the image directory.

---

## Configuration
The plugin looks for `Endodontic_Measurements.cfg` in the following locations:
1. The local directory where the plugin is running.
2. `ImageJ/plugins/Endodontic_Measurements_2.0/`.
3. `ImageJ/plugins/Endodontic_Measurements/` (Legacy location).

**Config Options:**
- `operator`: <Your Name/ID>
- `decimal-separator`: `.` or `,`
- `measurement_store`: `top` (global) or `local`
- `save_scored_image_copy`: `true` or `false`

---

## Technical Improvements in V2.0
- **Modular Structure**: Separated into `model`, `ui`, `logic`, and `util` packages.
- **Encapsulated Data**: Uses proper Java objects (`MeasurementRoot`, `MeasurementSite`) instead of global arrays.
- **Modern I/O**: Uses `java.nio.path` for robust file handling across different Operating Systems.
- **Clean Event Handling**: Separated UI events from business logic via a controller listener pattern.

---

## Compilation and Running
- **Compile**: Run `compile_v2.bat`.
- **Run**: Run `run_v2.bat`.

---

## License
Endodontic Measurements plugin is distributed under the **Creative Commons Attribution 4.0 International Public License**.
Copyright (C) 2024-2026 Gerald Torgersen.
