// For testing the plugin
package no.uio.odont.imagej;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;

public class EndoStarter {

	public static void main(String[] args) {
		new ImageJ();
	    ImagePlus image = IJ.openImage("M:\\GitHub\\EndodonticMeasurements\\src\\main\\resources\\sample.tif"); // has to be replaced with local image
	    //IJ.runPlugIn(image, "Endodontic_Measurements", null);
	    image.show();	    
	    WindowManager.addWindow(image.getWindow());
	    IJ.runPlugIn(image, "Endodontic_Measurements", null);
	}

}