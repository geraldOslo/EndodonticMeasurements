This is plain old ImageJ version of the plugin with updates. I never got the Maven version to work, so I gave ut that approach.

I have a working version of this Plugin, and it has already been used in some research:<br>
Jordal, K et al. (2021) [1].


# Purpose of the file:
The Plugin is used to mark landmarks as apex and stores the coordinates in a csv file for further analysis. There are also radiobuttons and free text fields for qualitative observations as PAI stored to the same csv-file.


# Instructions for use
Introduction
Repeated measurements of sites, distances and angles for the evaluation of the outcome of endodontic treatment can be time consuming and error prone. We provide a tool which allows the observer to concentrate on locating the correct point of the site and store the coordinates by mouse clicks.
The plugin is an extension of the free and open source image processing and analyzing package ImageJ1. It is written in the Java language and the source code is distributed under the Creative Commons Attribution 4.0 International Public License. The plugin is based on a similar plugin used to measure periodontal attachment level loss [2].
With the plugin a spreadsheet for calculation of distances and angles is available (will be added soon).
Copy the entire Endodontic_Measurements to the plugins folder of your ImageJ installation.

## The sites

Table 1: Sites registered
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
        <td><img src="/figures/EndodonticTooth.svg" alt="Figure 1a: The sites" width="500"/></td>
        <td><img src="/figures/EndodonticRoot.svg" alt="Figure 1b: Circles to place root canal diameter" width="350"/></td>
    </tr>
    <tr>
        <td colspan="2" style="text-align:center">Figure 1: The sites (a) and the circle to place the root canal diameter (b)</td>
    </tr>
</table>


Sites  4 and 5 are defined to obtain the Schneider angle [3]  together with the AGP. The sites 4, 5 and 7, 8 are used to measure the diameter of the root canal at distances 1 and 4 mm to the AGP. The user is guided to the distances by two circles centered at the AGP (Figure 1b).  

<table>
    <tr><img src="/figures/EndodonticToothAngles.svg" alt="Figure 2: The sites" width="500"/></tr>
    <tr>Figure 2: The Scneider angle (a) and the trigonometrics (b)</tr>
</table>

## Calculations
The coordinate data are imported to a spreadsheet for calculation of distances and angles|

Table 2: Quantities calculated
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
    <tr><img src="/figures/EndodonticRoot_d.svg" alt="Figure 3" width="800"/></tr>
    <tr>Figure 3: Deficit (a) and surplus (b) of filling material</tr>
</table>

### Distances
Distances d are calculated from the calibrated coordinates by the Pythagorean formula
$$d = \sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2}$$

### The Schneider angle

Figure 2b shows the sites and lines extracted from Figure 2a. A line is drawn between sites 4 and 2 to define a triangle. The distances A, B and C are defined as shown in the figure. Using the law of cosines the angle $\beta$ can be calculated using the law cosines yielding
$$\beta = \cos^{-1}(\frac{A^2 + C^2 - B^2}{2AC})$$

The Schneider angle $\alpha$ can be obtained by the observation
$$\alpha = 180\degree - \beta$$ 

## The qualitative observations
In addition to the site coordinates up to 14 qualitative observations may be stored. 

Table 3: Qualitative observations stored (NS for not scored)
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

("Two  approximal supports", "One approximal support " and "No approximal support")

## The output
1) Copy of image file with measure sites and lines burnt in, 
   file name: Measured-<timestamp>-<original filename>.tif

2) Resultfile in stored in:
	- if top mode choosen: Measurements.csv stored in directory over image directory
	- if local choosen: <image-filename without extension>-measurements.csv in same folder as image file
	One line per root and measurement added to the bottom of the csv if existing.

###Format of the csv file:
```
<path and filename>;<timestamp>;<operator>;<image type>;<EXIF-unit>;  
<quadrant number>, <tooth number>, <root number>, 
<apical voids (NS, N, Y)>, <coronal voids (NS, N, Y)>, <orifise plug (NS, N, Y)>, 
<Apical file fracture (NS, N, Y)>, <Coronal file fracture (NS, N, Y)>, 
<Apical perforation (NS, N, Y)>, <Coronal perforation (NS, N, Y)>, <Post(NS, N, Y)>,
<Caries (NS, None, Dentine, Pulp space)>, <Restoration (NS, None, Filling, Crown/bridge)>,
<Support/load (NS, Two appr, Lost tooth, No appr, Bridge abutment)>,
<site coordinates, format x,y, 
order: apex, apex GP, root canal deviation, canal entrance c., 
Lesion periphery, Lesion side M, Lesion side D, 
Bone level M, Bone level D,  CEJ M, CEJ D,
canal side M 1 mm, canal side D 1 mm,  canal side M 4mm, canal side D 4 mm, >
```

## The configuration file
The plugin is configured by an optional config file: Endodontic_Measurements.cfg in the same folder as the plugin:
operator:<operator name/ID> if missing: login user ID
decimal-separator:./, if missing uses system default
measurement_store:top/local if missing uses top storage
save_scored_image_copy:true/false if missing true

# References
1. Jordal, Kristin; Skudutyte-Rysstad, Rasa; Sen, Abhijit; Torgersen, Gerald; Ørstavik, Dag & Sunde, Pia Titterud (2021). Effects of an individualized training course on technical quality and periapical status of teeth treated endodontically by dentists in the Public Dental Service in Norway. An observational intervention study. International Endodontic Journal. ISSN 0143-2885. doi: [10.1111/iej.13669](https://doi.org/10.1111/iej.13669).
2. Preus et al. (2015). A new digital tool for radiographic bone level measurements in longitudinal studies. BMC Oral Health. ISSN 1472-6831. 15(1), s. 1–7. doi: [10.1186/s12903-015-0092-9](https://doi.org/10.1186/s12903-015-0092-9).
3. Schneider SW. A comparison of canal preparations in straight and curved root canals. Oral Surg Oral Med Oral Pathol 1971;32(2):271-5.
