This is an example Maven project implementing my Endotontic_Measurements plugin for ImagJ.

I have a working version of this Plugin, and it has already been used in some research:<br>
Jordal, K et al. (2021) [1].

# Problem
But now I try to do it the right way, using Maven, Git, Eclipse for maintaining the plugin. Sadly there is something wrong. When I run the EndoStarter.java for testing the Plugin I get a Class not found error. But the Class-file is in the target folder. 
I would be so happy if anybody could give me some advice on that

Class not found while attempting to run "Endodontic_measurements"
java.lang.NoClassDefFoundError: Endodontic_measurements (wrong name: no/uio/odont/imagej/ Endodontic_measurements)

I think this is because of the definition in the prom file:
<properties>
	<package-name>no.uio.odont.imagej</package-name>                   
	<main-class>no.uio.odont.imagej.Endodontic_Measurements</main-class>        
	<license.licenseName>CC BY 4.0</license.licenseName>                      
	<license.copyrightOwners>Faculty of dentistry, University of Oslo</license.copyrightOwners> </properties>

# Purpose of the file:
The Plugin is used to mark landmarks as apex and stores the coordinates in a csv file for further analysis. There are also radiobuttons and free text fields for qualitative observations as PAI stored to the same csv-file. I will add a better description when I get the program to work.

Now it does not work anymore ...

# Instructions for use
Introduction
Repeated measurements of sites, distances and angles for the evaluation of the outcome of endodontic treatment can be time consuming and error prone. We provide a tool which allows the observer to concentrate on locating the correct point of the site and store the coordinates by mouse clicks.
The plugin is an extension of the free and open source image processing and analyzing package ImageJ1. It is written in the Java language and the source code is distributed under the GPL license v. 32. The plugin is based on a similar plugin used to measure periodontal attachment level loss (ref to come).
With the plugin a spreadsheet for calculation of distances and angles is available.

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
        <td><img src="/figures/EndodonticRoot.svg" alt="Figure 1b: Circles to place root canal diameter" width="500"/></td>
    </tr>
    <tr>
        <td colspan="2" style="text-align:center">Figure 1: The sites (a) and the circle to place the root canal diameter (b)</td>
    </tr>
</table>


Sites  4 and 5 are defined to obtain the Schneider angle [2]  together with the AGP. The sites 4, 5 and 7, 8 are used to measure the diameter of the root canal at distances 1 and 4 mm to the AGP. The user is guided to the distances by two circles centered at the AGP (Figure 1b).  

<table>
    <tr><img src="/figures/EndodonticToothAngles.svg" alt="Figure 2: The sites" width="800"/></tr>
    <tr>Figure 2: The Scneider angle (a) and the trigonometrics (b)</tr>
</table>

## Calculations
The coordinate data are imported to a spreadsheet for calculation of distances and angles|
$$\sqrt{(x_2 - x_1)^2}$$


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
$$d = \sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2}

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

# References
1. Jordal, Kristin; Skudutyte-Rysstad, Rasa; Sen, Abhijit; Torgersen, Gerald; Ørstavik, Dag & Sunde, Pia Titterud (2021). Effects of an individualized training course on technical quality and periapical status of teeth treated endodontically by dentists in the Public Dental Service in Norway. An observational intervention study. International Endodontic Journal. ISSN 0143-2885. doi: [10.1111/iej.13669](https://doi.org/10.1111/iej.13669).
2. Schneider SW. A comparison of canal preparations in straight and curved root canals. Oral Surg Oral Med Oral Pathol 1971;32(2):271-5.
