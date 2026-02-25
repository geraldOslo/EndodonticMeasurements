package no.uio.odont.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.*;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.plugin.frame.PlugInFrame;
import no.uio.odont.model.MeasurementRoot;
import no.uio.odont.util.AppConfig;

/**
 * Modernized GUI for the Endodontic Measurements plugin.
 * Extends PlugInFrame to integrate with ImageJ.
 * 
 * @author Gerald Torgersen
 * @version 2.0
 * @date January 2026
 */
public class MeasurementUI extends PlugInFrame implements ActionListener {
    private final ControlListener listener;
    private final AppConfig config;

    // GUI Components
    private ButtonGroup qNumberGroup, tNumberGroup, rNumberGroup, iTypeGroup, pAiGroup;
    private ButtonGroup[] singleSiteGroups;
    private ButtonGroup[][] mdSiteGroups;
    private ButtonGroup[] qualitativeYnoGroups;
    private ButtonGroup[] qualitativeOtherGroups;
    private JTextField commentsField;
    private JButton saveButton, resetButton;

    // Constants from original app for site names
    private final String[] rootNames = { "1", "B", "L", "M", "D", "MB", "ML", "DB", "DL", "X" };
    private final String[] imageTypes = { "Preop", "Compl", "Ctrl", "Other" };
    private final String[] singleSitesNames = { "Apex", "Apex GP", "Root canal deviation", "Canal entrance center",
            "Lesion periphery" };
    private final String[] mdSitesNames = { "Lesion side", "Bone level", "CEJ", "Canal side 1mm", "Canal side 4mm" };
    private final String[] qualitativeYnNames = { "Apical voids", "Coronal voids", "Orifice plug",
            "Apical file fracture", "Coronal file fracture", "Apical perforation", "Coronal perforation", "Post" };
    private final String[][] qualitativeOtherItems = {
            { "Restoration gap", "NS", "No", "Yes" },
            { "Caries", "NS", "None", "Dentine", "Pulp space" },
            { "Restoration", "NS", "None", "Filling", "Crown/bridge" },
            { "Support/load", "NS", "Two appr", "One appr", "No appr", "Bridge abutment" }
    };

    private final Color[][] siteColors = {
            { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN }, // Single sites
            { Color.ORANGE, Color.MAGENTA, Color.PINK, new Color(128, 0, 128), new Color(0, 128, 128) } // MD sites
    };

    /**
     * Interface for the controller to listen to UI events.
     */
    public interface ControlListener {
        void onSaveRequested();

        void onSaveAndCloseRequested();

        void onResetRequested();

        void onSiteSelected(String siteName, Color color);

        void onMissingSiteSelected(String siteName);

        void onQualitativeSelected(String key, String value);

        void onIdentificationChanged(int quadrant, String tooth, String root, String imageType);
    }

    public MeasurementUI(String title, ControlListener listener, AppConfig config) {
        super("Endodontic Measurements 2.0");
        this.listener = listener;
        this.config = config;
        initializeUI();
    }

    private void initializeUI() {
        this.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(this.getBackground());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;

        // Selector Panel
        mainPanel.add(createIdentificationPanel(), c);

        // Single Sites
        c.gridy++;
        c.weighty = 1.0;
        mainPanel.add(createSitesPanel(), c);

        // MD Sites
        c.gridy++;
        c.weighty = 0.0;
        mainPanel.add(createMDSitesPanel(), c);

        // Qualitative YN
        c.gridy++;
        mainPanel.add(createQualitativeYNPanel(), c);

        // Qualitative Other
        c.gridy++;
        mainPanel.add(createQualitativeOtherPanel(), c);

        // Comment Line
        c.gridy++;
        c.weighty = 1.0;
        mainPanel.add(createCommentLine(), c);

        // Action Panel
        c.gridy++;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        mainPanel.add(createActionPanel(), c);

        // Disable everything until properly identified
        setMeasurementsEnabled(mainPanel, false);

        // Re-enable Identification Panel explicitly
        // Index 0 is the Identification panel we just added first
        setMeasurementsEnabled((JPanel) mainPanel.getComponent(0), true);

        // Add to scroll pane as v1.5 did
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        this.add(scrollPane, BorderLayout.CENTER);
        addMenu();

        // Calculate Scale Factor based on Screen Height
        this.pack();
        Dimension size = this.getPreferredSize();
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int targetHeight = (int) (screenHeight * 0.9); // Use 90% of screen height as target

        if (size.height > targetHeight) {
            double scale = (double) targetHeight / size.height;
            // Limit scaling to avoid unreadable text (min 0.7)
            scale = Math.max(0.7, scale);
            applyScale(this, scale);
            this.pack(); // Re-pack after scaling
        }

        this.setResizable(true);
        GUI.center(this);
        this.setVisible(true);
    }

    /**
     * Recursively applies a scale factor to a component and its children.
     * Scales fonts, insets, and layout gaps.
     */
    private void applyScale(Component comp, double scale) {
        // Scale Font
        Font font = comp.getFont();
        if (font != null) {
            comp.setFont(font.deriveFont((float) (font.getSize() * scale)));
        }

        if (comp instanceof Container) {
            Container container = (Container) comp;

            // Scale Layout Gaps
            LayoutManager layout = container.getLayout();
            if (layout instanceof FlowLayout) {
                FlowLayout fl = (FlowLayout) layout;
                fl.setHgap((int) (fl.getHgap() * scale));
                fl.setVgap((int) (fl.getVgap() * scale));
            } else if (layout instanceof GridBagLayout) {
                // GridBagLayout constraints are handled when adding components,
                // but we can try to scale existing ones if needed.
                // However, scaling the components themselves (like buttons)
                // usually handles the bulk of it.
            }

            // Scale Children
            for (Component child : container.getComponents()) {
                applyScale(child, scale);

                // Scale GridBagConstraints insets if applicable
                if (layout instanceof GridBagLayout) {
                    GridBagConstraints gbc = ((GridBagLayout) layout).getConstraints(child);
                    if (gbc != null && gbc.insets != null) {
                        gbc.insets.top = (int) (gbc.insets.top * scale);
                        gbc.insets.left = (int) (gbc.insets.left * scale);
                        gbc.insets.bottom = (int) (gbc.insets.bottom * scale);
                        gbc.insets.right = (int) (gbc.insets.right * scale);
                        ((GridBagLayout) layout).setConstraints(child, gbc);
                    }
                }
            }
        }

        // Scale individual component preferred size if it was hardcoded (like
        // JTextField)
        if (comp instanceof JTextField) {
            JTextField tf = (JTextField) comp;
            tf.setColumns((int) (tf.getColumns() * scale));
        }
    }

    private JPanel createIdentificationPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Quadrant number panel
        JPanel qPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        qPanel.add(new JLabel("Quadrant number:"));
        qNumberGroup = new ButtonGroup();
        for (int i = 1; i <= 4; i++) {
            JRadioButton rb = createRadioButton(String.valueOf(i), "QTR_qNumber;" + i);
            qNumberGroup.add(rb);
            qPanel.add(rb);
        }
        mainPanel.add(qPanel);

        // Tooth number panel
        JPanel tPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        tPanel.add(new JLabel("Tooth number:"));
        tNumberGroup = new ButtonGroup();
        for (int i = 1; i <= 8; i++) {
            JRadioButton rb = createRadioButton(String.valueOf(i), "QTR_tNumber;" + i);
            tNumberGroup.add(rb);
            tPanel.add(rb);
        }
        JRadioButton rbX = createRadioButton("X", "QTR_tNumber;X");
        tNumberGroup.add(rbX);
        tPanel.add(rbX);
        mainPanel.add(tPanel);

        // Root number panel
        JPanel rPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        rPanel.add(new JLabel("Root:"));
        rNumberGroup = new ButtonGroup();
        for (String rootName : rootNames) {
            JRadioButton rb = createRadioButton(rootName, "QTR_rNumber;" + rootName);
            rNumberGroup.add(rb);
            rPanel.add(rb);
        }
        mainPanel.add(rPanel);
        // Image Type panel
        JPanel iPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        iPanel.add(new JLabel("Image Type:"));
        iTypeGroup = new ButtonGroup();
        for (String type : imageTypes) {
            JRadioButton rb = createRadioButton(type, "QTR_iType;" + type);
            iTypeGroup.add(rb);
            iPanel.add(rb);
        }
        mainPanel.add(iPanel);

        return mainPanel;
    }

    /**
     * Recursively enables or disables components in a container.
     */
    private void setMeasurementsEnabled(Container container, boolean enabled) {
        for (Component c : container.getComponents()) {
            if (c instanceof JRadioButton) {
                // If it's a radio button in the identification panel, we don't touch it
                // We handle identifying the ID panel gracefully above by leaving it entirely
            }
            c.setEnabled(enabled);
            if (c instanceof Container) {
                setMeasurementsEnabled((Container) c, enabled);
            }
        }
    }

    private JPanel createSitesPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0;
        c.gridwidth = 1;

        singleSiteGroups = new ButtonGroup[singleSitesNames.length];
        for (int i = 0; i < singleSitesNames.length; i++) {
            c.gridy = i;
            singleSiteGroups[i] = new ButtonGroup();

            // Color label at start
            c.gridx = 0;
            p.add(createColorLabel(siteColors[0][i]), c);

            // Toggle button
            c.gridx = 1;
            JToggleButton btn = new JToggleButton(singleSitesNames[i]);
            btn.setActionCommand("SITE_" + singleSitesNames[i]);
            btn.addActionListener(this);
            singleSiteGroups[i].add(btn);
            p.add(btn, c);

            // Missing button (column 3 per v1.5)
            c.gridx = 3;
            JToggleButton miss = new JToggleButton("Missing");
            miss.setActionCommand("MISS_" + singleSitesNames[i]);
            miss.addActionListener(this);
            singleSiteGroups[i].add(miss);
            p.add(miss, c);

            // Color label at end (column 5 per v1.5)
            c.gridx = 5;
            p.add(createColorLabel(siteColors[0][i]), c);
        }
        return p;
    }

    private JPanel createMDSitesPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0;
        c.gridwidth = 1;

        mdSiteGroups = new ButtonGroup[mdSitesNames.length][2];

        // Header row
        c.gridy = 0;
        c.gridx = 1;
        p.add(new JLabel("Mesial:"), c);
        c.gridx = 3;
        p.add(new JLabel("Distal:"), c);

        for (int i = 0; i < mdSitesNames.length; i++) {
            c.gridy = i + 1;
            mdSiteGroups[i][0] = new ButtonGroup();
            mdSiteGroups[i][1] = new ButtonGroup();

            // Color label start
            c.gridx = 0;
            p.add(createColorLabel(siteColors[1][i]), c);

            // Mesial
            c.gridx = 1;
            JToggleButton mBtn = new JToggleButton(mdSitesNames[i] + "M");
            mBtn.setActionCommand("SITE_" + mdSitesNames[i] + "M");
            mBtn.addActionListener(this);
            mdSiteGroups[i][0].add(mBtn);
            p.add(mBtn, c);

            c.gridx = 2;
            JToggleButton mMiss = new JToggleButton("Missing");
            mMiss.setActionCommand("MISS_" + mdSitesNames[i] + "M");
            mMiss.addActionListener(this);
            mdSiteGroups[i][0].add(mMiss);
            p.add(mMiss, c);

            // Distal
            c.gridx = 3;
            JToggleButton dBtn = new JToggleButton(mdSitesNames[i] + "D");
            dBtn.setActionCommand("SITE_" + mdSitesNames[i] + "D");
            dBtn.addActionListener(this);
            mdSiteGroups[i][1].add(dBtn);
            p.add(dBtn, c);

            c.gridx = 4;
            JToggleButton dMiss = new JToggleButton("Missing");
            dMiss.setActionCommand("MISS_" + mdSitesNames[i] + "D");
            dMiss.addActionListener(this);
            mdSiteGroups[i][1].add(dMiss);
            p.add(dMiss, c);

            // Color label end
            c.gridx = 5;
            p.add(createColorLabel(siteColors[1][i]), c);
        }
        return p;
    }

    private JPanel createQualitativeYNPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;

        // PAI:
        p.add(new JLabel("PAI: "), c);
        pAiGroup = new ButtonGroup();
        String[] paiOptions = { "NS", "1", "2", "3", "4", "5" };
        for (int i = 0; i < paiOptions.length; i++) {
            c.gridx = i + 1;
            JRadioButton rb = createRadioButton(paiOptions[i], "QO_pAi;" + paiOptions[i]);
            pAiGroup.add(rb);
            p.add(rb, c);
        }

        // Other YN options
        String[] optionsYN = { "NS", "N", "Y" };
        qualitativeYnoGroups = new ButtonGroup[qualitativeYnNames.length];
        for (int i = 0; i < qualitativeYnNames.length; i++) {
            c.gridy++;
            c.gridx = 0;
            p.add(new JLabel(qualitativeYnNames[i] + ": "), c);
            qualitativeYnoGroups[i] = new ButtonGroup();
            for (int j = 0; j < optionsYN.length; j++) {
                c.gridx = j + 1;
                JRadioButton rb = createRadioButton(optionsYN[j], "QO_" + qualitativeYnNames[i] + ";" + optionsYN[j]);
                qualitativeYnoGroups[i].add(rb);
                p.add(rb, c);
            }
        }
        return p;
    }

    private JPanel createQualitativeOtherPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;

        qualitativeOtherGroups = new ButtonGroup[qualitativeOtherItems.length];
        for (int i = 0; i < qualitativeOtherItems.length; i++) {
            qualitativeOtherGroups[i] = new ButtonGroup();
            c.gridx = 0;
            p.add(new JLabel(qualitativeOtherItems[i][0] + ": "), c);
            for (int j = 1; j < qualitativeOtherItems[i].length; j++) {
                c.gridx = j;
                JRadioButton rb = createRadioButton(qualitativeOtherItems[i][j],
                        "QO_" + qualitativeOtherItems[i][0] + ";" + qualitativeOtherItems[i][j]);
                qualitativeOtherGroups[i].add(rb);
                p.add(rb, c);
            }
            c.gridy++;
        }
        return p;
    }

    private JPanel createCommentLine() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        p.add(new JLabel("Comments:"), c);
        c.gridx = 1;
        commentsField = new JTextField(30);
        p.add(commentsField, c);
        return p;
    }

    private JLabel createColorLabel(Color color) {
        JLabel label = new JLabel(" ");
        label.setOpaque(true);
        label.setBackground(color);
        return label;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveButton = new JButton("Save canal data");
        saveButton.setActionCommand("SAVE");
        saveButton.addActionListener(this);

        JButton saveCloseButton = new JButton("Save and close");
        saveCloseButton.setActionCommand("SAVE_CLOSE");
        saveCloseButton.addActionListener(this);

        resetButton = new JButton("Reset");
        resetButton.setActionCommand("RESET");
        resetButton.addActionListener(this);

        panel.add(resetButton);
        panel.add(saveButton);
        panel.add(saveCloseButton);
        return panel;
    }

    private JPanel createRadioRow(String label, String cmdPrefix, int start, int end, ButtonGroup group,
            String... extra) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel(label));
        for (int i = start; i <= end; i++) {
            JRadioButton rb = createRadioButton(String.valueOf(i), cmdPrefix + ";" + i);
            group.add(rb);
            p.add(rb);
        }
        for (String s : extra) {
            JRadioButton rb = createRadioButton(s, cmdPrefix + ";" + s);
            group.add(rb);
            p.add(rb);
        }
        return p;
    }

    private JPanel createRadioRow(String label, String cmdPrefix, String[] items, ButtonGroup group) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel(label));
        for (String item : items) {
            JRadioButton rb = createRadioButton(item, cmdPrefix + ";" + item);
            group.add(rb);
            p.add(rb);
        }
        return p;
    }

    private JRadioButton createRadioButton(String text, String cmd) {
        JRadioButton rb = new JRadioButton(text);
        rb.setMargin(new Insets(0, 0, 0, 0));
        rb.setActionCommand(cmd);
        rb.addActionListener(this);
        return rb;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if ("SAVE".equals(cmd)) {
            listener.onSaveRequested();
        } else if ("SAVE_CLOSE".equals(cmd)) {
            listener.onSaveAndCloseRequested();
        } else if ("RESET".equals(cmd)) {
            listener.onResetRequested();
        } else if (cmd.startsWith("SITE_")) {
            String siteName = cmd.substring(5);
            Color color = getSiteColor(siteName);
            listener.onSiteSelected(siteName, color);
        } else if (cmd.startsWith("MISS_")) {
            listener.onMissingSiteSelected(cmd.substring(5));
        } else if (cmd.startsWith("QO_")) {
            String[] parts = cmd.substring(3).split(";");
            listener.onQualitativeSelected(parts[0], parts[1]);
        } else if (cmd.startsWith("QTR_")) {
            updateIdentification();
        }
    }

    private void updateIdentification() {
        int q = getSelectedValue(qNumberGroup, -1);
        String t = getSelectedActionCommand(tNumberGroup, "-1");
        String r = getSelectedActionCommand(rNumberGroup, "-1");
        String it = getSelectedActionCommand(iTypeGroup, "-1");

        listener.onIdentificationChanged(q, t, r, it);

        boolean isFullyIdentified = (q != -1 && !"-1".equals(t) && !"-1".equals(r) && !"-1".equals(it));

        Component[] comps = this.getComponents();
        for (Component c : comps) {
            if (c instanceof JScrollPane) {
                Component view = ((JScrollPane) c).getViewport().getView();
                if (view instanceof JPanel) {
                    JPanel mainPanel = (JPanel) view;
                    // First component is Identification Panel, skip index 0
                    for (int i = 1; i < mainPanel.getComponentCount(); i++) {
                        Component child = mainPanel.getComponent(i);
                        if (child instanceof Container) {
                            setMeasurementsEnabled((Container) child, isFullyIdentified);
                        }
                    }
                }
            }
        }
    }

    private int getSelectedValue(ButtonGroup bg, int def) {
        String cmd = getSelectedActionCommand(bg, null);
        if (cmd == null)
            return def;
        try {
            return Integer.parseInt(cmd.substring(cmd.indexOf(";") + 1));
        } catch (Exception ex) {
            return def;
        }
    }

    private String getSelectedActionCommand(ButtonGroup bg, String def) {
        ButtonModel bm = bg.getSelection();
        if (bm == null)
            return def;
        String cmd = bm.getActionCommand();
        if (cmd.contains(";"))
            return cmd.substring(cmd.indexOf(";") + 1);
        return cmd;
    }

    private Color getSiteColor(String name) {
        for (int i = 0; i < singleSitesNames.length; i++) {
            if (singleSitesNames[i].equals(name))
                return siteColors[0][i];
        }
        for (int i = 0; i < mdSitesNames.length; i++) {
            if (name.startsWith(mdSitesNames[i]))
                return siteColors[1][i];
        }
        return Color.RED;
    }

    public void reset() {
        // Clear all selections
        qNumberGroup.clearSelection();
        tNumberGroup.clearSelection();
        rNumberGroup.clearSelection();
        iTypeGroup.clearSelection();

        // Clear site selections
        if (singleSiteGroups != null) {
            for (ButtonGroup bg : singleSiteGroups) {
                if (bg != null)
                    bg.clearSelection();
            }
        }

        if (mdSiteGroups != null) {
            for (ButtonGroup[] row : mdSiteGroups) {
                if (row != null) {
                    if (row[0] != null)
                        row[0].clearSelection();
                    if (row[1] != null)
                        row[1].clearSelection();
                }
            }
        }

        // Clear qualitative selections
        if (pAiGroup != null)
            pAiGroup.clearSelection();

        if (qualitativeYnoGroups != null) {
            for (ButtonGroup bg : qualitativeYnoGroups) {
                if (bg != null)
                    bg.clearSelection();
            }
        }

        if (qualitativeOtherGroups != null) {
            for (ButtonGroup bg : qualitativeOtherGroups) {
                if (bg != null)
                    bg.clearSelection();
            }
        }

        commentsField.setText("");

        // Request repaint to ensure UI updates visually if needed,
        // though ButtonGroup changes should handle it.
        this.repaint();
    }

    public String getComments() {
        return commentsField.getText();
    }

    private void addMenu() {
        MenuBar mb = new MenuBar();
        Menu m = new Menu("Plugin");
        MenuItem about = new MenuItem("About");
        about.addActionListener(e -> showAbout());
        m.add(about);
        mb.add(m);
        setMenuBar(mb);
    }

    private void showAbout() {
        IJ.showMessage("Endodontic Measurements 2.0",
                "Modernized version of the original plugin.\nGerald Torgersen (2026)");
    }
}
