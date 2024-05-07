//package no.uio.odont;

/*************************************************************************************************************************
* Holds data and methods for measurements of a single tooth 
**************************************************************************************************************************/

import java.awt.*;

import ij.plugin.frame.*;
import ij.*;
import ij.process.*;
import ij.gui.*;

import ij.measure.*;
import java.util.*;
import java.text.*;


class Root {
	
	private boolean debug; // Shows debug messages when set
	
	ImagePlus imp;
	RoiManager rm;		
	
	int quadrantNumber;
	String rootName, toothNumber;
	
	Calibration calibration;
	DecimalFormat formatter; 

	// Diameters of circles indicating distance from AGP
	final int NEAR = 2; // 1 mm from AGP
	final int FAR = 8; // 2 mm from AGP
	
	Hashtable <String, Site> sites;
	Hashtable <String, String> qualitativeObservations;
	
	public Root (ImagePlus imp, char decimalFormatSymbol, boolean debug) {
			
			this.imp = imp;
			debug = debug;
			
			// Get calibration and unit, initialize formatter:
			calibration = imp.getCalibration();
			if (calibration.getUnit().equals("pixels"))
					formatter = new DecimalFormat("####");
			else
					formatter = new DecimalFormat("0.00");
			dM(calibration.toString());
			dM("Calibratet value from 1: " + calibration.getCValue(1));
			
			DecimalFormatSymbols dFS = new DecimalFormatSymbols();
			dFS.setDecimalSeparator(decimalFormatSymbol);
			formatter.setDecimalFormatSymbols(dFS);
			
			quadrantNumber = -1;
			toothNumber = "-1";
			rootName = "-1";

			
			// Initialize roimanager:
			if (RoiManager.getInstance() != null) {
					rm = RoiManager.getInstance();
					emptyRoiManager();
			} else 
					rm = new RoiManager();
			rm.setVisible(false);
			rm.runCommand("Show All");
			
			sites = new Hashtable<String, Site> ();
			qualitativeObservations = new Hashtable<String, String> ();

			
	}
	
	public String toString() {
			return calibration.getUnit() + "; " + quadrantNumber + "; " + toothNumber + "; " + rootName + "; ";
	}
			

	
	// Update root and tooth information:
	public void setQuadrantNumber(int number) {
			quadrantNumber = number;
	}

	public void setToothNumber(String number) {
			toothNumber = number;
	}

	public void setRootName(String name) {
			//IJ.log(name);
			rootName = name;
	}


	// Set sites and qualitative observations
	public void setSite(Roi roi, String name, String [] colorNames) {
			//dM("Adding: " + name);
			removeIfExist(name); 
			Point p = new Point((int)roi.getBounds().getX(), (int)roi.getBounds().getY());
			if (name.trim().endsWith("M")) {
					addToRoiManager(roi, name, colorNames[0]);
					sites.put(name, new Site(p, Color.decode(colorNames[0]), formatter, calibration));
			} else {
					addToRoiManager(roi, name, colorNames[1]);
					sites.put(name, new Site(p, Color.decode(colorNames[1]), formatter, calibration));
			}
	}
	
	public void setQualitativeObservation(String observation, String score) {
			qualitativeObservations.put(observation, score);
	}


	
	// Helper methods		
	public void removeIfExist(String name) {
			//dM("Removing: " + name);
			int index = indexOfRoi(name);
			if (index != -1) {
					rm.select(index);
					rm.runCommand("Delete");
					sites.remove(name);
			}
	}
	
	public void drawArcs(String siteName) {
			removeArcs();
			
			Site s = sites.get(siteName);
			double x = s.getX();
			double y = s.getY();
			Roi r = circularRoiCenteredAtSite(x, y, calibration.getRawX(NEAR));
			dM("circularRoiCenteredAtSite: " + x + ", " + y + ", " + calibration.getRawX(NEAR));
			//dM("circularRoi diameters: " + 
			addToRoiManager(r, "near", "0xFF0000");

			r = circularRoiCenteredAtSite(x, y, calibration.getRawX(FAR));
			addToRoiManager(r, "far", "0xFF0000");
			rm.select(0);		
	}
	
	
	public void removeArcs() {
			removeIfExist("near");
			removeIfExist("far");
	}
	
	private void addToRoiManager(Roi r, String name, String color) {
			color = "#" + color.substring(2); // format for runCommand: "#FF0000"
			r.setName(name);
			Roi.setColor(Color.decode(color));
			imp.setRoi(r);
			rm.runCommand("Add", color, 0);		
	}
	
	
	private int indexOfRoi(String roiName) {
			int n = rm.getCount();
			for (int i = 0; i < n; i++)
					if (RoiManager.getName(String.valueOf(i)).equals(roiName))
							return i;						
			return - 1; // not found
	}
	
	
	/* Adds coordinates of site and coordinates of intersection site of normal
	   with main axis to a string
	*/
	public String siteCoordinatesToString(String siteName) {
			// Check if exist:
			if (sites.containsKey(siteName)) {
					Site s = sites.get(siteName);
					return s.coordinatesToString();
			} else
					return Site.missing();
	}

	public String qualitativeObservationsToString(String qualitativeObservation) {
			// Check if exist:
			if (qualitativeObservations.containsKey(qualitativeObservation))
					return qualitativeObservations.get(qualitativeObservation);
			else
					return "NS";
	}

	
	
	
	public void burnInSites() {
			ImageProcessor imPro = imp.getProcessor();
			Enumeration<String> keys = sites.keys();
			Site s;
			while(keys.hasMoreElements() ) {
					s = sites.get(keys.nextElement());
					imPro.setColor(s.getColor());
					s.siteAsRoi().drawPixels(imPro);
					circularRoiCenteredAtSite(s.getX(), s.getY(), 10).drawPixels(imPro);
			}
	}
	
	public boolean isIdentified() {
		return (quadrantNumber > 0 && !toothNumber.equals("-1") && !rootName.equals("-1"));
	}
	
	/*
	 *  Locates roi with the center coordinate
	 */
	private Roi circularRoiCenteredAtSite(double x, double y, double size) {
			double offset = size/2; 
			Roi r = new OvalRoi(x - offset, y - offset, size, size);
			return r;
	}

	
	private void emptyRoiManager() {
			//dM("Emptying ROI manager");
			if (rm.getCount() > 0) {
					rm.runCommand("Select All");
					rm.runCommand("Delete");
			}
	}
	
	// Message to log window in debug modus
		private void dM(String message) {
				if(debug) IJ.log(message);
		}
	
	
}