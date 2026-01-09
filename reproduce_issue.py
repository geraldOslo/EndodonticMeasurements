
import sys
import os

# Add python_app to path
sys.path.append(os.path.join(os.getcwd(), 'python_app'))

from PyQt6.QtWidgets import QApplication
from gui import MainWindow
from data_model import Root
from image_handler import ImageHandler
import numpy as np
from PIL import Image
import numpy as np
from PIL import Image

def mock_load_image(self, path):
    # exact 1000x1000 white image
    self.original_image = np.ones((1000, 1000, 3), dtype=np.uint8) * 255
    self.display_image = self.original_image.copy()
    self.reset_adjustments()
    return self.get_qt_image()

ImageHandler.load_image = mock_load_image

def main():
    app = QApplication(sys.argv)
    window = MainWindow()
    
    # Load a fake image
    window.load_image_file("test.jpg")
    
    # Simulate valid click
    window.image_viewer.temp_point = (500, 500)
    
    # Trigger Apex assignment
    print("Assigning Apex...")
    window.assign_point_to_site("Apex")
    
    # Check if circles are set
    circles = window.image_viewer.guide_circles
    print(f"Guide circles: {circles}")
    
    if len(circles) == 2:
        print("SUCCESS: Circles set in data structure.")
    else:
        print("FAILURE: Circles NOT set.")
        
    # Check if they persist into refresh
    # refresh_image_with_sites is called inside assign_point_to_site
    # We can check if `guide_circles` is still there
    if window.image_viewer.guide_circles:
        print("Circles persist after refresh.")
    
    sys.exit(0)

if __name__ == "__main__":
    main()
