# Endodontic Measurements

## Overview
The **Endodontic Measurements** plugin is a modernized tool for ImageJ designed to semi-automate the measurement of landmarks on dental radiographs. It allows observers to locate specific sites on a tooth/root and record their coordinates and qualitative observations (like PAI scores or clinical findings) directly into a structured CSV file for further analysis.

Version 2.0 features a completely refactored Swing-based UI, improved robustness, and dynamic scaling for better visibility on various screen resolutions.


---

## User Installation (Non-Technical)
For easy installation without compiling code:


### Windows
1.  Open the `dist` folder.
2.  Double-click `install.bat`.

### Mac / Linux
1.  Open Terminal.
2.  Navigate to the `dist` folder.
3.  Run `bash install.sh`.

### Manual Installation
Copy the files from the `dist` folder to your ImageJ plugins folder (e.g., `ImageJ/plugins/Endodontic_Measurements/`):
- `Endodontic_Measurements_2.0.jar`
- `Endodontic_Measurements.cfg`

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
2. Launch the plugin by pressing **F5** (or via Plugins > Endodontic Measurements 2.0).
3. Select the **Point Tool** (the plugin ensures this is selected by default).
3. Identify the object by selecting the **Quadrant**, **Tooth**, and **Root** numbers in the identification panel.

### Placing Markers
- Click on the point for a landmark on the X-ray image.
- Select a the corresponding site button (e.g., "Apex").
- The plugin will record the point and provide visual feedback.
- **Reference Circles**: When you place the **Apex GP** point, two circles (1mm and 4mm radius) are automatically drawn to guide you in placing the root canal diameter points.

### Sites Registered

**Table 1: Sites registered**
| Site number | Description                             |
|-------------|-----------------------------------------|
| 1           | Apex                                    |
| 2           | Apex gutta-percha (AGP)                 |
| 3           | Root canal start deviation; convex aspect |
| 4           | Root canal entrance center             |
| 5           | Mesial side of root canal 1 mm from point 2 |
| 6           | Distal side of root canal 1 mm from point 2 |
| 7           | Mesial side of root canal 4 mm from point 2 |
| 8           | Distal side of root canal 4 mm from point 2 |
| 9           | Marginal bone mesial                   |
| 10          | Marginal bone distal                   |
| 11          | Cemento-enamel junction (CEJ) mesial   |
| 12          | CEJ distal                             |
| 13          | Lesion periphery                       |
| 14          | Lesion mesial                          |
| 15          | Lesion distal                          |

<table>
    <tr>
        <td><img src="figures/EndodonticTooth.svg" alt="Figure 1a: The sites" width="400"/></td>
        <td><img src="figures/EndodonticRoot.svg" alt="Figure 1b: Circles to place root canal diameter" width="250"/></td>
    </tr>
    <tr>
        <td colspan="2" style="text-align:center">Figure 1: The sites (a) and the circle to place the root canal diameter (b)</td>
    </tr>
</table>

Sites 4 and 5 are defined to obtain the Schneider angle [3] together with the AGP. The sites 4, 5 and 7, 8 are used to measure the diameter of the root canal at distances 1 and 4 mm to the AGP. The user is guided to the distances by two circles centered at the AGP (Figure 1b).

<table>
    <tr><img src="figures/EndodonticToothAngles.svg" alt="Figure 2: The sites" width="500"/></tr>
    <tr>Figure 2: The Schneider angle (a) and the trigonometrics (b)</tr>
</table>

---

## Calculations
The coordinate data are imported to a spreadsheet for calculation of distances and angles.

**Table 2: Quantities calculated**
|Quantity to calculate |Points involved|
|-------------|-----------------------------------------|
|Schneider angle | 2, 3, 4|
|Root canal diameter | 5, 6, 7, 8|
|Distance between apex and the APG | 1, 2|
|Horizontal diameter of lesion | 14, 15|
|Vertical diameter of lesion | 1, 13|
|Bone height (attachment level) mesial | 9, 11|
|Bone height (attachment level) distal | 10, 12|

The distance between the apex and the AGP carries a sign depending on if there is a surplus (positive sign) or a deficit (negative sign) of filling material (Figure 3). The sign is automatically set in the evaluation spreadsheet by comparing the y-coordinates of points 1 and 2 together with the position of the tooth (maxilla or mandible) given by the quadrant number.

<table>
    <tr><img src="figures/EndodonticRoot_d.svg" alt="Figure 3" width="800"/></tr>
    <tr>Figure 3: Deficit (a) and surplus (b) of filling material</tr>
</table>

### Distances
Distances $d$ are calculated from the calibrated coordinates by the Pythagorean formula:
$$d = \sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2}$$

### The Schneider angle
Figure 2b shows the sites and lines extracted from Figure 2a. A line is drawn between sites 4 and 2 to define a triangle. The distances A, B and C are defined as shown in the figure. Using the law of cosines the angle $\beta$ can be calculated using the law cosines yielding:
$$\beta = \cos^{-1}(\frac{A^2 + C^2 - B^2}{2AC})$$

The Schneider angle $\alpha$ can be obtained by the observation:
$$\alpha = 180\degree - \beta$$

---

## Qualitative Observations
In addition to the site coordinates up to 14 qualitative observations may be stored.

**Table 3: Qualitative observations stored** (NS for not scored)
|Quality | Observations|
|-------------|-----------------------------------------|
|Periapical index (PAI) | NS, 1 - 5|
|Apical voids | NS, No, Yes|
|Coronal voids | NS, No, Yes|
|Orifice plug | NS, No, Yes|
|Apical file fracture | NS, No, Yes|
|Coronal file fracture | NS, No, Yes|
|Apical perforation | NS, No, Yes|
|Coronal perforation | NS, No, Yes|
|Post | NS, No, Yes|
|Restoration gap | NS, No, Yes|
|Caries | NS, None, Dentine, Pulp space|
|Restoration | NS, None, Filling, Crown/bridge|
|Support/load | NS, Two appr, One appr, No appr, Bridge abutment|
|Comments | Free text one line|

("Two approximal supports", "One approximal support" and "No approximal support")

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
2. `ImageJ/plugins/Endodontic_Measurements/`.


**Config Options:**
- `operator`: <Your Name/ID>
- `decimal-separator`: `.` or `,`
- `measurement_store`: `top` (global) or `local`
- `save_scored_image_copy`: `true` or `false`

---

## Compilation and Running
To compile and run the plugin, you can use the provided batch scripts in the repository root.

### Requirements
- **Java Development Kit (JDK)**: Version 8 or higher.
- **ImageJ**: A local installation of ImageJ with `ij.jar`.

### Commands
- **Compile**: Run `compile.bat`.
- **Package**: Run `package.bat` (outputs to `dist/`).
- **Install**: Run `install.bat` (installs from `dist/` to ImageJ).

---

## References
1. Jordal, Kristin; Skudutyte-Rysstad, Rasa; Sen, Abhijit; Torgersen, Gerald; Ørstavik, Dag & Sunde, Pia Titterud (2021). Effects of an individualized training course on technical quality and periapical status of teeth treated endodontically by dentists in the Public Dental Service in Norway. An observational intervention study. International Endodontic Journal. ISSN 0143-2885. doi: [10.1111/iej.13669](https://doi.org/10.1111/iej.13669).
2. Preus et al. (2015). A new digital tool for radiographic bone level measurements in longitudinal studies. BMC Oral Health. ISSN 1472-6831. 15(1), s. 1–7. doi: [10.1186/s12903-015-0092-9](https://doi.org/10.1186/s12903-015-0092-9).
3. Schneider SW. A comparison of canal preparations in straight and curved root canals. Oral Surg Oral Med Oral Pathol 1971;32(2):271-5.

---

## License
Endodontic Measurements plugin is distributed under the **Creative Commons Attribution 4.0 International Public License**.
Copyright (C) 2024-2026 Gerald Torgersen.
