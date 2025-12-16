import cv2
import numpy as np
from PIL import Image, ImageQt

class ImageHandler:
    def __init__(self):
        self.original_image = None
        self.display_image = None
        self.brightness = 0
        self.contrast = 0
        self.zoom_level = 1.0
        
    def load_image(self, path):
        # Read image using OpenCV
        self.original_image = cv2.imread(path)
        if self.original_image is None:
            raise ValueError("Could not load image")
        # Convert BGR to RGB
        self.original_image = cv2.cvtColor(self.original_image, cv2.COLOR_BGR2RGB)
        self.display_image = self.original_image.copy()
        self.reset_adjustments()
        return self.get_qt_image()

    def reset_adjustments(self):
        self.brightness = 0
        self.contrast = 0
        self.zoom_level = 1.0
        self.apply_adjustments()

    def apply_adjustments(self):
        if self.original_image is None:
            return

        # Apply brightness and contrast
        # Formula: new_img = alpha * old_img + beta
        # alpha = contrast (1.0 is no change)
        # beta = brightness (0 is no change)
        
        alpha = (self.contrast + 100) / 100.0
        beta = self.brightness
        
        adjusted = cv2.convertScaleAbs(self.original_image, alpha=alpha, beta=beta)
        self.display_image = adjusted
        
    def get_qt_image(self):
        if self.display_image is None:
            return None
            
        height, width, channel = self.display_image.shape
        bytes_per_line = 3 * width
        q_img = ImageQt.QImage(self.display_image.data, width, height, bytes_per_line, ImageQt.QImage.Format.Format_RGB888)
        return q_img

    def set_brightness(self, value):
        self.brightness = value
        self.apply_adjustments()

    def set_contrast(self, value):
        self.contrast = value
        self.apply_adjustments()
        
    def burn_in_sites(self, sites):
        # Create a copy to draw on
        img_copy = self.display_image.copy()
        for name, site in sites.items():
            # Draw point
            cv2.circle(img_copy, (int(site.x), int(site.y)), 5, (255, 0, 0), -1)
            # Draw label
            cv2.putText(img_copy, name, (int(site.x)+10, int(site.y)), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 1)
            
        return Image.fromarray(img_copy)
