/**********************************************************************************************************************************
Endodontic Measurements:
Plugin for registration of sites and qualitative observations in periapical radiographs obtained under endodontic treatment
The plugin can be used to measure extracted teeth in  the basic version or periapical radiographs in the
extended version. The variable extended_version has to be set for choice of version.
***********************************************************************************************************************************
The sites are:
		(basic/booth versions)
		Apex (Radiographic Root Apex)
		Apex gutta-percha
		Root canal deviation 
		Root canal entrance center
		Side of root canal 1 mm from site 2 (M/D)
		Side of root canal 4 mm from site 2 (M/D)

		(extended version only)
		Lesion periphery
		Bone level (M/D)
		Cemento-enamel junction (CEJ) (M/D)
		Lesion extention (horizontal) (M/D)

Sites with (M/D) are set on booth mesial and distal side.		

The qualitative observations are:
		Periapical index (PAI) (NS, 1 - 5)
		Apical voids (NS, Y, N)
		Coronal voids (NS, Y, N)
		Orifice plug (NS, Y, N)
		Apical file fracture (NS, Y, N)
		Coronal file fracture (NS, Y, N)
		Apical perforation (NS, Y, N)
		Coronal perforation (NS, Y, N)
		Post (NS, Y, N)
		Restoration gap (NS, Y, N)
		Caries (NS, None, Dentine, Pulp space)
		Restoration (NS, None, Filling, Crown/bridge)
		Support/load (NS, Two appr, One appr, No appr, Bridge abutment)

		
Where Y=yes, N = no, NS = not scored
***********************************************************************************************************************************
Output:
1) Copy of image file with measure sites and lines burnt in, 
   file name: Measured-<timestamp>-<original filename>.tif

2) Resultfile in same diretory as imagefiles with one line per tooth,
 format:
<folder name/imagefilename>;<timestamp>;<EXIF-unit>;  
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
		 
***********************************************************************************************************************************
The plugin was created for a endodontic research project at the Faculty of dentistry, University of Oslo
The program is not thoroughly tested for errors and side effects. The program is not optimized and the code
is not very beautiful.
***********************************************************************************************************************************
License and disclaimers:
Endodontic Measurements plugin
Copyright (C) 2022  Gerald Torgersen

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License version 3 as published by
	the Free Software Foundation.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
***********************************************************************************************************************************	
Programmed by Gerald R. Torgersen <gerald@odont.uio.no>
Faculty of dentistry, University of Oslo, Norway

Version 1.3 2022.02.01
- plugin checks now if resultfile is locked for writing
**********************************************************************************************************************************/

package no.uio.odont.imagej;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import ij.plugin.frame.*;
import ij.plugin.BrowserLauncher;
import ij.*;
import ij.process.*;
import ij.gui.*;
import javax.swing.*;

import ij.io .*;
import ij.measure.*;
import java.util.*;
import java.text.*;


public class Endodontic_Measurements extends PlugInFrame implements ActionListener {

	private final boolean debug = false; // Shows debug messages when set

	Panel panel;
	ImagePlus imp;
	Frame instance;
	ButtonGroup qNumber, tNumber, rNumber, pAI;
	ButtonGroup [] qualitativeYNOptionsButtonG, qualitativeOtherOptionsButtonG; 
	ButtonGroup [] singleSitesButtonG;
	ButtonGroup [][] MDSitesButtonG;
	JTextField comments;

	
	Roi roi;

	
	// The commands:
	final String [] singleSitesNames = {"Apex", "Apex GP", "Canal deviation", "Canal entrance c.", "Lesion periphery"};
	final String [] MDSitesNames = {"Lesion side", "Bone level", "CEJ", "C. s. 1 mm", "C. s. 4 mm"};
	final String [] qualitativeYNObservations = {"Apical voids", "Coronal voids", "Orifice plug", "Apical file fracture", "Coronal file fracture", "Apical perforation", "Coronal perforation", "Post", "Restoration gap"};
	final String [][] qualitativeOtherObservations = {{"Caries", "NS", "None", "Dentine", "Pulp space"}, {"Restoration", "NS", "None", "Filling", "Crown/bridge"}, {"Support/load", "NS", "Two appr", "One appr", "No appr",  "Bridge abutment"}};
	final String [] commands = {"Save canal data", "Save and close"};
	final String [][] colorNames = {{"0xFF0000", "0xFF9999"}, {"0xFF8000", "0xFFCC99"}, {"0xFFFF00", "0xFFFFCC"}, {"0x00FF00", "0xCCFFCC"}, {"0x00FFFF", "0xCCFFFF"}, {"0x0000FF", "0x9999FF"}, {"0xFF00FF", "0xFFCCFF"}, {"0x660000", "0x990000"}, {"0x006600", "0x00CC00"}, {"0x663399", "0xCC99FF"}}; 
	final Color [][] colors = new Color[2][colorNames.length]; 

	final String resultFileName = "MeasurementResults.csv";

	int numberOfSingleSites, numberOfMDSites;

	String fileSeparator;

	private final char decimalFormatSymbol = ','; // Has to be changed in other regions
	
	Root root;

	Color background;

	private static final long serialVersionUID = 4723442002956227761L;


	String aboutMessage;

	public Endodontic_Measurements() {
			super("Endodontic_Measurements");
			if (instance!=null) {
					instance.toFront();
					return;
			}
	}
			

	public void run(String arg) {
		try{
			if (arg.equals("about"))
					{showAbout(); return;}
			
			instance = this;
			instance.setAlwaysOnTop(true); 

			

			background = getBackground(); //new Color(240, 240, 240);
			instance.setBackground(background);
			imp = WindowManager.getCurrentImage();
			if (imp==null) {
					IJ.beep();
					IJ.noImage();
					return;
			}
			
			fileSeparator = Prefs.getFileSeparator();
			
			ImageConverter ic = new ImageConverter(imp);
			ic.convertToRGB();

			
			numberOfSingleSites = singleSitesNames.length;
			numberOfMDSites = MDSitesNames.length;

			
			// Set colors
			for (int i = 0; i < colorNames.length; i++) {
					colors[0][i] = Color.decode(colorNames[i][0]);
					colors[1][i] = Color.decode(colorNames[i][1]);
			}		  
			
			addMenu();
			panel = new Panel(new GridBagLayout());
			panel.setBackground(background);

			
			// Panel layout:
			GridBagConstraints c = new GridBagConstraints();
			c.fill = java.awt.GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;

			//c.weightx = 1.0;


			JPanel selectorPanel = makeSelectorPanel();
			panel.add(selectorPanel, c);

			//c.fill = java.awt.GridBagConstraints.HORIZONTAL;
			c.gridy++;
			//c.fill = java.awt.GridBagConstraints.HORIZONTAL;
			panel.add(addSitesPanel(), c);

			c.gridy++;
			//c.fill = java.awt.GridBagConstraints.NONE;
			//c.anchor = GridBagConstraints.NORTHWEST;
			panel.add(makeQualitativeYNOptionsPanel(qualitativeYNObservations), c);

			c.gridy++;
			panel.add(makeQualitativeOtherOptionsPanel(qualitativeOtherObservations), c);

			c.gridy++;
			panel.add(makeCommentLine(), c);
			
			c.gridx = 0;
			//c.gridwidth = 1;
			for (int i = 0; i < commands.length; i ++) {
				c.gridy++;
				Button b = new Button(commands[i]);
				b.setActionCommand(commands[i]);
				b.addActionListener(this);
				panel.add(b, c);
			}
			
			//Place plugin on the right top side of picture window, slightly overlapping
			Window pictureWindow = imp.getWindow();
			Point loc = pictureWindow.getLocation();
			loc.x = (int)(loc.x + pictureWindow.getWidth()*0.9);
			this.setLocation(loc); 
			
			
			
			add(panel);
			pack();
			setVisible(true);
			IJ.setTool("point");
			Roi.setColor(Color.blue);
		} catch (Exception ex) {IJ.log(ex.toString());}

	}


	
	// Reads the heading information of this file from jar-package and displays it
	void showAbout() {
			if (aboutMessage == null) {
					StringBuffer sB = new StringBuffer();
					try {
							BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("Endodontic_Measurements.java")));
							String currentLine = reader.readLine(); // Throw first line of /*
							while ((currentLine = reader.readLine()) != null && currentLine.indexOf("*/") < 0)
									sB.append(currentLine + "\n");
					} catch (IOException ex) {IJ.log(ex.toString());}
					aboutMessage = sB.toString();
			}
			IJ.showMessage("About Endodontic Measurements plugin", aboutMessage);
	}
	
	

	public void actionPerformed(ActionEvent e) {
			String command = "";
			
			
			roi = imp.getRoi();
			
			if (imp==null) {
					IJ.beep();
					IJ.showStatus("No image");
					return;
			}
			
			
			command = e.getActionCommand(); 
			
			dM("Command: " + command);
	
			// Sites:
			for (int i = 0; i < numberOfSingleSites; i++) {
					if (command.equals(singleSitesNames[i])) {
							setSite(roi, command, colorNames[i]);
							// Draw arcs for placement of canal diameter sites:
							if (i==1) {
									root.drawArcs(singleSitesNames[i]);
									IJ.setTool("point");
									imp.updateAndDraw();
							}
							return;
					}
			}
			for (int i = 0; i < numberOfMDSites; i++) {
				if (command.startsWith(MDSitesNames[i])) {
						setSite(roi, command, colorNames[numberOfSingleSites + i]);
						return;
				}
			}

					//Missing button clicked, reomve site if existing:
			if (command.startsWith("Missing"))
				root.removeIfExist(command.substring(8));

			// Qualitative observations:
			if (command.startsWith("QO_")) 
				setQualitativeObservation(command);		

			// Quadrant- tooth- or rootnumber changed:
			if (command.startsWith("QTR_")) 
				setQuadrantTeethRootNumber(command);
			
			// Save root/save and exit
			if (command.equals(commands[0])) {
					// next root
				if (root != null) 
					registerRootAndReset();
					
				imp.updateAndDraw(); 
			}
			else if (command.equals(commands[1])) {
					// write root to file if not done:
				if (root != null) 
						registerRootAndReset();
				dM("Saving image copy with sites to: " + getWorkingDirectory() + "Measured_" + timeStamp() + "-" + getStrippedFileName() + ".tif");
				IJ.saveAs(imp, "tif", getWorkingDirectory() + "Measured_" + timeStamp() + "-" + getStrippedFileName());
				// close program and file:
				imp.close(); // close picture
				close();
			}


			// XXX- add actions for menu and help
			
			imp.updateAndDraw();
			
			
	}

	// Sets site, creates root object if necessary
	private void setSite(Roi roi, String command, String [] c) {
		updateOrCreateRoot();
		root.setSite(roi, command, c);
	}

	// Sets qualitative observation, creates root object if necessary
	private void setQualitativeObservation(String command) {
		updateOrCreateRoot();
		String observation = command.substring(3, command.indexOf(";"));
		String score = command.substring(command.indexOf(";") + 1);
		dM(observation + " = " + score);
		root.setQualitativeObservation(observation, score);
			
	}

	private void setQuadrantTeethRootNumber(String command) {
		updateOrCreateRoot();
		if (command.startsWith("QTR_qNumber"))
			root.setQuadrantNumber(getNumberInput(qNumber));
		else if (command.startsWith("QTR_tNumber"))
			root.setToothNumber(command.substring(command.lastIndexOf(';') + 1));
		else if (command.startsWith("QTR_rNumber"))
			root.setRootName(command.substring(command.lastIndexOf(';') + 1));
//XXX
					
	}


	// Create root object if root = null
	private void updateOrCreateRoot() {
			dM("updateOrCreateRoot called");
			if (root == null) 
					root = new Root(imp, decimalFormatSymbol);
			dM("" + root.isIdentified());
			
	}



	/*******************************************************************************************************************************
	 *  Saving of results
	 *******************************************************************************************************************************/
	

	/* Performs measurements on tooth, saves data and resets system for
	   measurements on next tooth
	*/
	private void registerRootAndReset() {
		
		// Check that resulfile is not locket and root is identified:
		File f = new File(getRootDirectory() + resultFileName);			
		if (!f.canWrite()) {
			instance.setAlwaysOnTop(false);
			IJ.error("Cannot write to result file, please check if the file is open in another program");
			instance.setAlwaysOnTop(true);
			return;
		} else if (!root.isIdentified()) {
			instance.setAlwaysOnTop(false);
			IJ.error("Error", "You have to identify object (select quadrant- tooth- and root number) before saving");
			instance.setAlwaysOnTop(true);
			return;
		}
	
		root.removeArcs();
	
		// write line to file
		root.burnInSites();

		// Create result string:
		String result = getDirectoryAndFile() + ";" + timeStamp() + "; " + root.toString() + qualitativeObservations() + getCoordinates() + "; " + comments.getText();
			
		// Write to file and reset
		dM("Writing data to file: " + getRootDirectory() + resultFileName);
		IJ.append(result, getRootDirectory() + resultFileName); 
		resetGUI();
		root = null;
	}
	
	
	/* Generates the line of coordinates, has to be hardcoded
	if sites are changed in program */
	private String getCoordinates() {
			StringBuffer coordinates = new StringBuffer();

			for (int i = 0; i < numberOfSingleSites; i++) 
					coordinates.append(root.siteCoordinatesToString(singleSitesNames[i]));

			for (int i = 0; i < numberOfMDSites; i++) {
					coordinates.append(root.siteCoordinatesToString(MDSitesNames[i] + "M"));
					coordinates.append(root.siteCoordinatesToString(MDSitesNames[i] + "D"));
			}
			
			return coordinates.toString();
	}

	
	// Generate string of qualitative observations for output
	private String qualitativeObservations() {
			StringBuffer sB = new StringBuffer();
			

			// Periapical index:
			sB.append(root.qualitativeObservationsToString("pAi") + "; ");
			
			// Y/N qualitative observations:
			for (int i = 0; i < qualitativeYNObservations.length; i++) {
					sB.append(root.qualitativeObservationsToString(qualitativeYNObservations[i]) + "; ");
			}
			// Other qualitative observations:
			for (int i = 0; i < qualitativeOtherObservations.length; i++) {
					sB.append(root.qualitativeObservationsToString(qualitativeOtherObservations[i][0]) + "; ");
			}

			
			
			dM(sB.toString());
			return sB.toString();
	}


	

	/*******************************************************************************************************************************
	 *  The GUI building helper methods
	 *******************************************************************************************************************************/



	// Add textfield for optional comment:
	private JPanel makeCommentLine() {
		JPanel p = new JPanel(new java.awt.GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
   	 	c.gridy = 0;
		c.fill = java.awt.GridBagConstraints.BOTH;
		c.anchor = java.awt.GridBagConstraints.NORTHWEST;
		p.add(new JLabel("Comments: "), c);
		c.gridx = 1;
		comments = new JTextField(45);
		p.add(comments, c);
		return p;
	}
			
	private JPanel makeQualitativeOtherOptionsPanel(String [][] items) {
		
		JPanel p = new JPanel(new java.awt.GridBagLayout());
		qualitativeOtherOptionsButtonG = new ButtonGroup[items.length];
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
   	 	c.gridy = 0;
		c.fill = java.awt.GridBagConstraints.BOTH;
		c.anchor = java.awt.GridBagConstraints.NORTHWEST;
		
		JRadioButton rb;
	
		for (int i = 0; i < items.length; i++) {
			qualitativeOtherOptionsButtonG[i] = new ButtonGroup();
			c.gridx = 0;
				p.add(new JLabel(items[i][0] + ": "), c);
			for (int j = 1; j < items[i].length; j++) {
				c.gridx = j;
				//if (j == 5) {
				//	c.gridy++;
				//	c.gridx = 1;
				//}
				rb = new JRadioButton(items[i][j]);
				rb.setActionCommand("QO_"+ items[i][0] + ";" + items[i][j]);
				rb.addActionListener(this);
				rb.setSelected(j == 1);
				qualitativeOtherOptionsButtonG[i].add(rb);
				p.add(rb, c);
				
			}
			c.gridy++;
		}
		return p;
	}


	private JPanel makeQualitativeYNOptionsPanel(String [] items) {
		String [] options = {"NS", "N", "Y"};
		JPanel p = new JPanel(new java.awt.GridBagLayout());
		//p.setBackground(background);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
   	 	c.gridy = 0;
		c.fill = java.awt.GridBagConstraints.NONE;
		c.anchor = java.awt.GridBagConstraints.NORTHWEST;
		
		JRadioButton rb;
		pAI = new ButtonGroup();
		qualitativeYNOptionsButtonG = new ButtonGroup[items.length];

		// PAI:
		p.add(new JLabel("PAI:"), c);
		c.gridx = 1;
		rb = new JRadioButton("NS");
		rb.setActionCommand("QO_pAi;NS");
		rb.addActionListener(this);
		rb.setSelected(true);
		pAI.add(rb);
		p.add(rb, c);
		for (int i = 1; i <= 5; i++) {
			c.gridx = i + 1;
			rb = new JRadioButton("" + i);
			rb.setActionCommand("QO_pAi;" + i);
			rb.addActionListener(this);
			pAI.add(rb);
			p.add(rb, c);
		}

		// Other options
		for (int i = 0; i < items.length; i++) {
			c.gridy++;
			qualitativeYNOptionsButtonG[i] = new ButtonGroup();
			//panel.add(makeQualitativeScorePanel(items[i] , "QO_" + items[i], options), c);
			c.gridx = 0;
				p.add(new JLabel(items[i] + ": "), c);
			for (int j = 0; j < options.length; j++) {
				c.gridx = j + 1;
				rb = new JRadioButton(options[j]);
				rb.setActionCommand("QO_"+ items[i] + ";" + options[j]);
				rb.addActionListener(this);
				rb.setSelected(j == 0);
				qualitativeYNOptionsButtonG[i].add(rb);
				p.add(rb, c);
			}
		}
		
		return p;
	}

		   
	public JPanel makeSelectorPanel() {
		JPanel p = new JPanel(new GridBagLayout());
		qNumber = new ButtonGroup(); 
		tNumber = new ButtonGroup();
		rNumber = new ButtonGroup();
		JRadioButton rb;
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
   	 	c.gridy = 0;
		c.fill = java.awt.GridBagConstraints.BOTH;
		c.anchor = java.awt.GridBagConstraints.WEST;
		p.add(new JLabel("Quadrant number:"), c);
		for (int i = 1; i <= 4; i++) {
			c.gridx = i;
			rb = new JRadioButton("" + i);
			rb.setActionCommand("QTR_qNumber;" + i);
			rb.addActionListener(this);
			qNumber.add(rb);
			p.add(rb, c);
		}
		
		c.gridx = 0;
		c.gridy = 1;
		p.add(new JLabel("Tooth number:"), c);
		for (int i = 1; i <= 8; i++) {
			c.gridx = i;
			rb = new JRadioButton("" + i);
			rb.setActionCommand("QTR_tNumber;" + i);
			rb.addActionListener(this);
			tNumber.add(rb);
			p.add(rb, c);
		}
		c.gridx = 9;
		rb = new JRadioButton("X");
		rb.setActionCommand("QTR_tNumber;X");
		rb.addActionListener(this);
		tNumber.add(rb);
		p.add(rb, c);
		
		String [] rootNames = {"1", "B", "L", "M", "D", "MB", "ML", "DB", "DL", "X"};

		c.gridx = 0;
		c.gridy = 2;
		p.add(new JLabel("Root:"), c);
		for (int i = 0; i < rootNames.length; i++) {
			c.gridx = i + 1;
			rb = new JRadioButton(rootNames[i]);
			rb.setActionCommand("QTR_rNumber;" + rootNames[i]);
			rb.addActionListener(this);
			rNumber.add(rb);
			p.add(rb, c);
		}
		return p;
	}

	private JPanel addSitesPanel () {
			JPanel p = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			JToggleButton rb;
			
			c.fill = java.awt.GridBagConstraints.BOTH;
			c.anchor = java.awt.GridBagConstraints.WEST;
			c.gridwidth = 1;
			singleSitesButtonG = new ButtonGroup[numberOfSingleSites];
			MDSitesButtonG = new ButtonGroup[MDSitesNames.length][2];

			
			// Single sites:
			for (int i = 0; i < numberOfSingleSites; i++) {
   	 			c.gridy = i;
				singleSitesButtonG[i] = new  ButtonGroup();

				c.gridx = 0;
				c.gridwidth = 1;
				Label colorLabel = new Label("");
				colorLabel.setBackground(colors[0][i]);
				p.add(colorLabel, c);

				c.gridx = 1;
				c.gridwidth = 2;
				rb = new JToggleButton(singleSitesNames[i]);
				rb.setActionCommand(singleSitesNames[i]);
				rb.addActionListener(this);
				singleSitesButtonG[i].add(rb);
				p.add(rb, c);

				c.gridx = 3;
				rb = new JToggleButton("Missing");
				rb.setActionCommand("Missing " + singleSitesNames[i]);
				rb.addActionListener(this);
				rb.setSelected(true);
				singleSitesButtonG[i].add(rb);
				p.add(rb, c);

				c.gridx = 5;
				c.gridwidth = 1;
			   	colorLabel = new Label("");
				colorLabel.setBackground(colors[0][i]);
				p.add(colorLabel, c);					   
			}

			c.gridwidth = 2;
			c.gridy = numberOfSingleSites + 1;


			// Mesial/distal sites:
			MDSitesButtonG = new ButtonGroup[MDSitesNames.length][2];
			c.gridx = 1;
			p.add(new Label("Mesial:", Label.CENTER), c);

			c.gridx = 3;
			p.add(new Label("Distal:", Label.CENTER), c);
			c.gridwidth = 1;
			
		   
			for (int i = 0; i < numberOfSingleSites; i++) {  // BUG - rett opp!! XXX
   	 			c.gridy++;
				MDSitesButtonG[i] = new  ButtonGroup[2];
				MDSitesButtonG[i][0] = new ButtonGroup();
				MDSitesButtonG[i][1] = new ButtonGroup();
				
				c.gridx = 0;
				Label colorLabel = new Label("");
				colorLabel.setBackground(colors[0][numberOfSingleSites + i]);
				p.add(colorLabel, c);

				c.gridx = 1;
				rb = new JToggleButton(MDSitesNames[i]);
				rb.setActionCommand(MDSitesNames[i] + "M");
				rb.addActionListener(this);
				MDSitesButtonG[i][0].add(rb);
				p.add(rb, c);

				c.gridx = 2;
				rb = new JToggleButton("Missing");
				rb.setActionCommand("Missing " + MDSitesNames[i] + "M");
				rb.addActionListener(this);
				rb.setSelected(true);
				MDSitesButtonG[i][0].add(rb);
				p.add(rb, c);

				c.gridx = 3;
				rb = new JToggleButton(MDSitesNames[i]);
				rb.setActionCommand(MDSitesNames[i] + "D");
				rb.addActionListener(this);
				MDSitesButtonG[i][1].add(rb);
				p.add(rb, c);

				c.gridx = 4;
				rb = new JToggleButton("Missing");
				rb.setActionCommand("Missing " + MDSitesNames[i] + "D");
				rb.addActionListener(this);
				rb.setSelected(true);
				MDSitesButtonG[i][1].add(rb);
				p.add(rb, c);

				c.gridx = 5;
			   	colorLabel = new Label("");
				colorLabel.setBackground(colors[1][numberOfSingleSites + i]);
				p.add(colorLabel, c);					   
			}

			return p;
	}

	   
	

	// Resets all buttons (not the root/tooth/quadrant)
	private void resetGUI () {
			// Remove object selection:
		 	qNumber.clearSelection();
			tNumber.clearSelection();
			rNumber.clearSelection();

			
			// Single sites:
			for (int i = 0; i < numberOfSingleSites; i++)
					resetSitesButtonGroup(singleSitesButtonG[i]);

			// Mesial/distal sites:		
			for (int i = 0; i < numberOfMDSites; i++) 
					resetSitesButtonGroup(MDSitesButtonG[i]);


			// Qualitative options:
			setSelectedRadioButton(pAI, 0);
			
			for (int i = 0; i < qualitativeYNOptionsButtonG.length; i++) 
					setSelectedRadioButton(qualitativeYNOptionsButtonG[i], 0);

			for (int i = 0; i < qualitativeOtherOptionsButtonG.length; i++) 
					setSelectedRadioButton(qualitativeOtherOptionsButtonG[i], 0);

			// Clear comments field:
			comments.setText("");
	}

	private void setSelectedRadioButton(ButtonGroup bg, int index) {
			Enumeration e = bg.getElements();
			JToggleButton tb;
			for (int i = 0; i <= index; i++) {
					tb =  (JToggleButton)e.nextElement();
					tb.setSelected(i == index);
			}
	}
	

	private void resetSitesButtonGroup(ButtonGroup bg) {
			JToggleButton tb;
			Enumeration e = bg.getElements();
			tb =  (JToggleButton)e.nextElement();
			tb.setSelected(false);				
			tb =  (JToggleButton)e.nextElement();
			tb.setSelected(true);		
	}

	void resetSitesButtonGroup(ButtonGroup [] bg) {
			JToggleButton tb;
			Enumeration e;

			for (int i = 0; i < bg.length; i++) {
					e = bg[i].getElements();
					tb =  (JToggleButton)e.nextElement();
					 tb.setSelected(false);
					 tb =  (JToggleButton)e.nextElement();
					 tb.setSelected(true);
			}

	}



	private void addMenu () {
		//BrowserLauncher bL = new BrowserLauncher();
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Menu");
		MenuItem help = new MenuItem("Help");
		help.addActionListener(this);
		MenuItem about = new MenuItem("About plugin");
		about.addActionListener(this);
		MenuItem web = new MenuItem("Plugin's homepage");
		web.setActionCommand("web");
		web.addActionListener(this);
		menu.add(help);
		menu.add(about);
		menu.add(web);
		menuBar.add(menu);
		this.setMenuBar(menuBar);

		GenericDialog helpDialog = new GenericDialog("Help");
		String helpText = 	"Help is on its way ..."; 
			helpDialog.addMessage(helpText);
	
		GenericDialog aboutDialog = new GenericDialog("About plugin");
		String aboutText = "Endodontic measurements plugin\n" +
		"Copyright (C) 2014  Gerald Torgersen\n \n"+
		"This program is free software: you can redistribute it and/or modify it under the terms of the\nGNU General Public License version 3 as published by the Free Software Foundation.\n" +
		"This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;\nwithout even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\nSee the GNU General Public License for more details.\n" +
		"You should have received a copy of the GNU General Public License along with this program.\nIf not, see <http://www.gnu.org/licenses/>.\n \n" +   
		"Programmed by Gerald R. Torgersen <gerald@odont.uio.no>\nFaculty of dentistry, University of Oslo, Norway\nVersion 1.0 09-30-2013";
		aboutDialog.addMessage(aboutText);
		setMenuBar(menuBar);
	
}


	/*******************************************************************************************************************************
	 *  Other helper methods
	 *******************************************************************************************************************************/
	
	private String getWorkingDirectory() {
			FileInfo fi = imp.getOriginalFileInfo();
			return fi.directory;
	}
	
	private String getStrippedFileName() {
			FileInfo fi = imp.getOriginalFileInfo();
			String file = fi.fileName;
			// remove extention:
			int ex = file.lastIndexOf(".");
			return file.substring(0, ex);
	}
	
	/* Returns directory leaf node and filename to identify the picture
	   in the measurements result file */
	private String getDirectoryAndFile() {
			return getWorkingDirectory() + imp.getOriginalFileInfo().fileName;		
	}

	// Directory over directories containing patient images:
	private String getRootDirectory() {
			String dir = getWorkingDirectory();
			int leaf = dir.substring(0, dir.length() - 1).lastIndexOf(fileSeparator);
			return dir.substring(0, leaf + 1);
	}
	
	private String timeStamp() {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-H.mm");
			Date today = new Date();
			return formatter.format(new java.sql.Timestamp(today.getTime()));
	}
	

	// Extracts number at the end of the buttons command
	private int getNumberInput(ButtonGroup bG) {
			ButtonModel bM = bG.getSelection();
			String command = bM.getActionCommand().trim();
			return Integer.parseInt(command.substring(command.indexOf(";") + 1));

	}

	// Message to low window in debug modus
	private void dM(String message) {
			if(debug) IJ.log(message);
	}
	
	// main method for debugging and development
	public static void main(String[] args) {
		new ImageJ();
	    ImagePlus image = IJ.openImage("M:\\GitHub\\EndodonticMeasurements\\src\\main\\resources\\sample.tif"); // has to be replaced with local image
	    //IJ.runPlugIn(image, "Endodontic_Measurements", null);
	    image.show();	    
	    WindowManager.addWindow(image.getWindow());
	    IJ.runPlugIn(image, "Endodontic_Measurements", null);
	}
	
}

/*************************************************************************************************************************
* Holds data and methods for measurements of a single tooth 
**************************************************************************************************************************/
class Root {
	
	ImagePlus imp;
	RoiManager rm;		
	
	int quadrantNumber;
	String rootName, toothNumber;
	
	Calibration calibration;
	DecimalFormat formatter; 

	final int near = 1;
	final int far = 4;
	
	Hashtable <String, Site> sites;
	Hashtable <String, String> qualitativeObservations;
	
	public Root (ImagePlus imp, char decimalFormatSymbol) {
			
			this.imp = imp;
			
			// Get calibration and unit, initialize formatter:
			calibration = imp.getCalibration();
			if (calibration.getUnit().equals("pixels"))
					formatter = new DecimalFormat("####");
			else
					formatter = new DecimalFormat("0.00");
			DecimalFormatSymbols dFS = new DecimalFormatSymbols(Locale.US);
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
			//dM("Near: " + calibration.getRawX(near));
			Roi r = circularRoiCenteredAtSite(x, y, 2 * calibration.getRawValue(near));
			addToRoiManager(r, "near", "0xFF0000");

			r = circularRoiCenteredAtSite(x, y, 2 * calibration.getRawValue(far));
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
			r.setColor(Color.decode(color));
			imp.setRoi(r);
			rm.runCommand("Add", color, 0);		
	}
	
	
	private int indexOfRoi(String roiName) {
			int n = rm.getCount();
			for (int i = 0; i < n; i++)
					if (rm.getName(String.valueOf(i)).equals(roiName))
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
			Enumeration keys = sites.keys();
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
	
	
}




/****************************************************************************************** 
*  Holds the site of the site and the site of the normal from the site
*  intersecting with the main axis. Provides formatted output of coordinates.
*/

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


