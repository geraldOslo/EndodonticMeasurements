from PyQt6.QtWidgets import (QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, 
                             QLabel, QPushButton, QScrollArea, QFileDialog, QSlider, 
                             QGroupBox, QRadioButton, QButtonGroup, QLineEdit, QFormLayout,
                             QDockWidget, QMessageBox)
from PyQt6.QtCore import Qt, QPoint
from PyQt6.QtGui import QPixmap, QPainter, QPen, QColor, QAction
from image_handler import ImageHandler
from data_model import Root, Site, DataManager

# Constants
PIXELS_PER_MM = 20.0 # Default calibration assumption

class ImageViewer(QLabel):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.image_handler = ImageHandler()
        self.scale_factor = 1.0
        self.current_pixmap = None
        self.temp_point = None # (x, y)
        self.guide_circles = [] # List of tuples (x, y, radius)
        self.parent_window = parent

    def load_image(self, path):
        q_img = self.image_handler.load_image(path)
        self.current_pixmap = QPixmap.fromImage(q_img)
        self.guide_circles = [] # Clear guide circles
        self.update_display()

    def update_display(self):
        if self.current_pixmap:
            scaled_pixmap = self.current_pixmap.scaled(
                self.current_pixmap.size() * self.scale_factor,
                Qt.AspectRatioMode.KeepAspectRatio,
                Qt.TransformationMode.SmoothTransformation
            )
            self.setPixmap(scaled_pixmap)

    def set_brightness(self, value):
        self.image_handler.set_brightness(value)
        self.refresh_image()

    def set_contrast(self, value):
        self.image_handler.set_contrast(value)
        self.refresh_image()

    def refresh_image(self):
        self.refresh_image_with_sites()

    def wheelEvent(self, event):
        if self.current_pixmap:
            delta = event.angleDelta().y()
            if delta > 0:
                self.scale_factor *= 1.1
            else:
                self.scale_factor /= 1.1
            
            self.update_display()

    def mousePressEvent(self, event):
        if self.current_pixmap:
            # Map click to image coordinates accounting for offset
            # When the label is larger than the pixmap (e.g. zoomed out or small image),
            # the pixmap is centered. We need to subtract the offset.
            
            # Get the size of the displayed pixmap (scaled)
            scaled_size = self.pixmap().size()
            
            # Calculate offsets (centering)
            x_offset = (self.width() - scaled_size.width()) / 2
            y_offset = (self.height() - scaled_size.height()) / 2
            
            # Adjust click position
            local_x = event.pos().x() - x_offset
            local_y = event.pos().y() - y_offset
            
            # Scale back to original coordinates
            x = local_x / self.scale_factor
            y = local_y / self.scale_factor
            
            # Log for debug
            if self.parent_window:
                self.parent_window.update_status(f"Clicked: ({x:.1f}, {y:.1f}) Scale: {self.scale_factor:.2f}")

            # Ensure coordinates are within bounds
            if 0 <= x < self.image_handler.original_image.shape[1] and 0 <= y < self.image_handler.original_image.shape[0]:
                self.temp_point = (x, y)
                self.refresh_image_with_sites()
            else:
                if self.parent_window:
                    self.parent_window.update_status(f"Click out of bounds: ({x:.1f}, {y:.1f})")


    def refresh_image_with_sites(self):
        # We need to get the image with sites burned in for display
        
        # Get the base adjusted image
        q_img = self.image_handler.get_qt_image()
        if not q_img:
            return
            
        # Create a mutable pixmap
        pixmap = QPixmap.fromImage(q_img)
        painter = QPainter(pixmap)
        
        # Determine point size:
        # Cross hair size (radius)
        base_radius = 5.0
        # Keep it relatively constant on screen, or slightly scaling?
        # User wants "much smaller", covering less.
        # A 1-pixel cross is good.
        
        # We calculate "screen" pixels size.
        # Screen_Radius = 10px
        # Image_Radius = 10 / Scale
        radius = max(2.0, 5.0 / self.scale_factor)
        
        pen_width = max(1, 1 / self.scale_factor)
        painter.setPen(QPen(QColor(255, 0, 0), int(pen_width)))
        painter.setBrush(Qt.BrushStyle.NoBrush) # No filling
        
        # Draw all sites as Crosses
        for name, site in self.parent_window.root_data.sites.items():
            cx, cy = int(site.x), int(site.y)
            r = int(radius)
            # Draw Cross (+)
            painter.drawLine(cx - r, cy, cx + r, cy)
            painter.drawLine(cx, cy - r, cx, cy + r)
            
            # Draw text slightly offset, smaller font
            font = painter.font()
            font.setPointSize(int(max(8, 10 / self.scale_factor)))
            painter.setFont(font)
            painter.drawText(cx + r + 2, cy + r + 2, name)
            
        # Draw temp point if exists
        if self.temp_point:
            painter.setPen(QPen(QColor(0, 255, 0), int(pen_width))) # Green for temp
            cx, cy = int(self.temp_point[0]), int(self.temp_point[1])
            r = int(radius)
            painter.drawLine(cx - r, cy, cx + r, cy)
            painter.drawLine(cx, cy - r, cx, cy + r)
            
        # Draw guide circles
        if self.guide_circles:
            painter.setPen(QPen(QColor(255, 0, 0), int(pen_width)))
            painter.setBrush(Qt.BrushStyle.NoBrush)
            for x, y, radius in self.guide_circles:
                # Radius in image pixels needs to be scaled to screen pixels?
                # No, standard is: draw on image coords.
                # But wait, we are drawing on a pixmap that is NOT yet scaled?
                # The pixmap here is `q_img` -> `pixmap` which IS the high res image.
                # So we draw in Image Coordinates.
                
                cx, cy = int(x), int(y)
                r = int(radius)
                painter.drawEllipse(QPoint(cx, cy), r, r)

        painter.end()

        self.current_pixmap = pixmap
        self.update_display()
        
    def clear_guide_circles(self):
        self.guide_circles = []
        self.refresh_image_with_sites()

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Endodontic Measurements")
        self.resize(1200, 800)
        
        self.root_data = Root()
        self.data_manager = DataManager("output") 
        # Make sure output directory exists
        import os
        os.makedirs("output", exist_ok=True)
        
        self.init_ui()
        
        # Enable Drag and Drop
        self.setAcceptDrops(True)

    def dragEnterEvent(self, event):
        if event.mimeData().hasUrls():
            event.accept()
        else:
            event.ignore()

    def dropEvent(self, event):
        files = [u.toLocalFile() for u in event.mimeData().urls()]
        if files:
            # Load the first file
            self.load_image_file(files[0])

        
    def init_ui(self):
        # Central Widget - Image Viewer
        self.scroll_area = QScrollArea()
        self.image_viewer = ImageViewer(self)
        self.scroll_area.setWidget(self.image_viewer)
        self.scroll_area.setWidgetResizable(True)
        self.setCentralWidget(self.scroll_area)
        
        # Dock Widget - Controls
        self.create_control_panel()
        
        # Menu
        self.create_menu()

    def create_menu(self):
        menubar = self.menuBar()
        file_menu = menubar.addMenu('File')
        
        open_action = QAction('Open Image', self)
        open_action.triggered.connect(self.open_image)
        file_menu.addAction(open_action)
        
        exit_action = QAction('Exit', self)
        exit_action.triggered.connect(self.close)
        file_menu.addAction(exit_action)

    def create_control_panel(self):
        dock = QDockWidget("Controls", self)
        dock.setAllowedAreas(Qt.DockWidgetArea.RightDockWidgetArea)
        
        # Create a scrollable widget for the dock
        scroll = QScrollArea()
        scroll.setWidgetResizable(True)
        content = QWidget()
        layout = QVBoxLayout(content)
        
        # Image Adjustments
        adj_group = QGroupBox("Image Adjustments")
        adj_layout = QFormLayout()
        
        self.bright_slider = QSlider(Qt.Orientation.Horizontal)
        self.bright_slider.setRange(-100, 100)
        self.bright_slider.valueChanged.connect(self.image_viewer.set_brightness)
        adj_layout.addRow("Brightness", self.bright_slider)
        
        self.contrast_slider = QSlider(Qt.Orientation.Horizontal)
        self.contrast_slider.setRange(-100, 100)
        self.contrast_slider.valueChanged.connect(self.image_viewer.set_contrast)
        adj_layout.addRow("Contrast", self.contrast_slider)
        
        adj_group.setLayout(adj_layout)
        layout.addWidget(adj_group)
        
        # Identification
        id_group = QGroupBox("Identification")
        id_layout = QVBoxLayout()
        
        # Helper to create horizontal radio groups
        def create_radio_group(label_text, options, default=None):
            group_layout = QHBoxLayout()
            group_layout.addWidget(QLabel(label_text))
            bg = QButtonGroup(self)
            # Make buttons exclusive
            bg.setExclusive(True)
            
            for opt in options:
                rb = QRadioButton(opt)
                bg.addButton(rb)
                group_layout.addWidget(rb)
                if default and opt == default:
                    rb.setChecked(True)
            
            group_layout.addStretch()
            return bg, group_layout

        # Quadrant
        self.quad_bg, quad_lo = create_radio_group("Quadrant:", ["1", "2", "3", "4"])
        id_layout.addLayout(quad_lo)
        
        # Tooth
        # Tooth 1-8 and X. Might be too wide for one row, let's split or allow wrap.
        # Simple QHBoxLayout might overflow. Let's try two rows if needed or just one.
        # 9 items should fit.
        self.tooth_bg, tooth_lo = create_radio_group("Tooth:", [str(i) for i in range(1, 9)] + ["X"])
        id_layout.addLayout(tooth_lo)
        
        # Root
        # 1, B, L, M, D, MB, ML, DB, DL, X
        # This is quite long. Let's split into two rows? Or just flow it.
        # For simplicity, let's try a custom FlowLayout or just use wrapping QFormLayout or similar.
        # Or just two hardcoded rows.
        self.root_bg = QButtonGroup(self)
        root_opts = ["1", "B", "L", "M", "D", "MB", "ML", "DB", "DL", "X"]
        
        root_layout = QVBoxLayout()
        root_layout.addWidget(QLabel("Root:"))
        
        r_row1 = QHBoxLayout()
        r_row2 = QHBoxLayout()
        
        for i, opt in enumerate(root_opts):
            rb = QRadioButton(opt)
            self.root_bg.addButton(rb)
            if i < 5:
                r_row1.addWidget(rb)
            else:
                r_row2.addWidget(rb)
                
        r_row1.addStretch()
        r_row2.addStretch()
        root_layout.addLayout(r_row1)
        root_layout.addLayout(r_row2)
        id_layout.addLayout(root_layout)

        # Image Type
        self.img_type_bg, img_type_lo = create_radio_group("Image Type:", ["Preop", "Compl", "Ctrl", "Other"], default="Other")
        id_layout.addLayout(img_type_lo)
        
        id_group.setLayout(id_layout)
        layout.addWidget(id_group)
        
        # Single Sites
        site_group = QGroupBox("Single Sites")
        site_layout = QVBoxLayout()
        
        single_sites = ["Apex", "Apex GP", "Canal deviation", "Canal entrance c.", "Lesion periphery"]
        for site in single_sites:
            btn = QPushButton(site)
            btn.clicked.connect(lambda checked, s=site: self.assign_point_to_site(s))
            site_layout.addWidget(btn)
            
        site_group.setLayout(site_layout)
        layout.addWidget(site_group)

        # M/D Sites
        md_group = QGroupBox("M/D Sites")
        md_layout = QVBoxLayout()
        
        md_sites = ["Lesion side", "Bone level", "CEJ", "C. s. 1 mm", "C. s. 4 mm"]
        for site in md_sites:
            h_layout = QHBoxLayout()
            h_layout.addWidget(QLabel(site))
            
            btn_m = QPushButton("M")
            btn_m.clicked.connect(lambda checked, s=site+" M": self.assign_point_to_site(s))
            h_layout.addWidget(btn_m)
            
            btn_d = QPushButton("D")
            btn_d.clicked.connect(lambda checked, s=site+" D": self.assign_point_to_site(s))
            h_layout.addWidget(btn_d)
            
            md_layout.addLayout(h_layout)
            
        md_group.setLayout(md_layout)
        layout.addWidget(md_group)
        
        # Observations
        obs_group = QGroupBox("Observations")
        obs_layout = QFormLayout()
        
        self.obs_inputs = {}
        
        # PAI
        pai_layout = QHBoxLayout()
        self.pai_group = QButtonGroup(self)
        for i in ["NS", "1", "2", "3", "4", "5"]:
            rb = QRadioButton(i)
            self.pai_group.addButton(rb)
            pai_layout.addWidget(rb)
            if i == "NS": rb.setChecked(True)
        obs_layout.addRow("PAI:", pai_layout)
        
        # Y/N Observations
        yn_obs = ["Apical voids", "Coronal voids", "Orifice plug", "Apical file fracture", 
                  "Coronal file fracture", "Apical perforation", "Coronal perforation", 
                  "Post", "Restoration gap"]
                  
        for obs in yn_obs:
            yn_layout = QHBoxLayout()
            bg = QButtonGroup(self)
            self.obs_inputs[obs] = bg
            
            for opt in ["NS", "N", "Y"]:
                rb = QRadioButton(opt)
                bg.addButton(rb)
                yn_layout.addWidget(rb)
                if opt == "NS": rb.setChecked(True)
            
            obs_layout.addRow(obs, yn_layout)

        obs_group.setLayout(obs_layout)
        layout.addWidget(obs_group)
        
        # Comments
        layout.addWidget(QLabel("Comments:"))
        self.comments_input = QLineEdit()
        layout.addWidget(self.comments_input)
        
        # Save Button
        save_btn = QPushButton("Save and Close")
        save_btn.clicked.connect(self.save_data)
        layout.addWidget(save_btn)
        
        layout.addStretch()
        
        scroll.setWidget(content)
        dock.setWidget(scroll)
        self.addDockWidget(Qt.DockWidgetArea.RightDockWidgetArea, dock)

    def save_data(self):
        if not self.image_viewer.image_handler.original_image is not None:
            QMessageBox.warning(self, "Error", "No image loaded")
            return

        # Collect Identification
        q_btn = self.quad_bg.checkedButton()
        if q_btn:
            self.root_data.quadrant = int(q_btn.text())
            
        t_btn = self.tooth_bg.checkedButton()
        if t_btn:
            self.root_data.tooth = t_btn.text()
            
        r_btn = self.root_bg.checkedButton()
        if r_btn:
            self.root_data.root_name = r_btn.text()

        it_btn = self.img_type_bg.checkedButton()
        if it_btn:
            self.root_data.image_type = it_btn.text()
        
        if not self.root_data.is_identified():
            QMessageBox.warning(self, "Error", "Please fill in Quadrant, Tooth, and Root")
            return
            
        # Collect Observations
        # PAI
        pai_btn = self.pai_group.checkedButton()
        if pai_btn:
            self.root_data.observations["PAI"] = pai_btn.text()
            
        # Other Obs
        for name, bg in self.obs_inputs.items():
            btn = bg.checkedButton()
            if btn:
                self.root_data.observations[name] = btn.text()
                
        # Comments
        self.root_data.comments = self.comments_input.text()
        
        # Save CSV
        try:
            # We need the original filename. 
            # Currently ImageHandler doesn't store it, let's assume we can pass it or store it.
            # I'll update ImageHandler to store the path or pass it from MainWindow.
            # MainWindow has it in open_image, let's store it there.
            if hasattr(self, 'current_image_path'):
                # Clear guide circles before saving/resetting
                self.image_viewer.clear_guide_circles()
                
                self.data_manager.save_root(self.root_data, self.current_image_path)
                
                # Save Burned-in Image
                # Generate filename: Measured-<timestamp>-<original_name>
                import os
                import datetime
                timestamp = datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
                base_name = os.path.basename(self.current_image_path)
                new_name = f"Measured-{timestamp}-{base_name}"
                
                # Save in same directory as original
                image_dir = os.path.dirname(self.current_image_path)
                save_path = os.path.join(image_dir, new_name)
                
                # Ensure output dir exists (technically image_dir exists)
                
                burned_img = self.image_viewer.image_handler.burn_in_sites(self.root_data.sites)
                burned_img.save(save_path)
                
                QMessageBox.information(self, "Success", f"Data saved to {self.data_manager.csv_filename}\nImage saved to {new_name}")
                self.close()
            else:
                QMessageBox.warning(self, "Error", "Image path not found")
                
        except Exception as e:
            QMessageBox.critical(self, "Error", str(e))

    def open_image(self):
        file_name, _ = QFileDialog.getOpenFileName(self, "Open Image", "", "Images (*.png *.xpm *.jpg *.tif)")
        if file_name:
            self.load_image_file(file_name)

    def load_image_file(self, file_path):
        self.current_image_path = file_path
        self.image_viewer.load_image(file_path)

    def assign_point_to_site(self, site_name):
        if self.image_viewer.temp_point:
            x, y = self.image_viewer.temp_point
            self.root_data.sites[site_name] = Site(site_name, x, y)
            
            # Special logic for "Apex": Draw guide circles
            if site_name == "Apex":
                # Circle 1: 1 mm radius
                r1 = 1.0 * PIXELS_PER_MM
                # Circle 2: 4 mm radius
                r4 = 4.0 * PIXELS_PER_MM
                
                self.image_viewer.guide_circles = [
                    (x, y, r1),
                    (x, y, r4)
                ]
            
            self.image_viewer.temp_point = None
            self.image_viewer.refresh_image_with_sites()
            self.update_status(f"Site recorded: {site_name}")
        else:
            QMessageBox.warning(self, "Warning", "Please click on the image to select a point first.")

    def update_status(self, message):
        self.statusBar().showMessage(message)
