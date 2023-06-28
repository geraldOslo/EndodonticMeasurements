This is an example Maven project implementing my Endotontic_Measurements plugin for ImagJ.

I have a working version of this Plugin, and it has already been used in some research:<br>
Jordal, K et al. (2021). Effects of an individualized training course on technical quality and periapical status of teeth treated endodontically by dentists in the Public Dental Service in Norway. An observational intervention study. International Endodontic Journal. ISSN 0143-2885. doi: 10.1111/iej.13669.

But now I try to do it the right way, using Maven, Git, Eclipse for maintaining the plugin. Sadly there is something wrong. When I run the EndoStarter.java for testing the Plugin I get a Class not found error. But the Class-file is in the target folder. I also get an error on declaring OvalRoi in the main file.

I would be so happy if anybody could give me some advice on that

# Purpose of the file:
The Plugin is used to mark landmarks as apex and stores the coordinates in a csv file for further analysis. There are also radiobuttons and free text fields for qualitative observations as PAI stored to the same csv-file. I will add a better description when I get the program to work.

# Error:
I still get this error when starting the plugin from Eclipse or copy the class files to the local ImageJ plugin folder:

Class not found while attempting to run "Endodontic_measurements"
java.lang.NoClassDefFoundError: Endodontic_measurements (wrong name: no/uio/odont/imagej/ Endodontic_measurements)

I think this is because of the definition in the prom file:
<properties>
	<package-name>no.uio.odont.imagej</package-name>                   
	<main-class>no.uio.odont.imagej.Endodontic_Measurements</main-class>        
	<license.licenseName>CC BY 4.0</license.licenseName>                      
	<license.copyrightOwners>Faculty of dentistry, University of Oslo</license.copyrightOwners> </properties>
	

