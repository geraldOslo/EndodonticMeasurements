This is an example Maven project implementing my Endotontic_Measurements plugin for ImagJ.

I have a working version of this Plugin, and it has already been used in some research:<br>
Jordal, K et al. (2021).

The plugin is now running in Eclipse but there might be some errors after I chagned some declarations because of warnings. Will go on debugging ...

# Purpose of the file:
The Plugin is used to mark landmarks as apex and stores the coordinates in a csv file for further analysis. There are also radiobuttons and free text fields for qualitative observations as PAI stored to the same csv-file. I will add a better description when I get the program to work.

Now it does not work anymore ...

# Instructions for use
Introduction
Repeated measurements of sites, distances and angles for the evaluation of the outcome of endodontic treatment can be time consuming and error prone. We provide a tool which allows the observer to concentrate on locating the correct point of the site and store the coordinates by mouse clicks.
The plugin is an extension of the free and open source image processing and analyzing package ImageJ1. It is written in the Java language and the source code is distributed under the GPL license v. 32. The plugin is based on a similar plugin used to measure periodontal attachment level loss (ref to come).
With the plugin a spreadsheet for calculation of distances and angles is available.

## The sites
Site number	Description
1	Apex
2	Apex gutta-percha (AGP)
3	Root canal start deviation; convex aspect
4	Root canal entrance center
5	mesial side of root canal 1 mm from point 2
6	distal side of root canal 1 mm from point 2
7	mesial side of root canal 4 mm from point 2
8	distal side of root canal 4 mm from point 2
9	Marginal bone mesial
10	Marginal bone distal
11	Cemento-enamel junction (CEJ) mesial
12	CEJ distal
13	Lesion periphery
14	Lesion mesial
15	Lesion distal

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


Sites  4 and 5 are defined to obtain the Schneider angle (Schneider 1971) together with the AGP. The sites 4, 5 and 7, 8 are used to measure the diameter of the root canal at distances 1 and 4 mm to the AGP. The user is guided to the distances by two circles centered at the AGP (Figure 1b).  

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
$$\sqrt{(x_1 - x_2)^2}

# References
Jordal, Kristin; Skudutyte-Rysstad, Rasa; Sen, Abhijit; Torgersen, Gerald; Ørstavik, Dag & Sunde, Pia Titterud (2021). Effects of an individualized training course on technical quality and periapical status of teeth treated endodontically by dentists in the Public Dental Service in Norway. An observational intervention study. International Endodontic Journal. ISSN 0143-2885. doi: 10.1111/iej.13669.
Schneider SW. A comparison of canal preparations in straight and curved root canals. Oral Surg Oral Med Oral Pathol 1971;32(2):271-5.


	

