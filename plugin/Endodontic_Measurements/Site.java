/****************************************************************************************** 
*  Holds the site of the site and the site of the normal from the site
*  intersecting with the main axis. Provides formatted output of coordinates.
*/

//package no.uio.odont;

import java.awt.*;
import java.text.*;

import ij.gui.*;
import ij.measure.*;

class Site {
	Point site;
	DecimalFormat formatter;
	Calibration calibration;
	Color color;
	
	Site (Point site, Color color, DecimalFormat formatter, Calibration calibration) {
			this.site = site;
			this.color = color;
			this.formatter = formatter;
			this.calibration = calibration;
	}
	
	public PointRoi siteAsRoi() {
			return new PointRoi(site.getX(), site.getY());
	}
	
	public double getX() {
			return site.getX();
	}
	
	public double getY() {
			return site.getY();
	}
	// Returns coordinates converted to image units
	public String coordinatesToString() {
			return formatter.format(calibration.getX(site.getX())) + "; " 
			+ formatter.format(calibration.getY(site.getY())) + "; ";
	}
	
	public Color getColor() {
			return color;
	}
	
	static String missing() {
			return "X;X;";		
	}
}


