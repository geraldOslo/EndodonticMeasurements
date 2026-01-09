
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
The plugin is configured by an optional config file: Endodontic_Measurements.cfg in the same folder as the plugin:
operator:<operator name/ID> if missing: login user ID
decimal-separator:./, if missing uses system default
measurement_store:top/local if missing uses top storage
save_scored_image_copy:true/false if missing true


Output:
1) Copy of image file with measure sites and lines burnt in, 
   file name: Measured-<timestamp>-<original filename>.tif

2) Resultfile in stored in:
	- if top mode choosen: Measurements.csv stored in directory over image directory
	- if local choosen: <image-filename without extension>-measurements.csv in same folder as image file
	One line per root and measurement added to the bottom of the csv if existing.

Format:
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
		 
***********************************************************************************************************************************
The plugin was created for a endodontic research project at the Faculty of dentistry, University of Oslo
The program is not thoroughly tested for errors and side effects. The program is not optimized and the code
is not very beautiful.
***********************************************************************************************************************************
License and disclaimers:
Endodontic Measurements plugin
Copyright (C) 2024  Gerald Torgersen
Creative Commons Attribution 4.0 International Public License: https://creativecommons.org/licenses/by/4.0/legalcode 
***********************************************************************************************************************************	
Programmed by Gerald R. Torgersen <gerald@odont.uio.no>
Faculty of dentistry, University of Oslo, Norway

Version 1.5 2024.06.05
**********************************************************************************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;

import ij.plugin.frame.*;
import ij.*;
import ij.process.*;
import ij.gui.*;
import javax.swing.*;

import ij.io.*;
import java.util.*;
import java.text.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Endodontic_Measurements extends PlugInFrame implements ActionListener {

	private final boolean debug = true; // Shows debug messages when set

	JPanel panel;
	ImagePlus imp;
	Frame instance;
	ButtonGroup qNumber, tNumber, rNumber, iType, pAI;
	ButtonGroup[] qualitativeYNOptionsButtonG, qualitativeOtherOptionsButtonG;
	ButtonGroup[] singleSitesButtonG;
	ButtonGroup[][] MDSitesButtonG;
	JTextField comments;

	Roi roi;

	// The commands:
	final String[] singleSitesNames = { "Apex", "Apex GP", "Canal deviation", "Canal entrance c.", "Lesion periphery" };
	final String[] MDSitesNames = { "Lesion side", "Bone level", "CEJ", "C. s. 1 mm", "C. s. 4 mm" };
	final String[] qualitativeYNObservations = { "Apical voids", "Coronal voids", "Orifice plug",
			"Apical file fracture", "Coronal file fracture", "Apical perforation", "Coronal perforation", "Post",
			"Restoration gap" };
	final String[][] qualitativeOtherObservations = { { "Caries", "NS", "None", "Dentine", "Pulp space" },
			{ "Restoration", "NS", "None", "Filling", "Crown/bridge" },
			{ "Support/load", "NS", "Two appr", "One appr", "No appr", "Bridge abutment" } };
	final String[] commands = { "Save canal data", "Save and close" };
	final String[][] colorNames = { { "0xFF0000", "0xFF9999" }, { "0xFF8000", "0xFFCC99" }, { "0xFFFF00", "0xFFFFCC" },
			{ "0x00FF00", "0xCCFFCC" }, { "0x00FFFF", "0xCCFFFF" }, { "0x0000FF", "0x9999FF" },
			{ "0xFF00FF", "0xFFCCFF" }, { "0x660000", "0x990000" }, { "0x006600", "0x00CC00" },
			{ "0x663399", "0xCC99FF" } };
	final Color[][] colors = new Color[2][colorNames.length];
	final String[] rootNames = { "1", "B", "L", "M", "D", "MB", "ML", "DB", "DL", "X" };
	final String[] imageTypes = { "Preop", "Compl", "Ctrl", "Other" };

	final String resultFileName = "MeasurementResults.csv";

	int numberOfSingleSites, numberOfMDSites;

	// Configerations
	String operator;
	String measurementStore; // top or local
	String imageType;
	char decimalFormatSymbol;
	boolean saveScoredCopy;
	String fileSeparator;

	Root root;

	Color background;

	private static final long serialVersionUID = 4723442002956227761L;

	String aboutMessage;

	public Endodontic_Measurements() {
		super("Endodontic_Measurements");
		if (instance != null) {
			instance.toFront();
			return;
		}
	}

	public void run(String arg) {
		try {
			if (arg.equals("about")) {
				showAbout();
				return;
			}

			configApp();

			instance = this;
			instance.setAlwaysOnTop(true);

			background = getBackground();
			instance.setBackground(background);
			imp = WindowManager.getCurrentImage();
			if (imp == null) {
				IJ.beep();
				IJ.noImage();
				return;
			}

			fileSeparator = Prefs.getFileSeparator();

			ImageConverter ic = new ImageConverter(imp);
			;

			numberOfSingleSites = singleSitesNames.length;
			numberOfMDSites = MDSitesNames.length;

			// Set colors
			for (int i = 0; i < colorNames.length; i++) {
				colors[0][i] = Color.decode(colorNames[i][0]);
				colors[1][i] = Color.decode(colorNames[i][1]);
			}

			addMenu();
			panel = new JPanel(new GridBagLayout());
			panel.setBackground(background);

			// Panel layout:
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1.0;

			JPanel selectorPanel = makeSelectorPanel();
			panel.add(selectorPanel, c);

			c.gridy++;
			c.weightx = 0;
			c.weighty = 1.0;
			panel.add(addSingleSitesPanel(), c);

			c.gridy++;
			panel.add(addMDSitesPanel(), c);

			c.gridy++;
			panel.add(makeQualitativeYNOptionsPanel(qualitativeYNObservations), c);

			c.gridy++;
			panel.add(makeQualitativeOtherOptionsPanel(qualitativeOtherObservations), c);

			c.gridy++;
			c.weighty = 1.0;
			panel.add(makeCommentLine(), c);

			c.gridy++;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.CENTER;
			JPanel buttonPanel = new JPanel(new FlowLayout());
			for (int i = 0; i < commands.length; i++) {
				JButton b = new JButton(commands[i]);
				b.setActionCommand(commands[i]);
				b.addActionListener(this);
				buttonPanel.add(b);
			}
			panel.add(buttonPanel, c);

			// Add the panel to a scroll pane
			JScrollPane scrollPane = new JScrollPane(panel);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			// Add the scroll pane to the frame
			add(scrollPane);
			pack();
			setVisible(true);
			IJ.setTool("point");
			Roi.setColor(Color.blue);
		} catch (Exception ex) {
			IJ.log(ex.toString());
		}
	}

	// Reads the heading information of this file from jar-package and displays it
	void showAbout() {
		if (aboutMessage == null) {
			StringBuffer sB = new StringBuffer();
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(this.getClass().getResourceAsStream("Endodontic_Measurements.java")));
				String currentLine = reader.readLine(); // Throw first line of /*
				while ((currentLine = reader.readLine()) != null && currentLine.indexOf("*/") < 0)
					sB.append(currentLine + "\n");
			} catch (IOException ex) {
				IJ.log(ex.toString());
			}
			aboutMessage = sB.toString();
		}
		IJ.showMessage("About Endodontic Measurements plugin", aboutMessage);
	}

	public void actionPerformed(ActionEvent e) {
		String command = "";

		roi = imp.getRoi();

		if (imp == null) {
			IJ.beep();
			IJ.showStatus("No image");
			return;
		}

		command = e.getActionCommand();

		dM("Command: " + command);
		// Check if the root object is null
		if (root == null) {
			dM("The root object is null.");
		} else {
			dM("The root object is not null.");
		}
		// Sites:
		for (int i = 0; i < numberOfSingleSites; i++) {
			if (command.equals(singleSitesNames[i])) {
				setSite(roi, command, colorNames[i]);
				// Draw arcs for placement of canal diameter sites:
				if (i == 1) {
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

		// Missing button clicked, reomve site if existing:
		if (command.startsWith("Missing"))
			root.removeIfExist(command.substring(8));

		// Qualitative observations:
		else if (command.startsWith("QO_"))
			setQualitativeObservation(command);

		// Quadrant- tooth- or rootnumber changed:
		else if (command.startsWith("QTR_"))
			setQuadrantTeethRootNumber(command);

		// Image type choosen:
		else if (command.startsWith("ImageType")) {
			imageType = command.substring(command.lastIndexOf(';') + 1);
			dM(imageType);
		}
		// Save root/save and exit
		else if (command.equals(commands[0])) {
			// next root
			if (root != null)
				registerRootAndReset();

			imp.updateAndDraw();
		} else if (command.equals(commands[1])) {
			// write root to file if not done:
			if (root != null)
				registerRootAndReset();

			if (saveScoredCopy) {
				dM("Saving image copy with sites to: " + getWorkingDirectory() + "Measured_" + timeStamp() + "-"
						+ getStrippedFileName() + ".tif");
				IJ.saveAs(imp, "tif", getWorkingDirectory() + "Measured_" + timeStamp() + "-" + getStrippedFileName());
			}

			// close program and file:
			imp.close(); // close picture
			close();

			// Force exit to prevent zombie processes in dev/test mode
			System.exit(0);
			return;
		}

		imp.updateAndDraw();

		// activate menus if quadrant, tooth, root and image_type selected
		if (imageType != null && root != null && root.isIdentified())
			activateAllMenus(true);
		comments.setEnabled(true);

	}

	// Sets site, creates root object if necessary
	private void setSite(Roi roi, String command, String[] c) {
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

	}

	// Create root object if root = null
	private void updateOrCreateRoot() {
		dM("updateOrCreateRoot called");
		if (root == null) {
			dM("Creating new root");
			root = new Root(imp, decimalFormatSymbol, debug);
		}
		dM("" + root.isIdentified());

	}

	/*******************************************************************************************************************************
	 * Saving of results
	 *******************************************************************************************************************************/

	/*
	 * Performs measurements on tooth, saves data and resets system for
	 * measurements on next tooth
	 */

	private void registerRootAndReset() {
		dM("registerRootAndReset called");
		Path filePath = null;

		dM("Store config value: " + measurementStore);
		if (measurementStore.equals("top")) {
			// Building the file path for the "top" store using Paths
			String rootDir = getRootDirectory();
			dM("Root Directory: " + rootDir);
			Path rootDirectory = Paths.get(rootDir);
			filePath = rootDirectory.resolve(resultFileName);
		} else if (measurementStore.equals("local")) {
			// Building the file path for the "local" store with a stripped file name
			String workDir = getWorkingDirectory();
			dM("Working Directory: " + workDir);
			Path workingDirectory = Paths.get(workDir);
			String csvFileName = getStrippedFileName() + "-measurements.csv";
			dM("CSV Filename: " + csvFileName);
			filePath = workingDirectory.resolve(csvFileName);

			dM("Resolved CSV File Path: " + filePath.toString());
		}

		// Convert Path to a File object for compatibility with existing code
		File f = filePath.toFile();
		dM("Target File object: " + f.getAbsolutePath());

		// Check if the file exists, if not, create it
		if (!f.exists()) {
			dM("File does not exist. Attempting to create.");
			try {
				// Ensure the parent directory exists
				File directory = f.getParentFile();
				dM("Parent directory: " + directory.getAbsolutePath());
				if (!directory.exists() && !directory.mkdirs()) {
					instance.setAlwaysOnTop(false);
					IJ.error("Cannot create directory for result file: " + directory.getAbsolutePath());
					dM("Failed to create parent directory.");
					instance.setAlwaysOnTop(true);
					return;
				}

				// Create the new file
				if (!f.createNewFile()) {
					instance.setAlwaysOnTop(false);
					IJ.error("Cannot create result file: " + f.getAbsolutePath());
					dM("Failed to create file.");
					instance.setAlwaysOnTop(true);
					return;
				}
				dM("File created successfully.");
			} catch (IOException e) {
				instance.setAlwaysOnTop(false);
				IJ.error("Cannot create result file, error occurred while creating file: " + e.getMessage());
				dM("IOException creating file: " + e.getMessage());
				instance.setAlwaysOnTop(true);
				return;
			}
		} else {
			dM("File already exists.");
		}

		// Check if JVM has write permissions
		if (!f.canWrite()) {
			instance.setAlwaysOnTop(false);
			IJ.error("Cannot write to result file, JVM does not have write permissions");
			dM("No write permissions.");
			instance.setAlwaysOnTop(true);
			return;
		}

		// Check if the file is locked
		try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
			FileLock fl = raf.getChannel().tryLock();
			if (fl == null) {
				instance.setAlwaysOnTop(false);
				IJ.error("Cannot write to result file, file is open in another program");
				dM("File is locked by another program.");
				instance.setAlwaysOnTop(true);
				return;
			}
			fl.release();
		} catch (Exception e) {
			instance.setAlwaysOnTop(false);
			IJ.error("Cannot write to result file, error occurred while attempting to lock file");
			dM("Exception checking lock: " + e.getMessage());
			instance.setAlwaysOnTop(true);
			return;
		}

		dM("Root identification status: " + (root != null ? root.isIdentified() : "root is null"));
		if (root.isIdentified()) {
			root.removeArcs();

			// Write line to the file
			root.burnInSites();

			// Write information and coordinates to the result file
			String result = getDirectoryAndFile() + ";" + timeStamp() + "; " + operator + "; " + imageType + ";"
					+ root.toString() + qualitativeObservations() + getCoordinates() + "; " + comments.getText();

			// Write to the file using ImageJ's append function
			dM("Writing data string to file: " + filePath);
			IJ.append(result, filePath.toString());
			dM("Data written.");

			resetGUI();

			root = null;
		} else {
			dM("Root is not identified. Cannot save.");
			IJ.showMessageWithCancel("Error",
					"You have to identify object (select quadrant- tooth- and root number) before saving");
		}
	}

	/*
	 * Generates the line of coordinates, has to be hardcoded
	 * if sites are changed in program
	 */
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
	 * The GUI building helper methods
	 *******************************************************************************************************************************/

	// Add textfield for optional comment:
	private JPanel makeCommentLine() {
		JPanel p = new JPanel(new java.awt.GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = java.awt.GridBagConstraints.BOTH;
		c.anchor = java.awt.GridBagConstraints.NORTHWEST;
		addLabel(p, "Comments:", c);
		c.gridx = 1;
		comments = new JTextField(30);
		comments.setEnabled(false);
		p.add(comments, c);
		return p;
	}

	private JPanel makeQualitativeOtherOptionsPanel(String[][] items) {

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
			addLabel(p, items[i][0] + ": ", c);
			// p.add(new JLabel(items[i][0] + ": "), c);
			for (int j = 1; j < items[i].length; j++) {
				c.gridx = j;
				rb = new JRadioButton(items[i][j]);
				rb.setActionCommand("QO_" + items[i][0] + ";" + items[i][j]);
				rb.addActionListener(this);
				rb.setSelected(j == 1);
				rb.setEnabled(false);
				qualitativeOtherOptionsButtonG[i].add(rb);
				p.add(rb, c);

			}
			c.gridy++;
		}
		return p;
	}

	private JPanel makeQualitativeYNOptionsPanel(String[] items) {
		String[] options = { "NS", "N", "Y" };
		JPanel p = new JPanel(new java.awt.GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = java.awt.GridBagConstraints.NONE;
		c.anchor = java.awt.GridBagConstraints.NORTHWEST;

		JRadioButton rb;
		pAI = new ButtonGroup();
		qualitativeYNOptionsButtonG = new ButtonGroup[items.length];

		// PAI:
		// p.add(new JLabel("PAI:"), c);
		addLabel(p, "PAI: ", c);
		c.gridx = 1;
		rb = new JRadioButton("NS");
		rb.setActionCommand("QO_pAi;NS");
		rb.addActionListener(this);
		rb.setSelected(true);
		rb.setEnabled(false);
		pAI.add(rb);
		p.add(rb, c);
		for (int i = 1; i <= 5; i++) {
			c.gridx = i + 1;
			rb = new JRadioButton("" + i);
			rb.setActionCommand("QO_pAi;" + i);
			rb.addActionListener(this);
			rb.setEnabled(false);
			pAI.add(rb);
			p.add(rb, c);
		}

		// Other options
		for (int i = 0; i < items.length; i++) {
			c.gridy++;
			qualitativeYNOptionsButtonG[i] = new ButtonGroup();
			// panel.add(makeQualitativeScorePanel(items[i] , "QO_" + items[i], options),
			// c);
			c.gridx = 0;
			addLabel(p, items[i] + ": ", c);
			// p.add(new JLabel(items[i] + ": "), c);
			for (int j = 0; j < options.length; j++) {
				c.gridx = j + 1;
				rb = new JRadioButton(options[j]);
				rb.setActionCommand("QO_" + items[i] + ";" + options[j]);
				rb.addActionListener(this);
				rb.setSelected(j == 0);
				rb.setEnabled(false);
				qualitativeYNOptionsButtonG[i].add(rb);
				p.add(rb, c);
			}
		}

		return p;
	}

	public JPanel makeSelectorPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// Quadrant number panel
		JPanel qPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		qPanel.add(new JLabel("Quadrant number:"));
		qNumber = new ButtonGroup();
		for (int i = 1; i <= 4; i++) {
			JRadioButton rb = new JRadioButton("" + i);
			rb.setActionCommand("QTR_qNumber;" + i);
			rb.addActionListener(this::actionPerformed); // Adjust listener method reference
			qNumber.add(rb);
			qPanel.add(rb);
		}
		mainPanel.add(qPanel);

		// Tooth number panel
		JPanel tPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		tPanel.add(new JLabel("Tooth number:"));
		tNumber = new ButtonGroup();
		for (int i = 1; i <= 8; i++) {
			JRadioButton rb = new JRadioButton("" + i);
			rb.setActionCommand("QTR_tNumber;" + i);
			rb.addActionListener(this::actionPerformed); // Adjust listener method reference
			tNumber.add(rb);
			tPanel.add(rb);
		}
		JRadioButton rbX = new JRadioButton("X");
		rbX.setActionCommand("QTR_tNumber;X");
		rbX.addActionListener(this::actionPerformed); // Adjust listener method reference
		tNumber.add(rbX);
		tPanel.add(rbX);
		mainPanel.add(tPanel);

		// Root number panel
		JPanel rPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		rPanel.add(new JLabel("Root:"));
		rNumber = new ButtonGroup();
		for (String rootName : rootNames) {
			JRadioButton rb = new JRadioButton(rootName);
			rb.setActionCommand("QTR_rNumber;" + rootName);
			rb.addActionListener(this::actionPerformed); // Adjust listener method reference
			rNumber.add(rb);
			rPanel.add(rb);
		}
		mainPanel.add(rPanel);

		// Image type panel
		JPanel iPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		iPanel.add(new JLabel("Image Type:"));
		iType = new ButtonGroup();
		for (String imageType : imageTypes) {
			JRadioButton rb = new JRadioButton(imageType);
			rb.setActionCommand("ImageType;" + imageType);
			rb.addActionListener(this::actionPerformed); // Adjust listener method reference
			iType.add(rb);
			iPanel.add(rb);
		}
		mainPanel.add(iPanel);

		return mainPanel;
	}

	private JPanel addSingleSitesPanel() {
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL; // Use HORIZONTAL if you don't want vertical stretching
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0; // Set weightx to 0 for most components
		c.gridwidth = 1;

		// Initialize ButtonGroups arrays
		singleSitesButtonG = new ButtonGroup[numberOfSingleSites];

		// Initialize singleSitesButtonG array
		singleSitesButtonG = new ButtonGroup[numberOfSingleSites];
		for (int i = 0; i < numberOfSingleSites; i++) {
			singleSitesButtonG[i] = new ButtonGroup(); // Initialize each ButtonGroup in the array
		}

		// Setup for single sites
		for (int i = 0; i < numberOfSingleSites; i++) {
			c.gridy = i;

			// Color label at the start of the row
			c.gridx = 0;
			addColorLabel(p, colors[0][i], c);

			// Toggle button for site names
			c.gridx = 1;
			addToggleButton(p, singleSitesButtonG[i], singleSitesNames[i], c);

			// Toggle button for "Missing"
			c.gridx = 3;
			addToggleButton(p, singleSitesButtonG[i], "Missing", c, "Missing " + singleSitesNames[i]);

			// Color label at the end of the row
			c.gridx = 5;
			addColorLabel(p, colors[0][i], c);
		}

		return p;
	}

	private JPanel addMDSitesPanel() {
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0;
		c.gridwidth = 1;

		// Initialize ButtonGroups arrays
		MDSitesButtonG = new ButtonGroup[MDSitesNames.length][2];
		for (int i = 0; i < MDSitesNames.length; i++) {
			MDSitesButtonG[i][0] = new ButtonGroup(); // Initialize each ButtonGroup for Mesial
			MDSitesButtonG[i][1] = new ButtonGroup(); // Initialize each ButtonGroup for Distal
		}

		// Labels should be in the first row
		c.gridy = 0; // Set this to zero for the first row where labels will be

		c.gridx = 1; // Position for the "Mesial:" label
		p.add(new JLabel("Mesial:"), c);

		c.gridx = 3; // Position for the "Distal:" label
		p.add(new JLabel("Distal:"), c);

		// Setup toggle buttons for each site
		for (int i = 0; i < MDSitesNames.length; i++) {
			c.gridy = i + 1; // Start from the second row and move downwards

			// Color label at the start of the row
			c.gridx = 0;
			addColorLabel(p, colors[1][i], c);

			// Mesial toggle buttons
			c.gridx = 1;
			addToggleButton(p, MDSitesButtonG[i][0], MDSitesNames[i] + "M", c);

			c.gridx = 2;
			addToggleButton(p, MDSitesButtonG[i][0], "Missing", c, "Missing " + MDSitesNames[i] + "M");

			// Distal toggle buttons
			c.gridx = 3;
			addToggleButton(p, MDSitesButtonG[i][1], MDSitesNames[i] + "D", c);

			c.gridx = 4;
			addToggleButton(p, MDSitesButtonG[i][1], "Missing", c, "Missing " + MDSitesNames[i] + "D");

			// Color label at the end of the row
			c.gridx = 5;
			addColorLabel(p, colors[1][i], c);
		}

		return p;
	}

	private void addColorLabel(JPanel panel, Color color, GridBagConstraints c) {
		Label colorLabel = new Label("");
		colorLabel.setBackground(color);
		panel.add(colorLabel, c);
	}

	private void addToggleButton(JPanel panel, ButtonGroup group, String text, GridBagConstraints c) {
		addToggleButton(panel, group, text, c, text);
	}

	private void addToggleButton(JPanel panel, ButtonGroup group, String text, GridBagConstraints c,
			String actionCommand) {
		JToggleButton rb = new JToggleButton(text);
		rb.setActionCommand(actionCommand);
		rb.addActionListener(this);
		rb.setEnabled(false);
		group.add(rb);
		panel.add(rb, c);
	}

	private void addLabel(JPanel panel, String text, GridBagConstraints c) {
		panel.add(new Label(text, Label.CENTER), c);
	}

	// Resets all buttons (not the root/tooth/quadrant)
	private void resetGUI() {
		// Remove object selection:
		qNumber.clearSelection();
		tNumber.clearSelection();
		rNumber.clearSelection();
		iType.clearSelection();

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

		activateAllMenus(false);
		comments.setEnabled(false);
	}

	private void setSelectedRadioButton(ButtonGroup bg, int index) {
		Enumeration<AbstractButton> e = bg.getElements();
		JToggleButton tb;
		for (int i = 0; i <= index; i++) {
			tb = (JToggleButton) e.nextElement();
			tb.setSelected(i == index);
		}
	}

	private void resetSitesButtonGroup(ButtonGroup bg) {
		JToggleButton tb;
		Enumeration<AbstractButton> e = bg.getElements();
		tb = (JToggleButton) e.nextElement();
		tb.setSelected(false);
		tb = (JToggleButton) e.nextElement();
		tb.setSelected(true);
	}

	void resetSitesButtonGroup(ButtonGroup[] bg) {
		JToggleButton tb;
		Enumeration<AbstractButton> e;

		for (int i = 0; i < bg.length; i++) {
			e = bg[i].getElements();
			tb = (JToggleButton) e.nextElement();
			tb.setSelected(false);
			tb = (JToggleButton) e.nextElement();
			tb.setSelected(true);
		}

	}

	private void addMenu() {
		// BrowserLauncher bL = new BrowserLauncher();
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
		String helpText = "Help is on its way ...";
		helpDialog.addMessage(helpText);

		GenericDialog aboutDialog = new GenericDialog("About plugin");
		String aboutText = "Endodontic measurements plugin\n" +
				"Copyright (C) 2024  Gerald Torgersen\n \n" +
				"This program is free software: you can redistribute it and/or modify it under the terms of the\nGNU General Public License version 3 as published by the Free Software Foundation.\n"
				+
				"This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;\nwithout even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\nSee the GNU General Public License for more details.\n"
				+
				"You should have received a copy of the GNU General Public License along with this program.\nIf not, see <http://www.gnu.org/licenses/>.\n \n"
				+
				"Programmed by Gerald R. Torgersen <gerald@odont.uio.no>\nFaculty of dentistry, University of Oslo, Norway\nVersion 1.5 03-05-2024";
		aboutDialog.addMessage(aboutText);
		setMenuBar(menuBar);

	}

	/*******************************************************************************************************************************
	 * Other helper methods
	 *******************************************************************************************************************************/

	/* Set up parameters for the app */
	/* Set up parameters for the app */
	private void configApp() {
		String pluginsDirectory = IJ.getDirectory("plugins");
		dM("IJ.getDirectory('plugins'): " + pluginsDirectory);

		Path configFilePath;

		// Check if running from the ImageJ plugins directory
		if (pluginsDirectory != null) {
			configFilePath = Paths.get(pluginsDirectory, "Endodontic_Measurements", "Endodontic_Measurements.cfg");
			;
		} else if (debug) {
			// Development mode: Use a specific location for the configuration file
			configFilePath = Paths.get("M:", "GitHub", "EndodonticMeasurements", "src", "main", "resources",
					"Endodontic_Measurements.cfg");
		} else {
			configFilePath = null;
		}

		if (configFilePath != null) {
			dM("Resolved Config File Path: " + configFilePath.toString());
			dM("Config File Exists: " + new File(configFilePath.toString()).exists());
		} else {
			dM("Config File Path is null");
		}

		// Set default values for all settings
		String defaultOperator = System.getProperty("user.name");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
		char defaultDecimalFormatSymbol = symbols.getDecimalSeparator();
		String defaultMeasurementStore = "top";

		// Initialize variables with defaults
		operator = defaultOperator;
		decimalFormatSymbol = defaultDecimalFormatSymbol;
		measurementStore = defaultMeasurementStore;

		if (configFilePath == null) {
			dM("Config file path is null, skipping config file load.");
			return;
		}

		File configFile = new File(configFilePath.toString());

		// If a config file path is set, attempt to read the config file
		if (configFile.exists() && configFile.isFile()) {

			try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
				String line;
				String[] parts;

				// Debugging statement to check the operator line
				line = reader.readLine();
				if (line != null) {
					parts = line.split(":");
					if (parts.length > 1 && !parts[1].trim().isEmpty()) {
						operator = parts[1].trim();
					}
				}

				// Debugging statement to check the decimal separator line
				line = reader.readLine();
				if (line != null) {
					parts = line.split(":");
					if (parts.length > 1 && !parts[1].trim().isEmpty()) {
						decimalFormatSymbol = parts[1].trim().charAt(0);
					}
				}

				// Debugging statement to check the storage mode line
				line = reader.readLine();
				if (line != null) {
					parts = line.split(":");
					if (parts.length > 1 && !parts[1].trim().isEmpty()) {
						measurementStore = parts[1].trim();
					}
				}

				// Debugging statement to check the storage mode line
				line = reader.readLine();
				if (line != null) {
					parts = line.split(":");
					if (parts.length > 1 && !parts[1].trim().isEmpty()) {
						saveScoredCopy = Boolean.parseBoolean(parts[1].trim());
					}
				}
			} catch (IOException e) {
				IJ.log("Error reading configuration file: " + e.getMessage());
			}

		}

		// Log the applied settings for verification
		dM("Operator: " + operator);
		dM("Decimal Separator: " + decimalFormatSymbol);
		dM("Measurement Store: " + measurementStore);

	}

	/*****************************************************************************/
	// Single method that activates/deactivates all button groups
	public void activateAllMenus(Boolean activate) {
		// Handle all individual ButtonGroups
		toggleButtonGroup(pAI, activate);

		// Handle all 1D ButtonGroup arrays
		toggleButtonGroupArray(qualitativeYNOptionsButtonG, activate);
		toggleButtonGroupArray(qualitativeOtherOptionsButtonG, activate);
		toggleButtonGroupArray(singleSitesButtonG, activate);

		// Handle all 2D ButtonGroup arrays
		for (ButtonGroup[] buttonGroupArray : MDSitesButtonG) {
			toggleButtonGroupArray(buttonGroupArray, activate);
		}
	}

	// Toggle a single ButtonGroup
	private void toggleButtonGroup(ButtonGroup group, Boolean activate) {
		if (group != null) {
			Enumeration<AbstractButton> buttons = group.getElements();
			while (buttons.hasMoreElements()) {
				buttons.nextElement().setEnabled(activate);
			}
		}
	}

	// Toggle all ButtonGroups in a 1D array
	private void toggleButtonGroupArray(ButtonGroup[] groupArray, Boolean activate) {
		if (groupArray != null) {
			for (ButtonGroup group : groupArray) {
				toggleButtonGroup(group, activate);
			}
		}
	}

	/*****************************************************************************/

	private String getWorkingDirectory() {
		FileInfo fi = imp.getOriginalFileInfo();
		if (fi != null && fi.directory != null) {
			return fi.directory;
		}
		// Fallback to user.dir if image info is missing (common when opening via API)
		return System.getProperty("user.dir") + java.io.File.separator;
	}

	private String getStrippedFileName() {
		FileInfo fi = imp.getOriginalFileInfo();
		String file = "";
		if (fi != null && fi.fileName != null) {
			file = fi.fileName;
		} else {
			if (imp.getTitle() != null) {
				file = imp.getTitle();
			} else {
				file = "unknown.tif";
			}
		}
		// remove extention:
		int ex = file.lastIndexOf(".");
		if (ex > 0) {
			return file.substring(0, ex);
		}
		return file;
	}

	/*
	 * Returns directory leaf node and filename to identify the picture
	 * in the measurements result file
	 */
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

	// Message to log window in debug modus
	private void dM(String message) {
		if (debug)
			IJ.log(message);
	}

	// main method for debugging and development
	public static void main(String[] args) throws URISyntaxException {
		new ImageJ();
		Class<?> clazz = Endodontic_Measurements.class;

		// Set plugins.dir to the 'plugin' directory in the workspace so config can be
		// found
		String userDir = System.getProperty("user.dir");
		String pluginDir = userDir + "/plugin";
		System.setProperty("plugins.dir", pluginDir);

		// Load a local test image
		String imagePath = userDir + "/testimages/AI-patient/LI.tif";
		ImagePlus image = IJ.openImage(imagePath);
		if (image != null) {
			image.show();
			// Manually set file info directly on the ImagePlus object if missing
			if (image.getOriginalFileInfo() == null) {
				FileInfo fi = new FileInfo();
				fi.directory = userDir + "/testimages/AI-patient/";
				fi.fileName = "LI.tif";
				image.setFileInfo(fi);
			}
			WindowManager.addWindow(image.getWindow());
		} else {
			IJ.log("Could not open test image: " + imagePath);
		}

		IJ.runPlugIn(clazz.getName(), "");
	}

}
