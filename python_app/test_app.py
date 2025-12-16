import os
import cv2
import numpy as np
from image_handler import ImageHandler
from data_model import Root, Site, DataManager

def create_dummy_image(path):
    # Create a 100x100 black image
    img = np.zeros((100, 100, 3), dtype=np.uint8)
    cv2.imwrite(path, img)

def test_backend():
    print("Starting backend verification...")
    
    # Setup
    test_dir = "test_output"
    import shutil
    if os.path.exists(test_dir):
        shutil.rmtree(test_dir)
    os.makedirs(test_dir, exist_ok=True)
    img_path = os.path.join(test_dir, "test_img.jpg")
    create_dummy_image(img_path)
    
    # 1. Image Handler
    print("Testing ImageHandler...")
    handler = ImageHandler()
    handler.load_image(img_path)
    
    # Test adjustments
    handler.set_brightness(50)
    handler.set_contrast(20)
    assert handler.brightness == 50
    assert handler.contrast == 20
    print("ImageHandler adjustments OK")
    
    # 2. Data Model
    print("Testing DataModel...")
    root = Root()
    root.quadrant = 1
    root.tooth = "1"
    root.root_name = "1"
    root.sites["Apex"] = Site("Apex", 10, 10)
    root.observations["PAI"] = "1"
    
    assert root.is_identified()
    print("DataModel identification OK")
    
    # 3. Data Manager
    print("Testing DataManager...")
    manager = DataManager(test_dir)
    manager.save_root(root, img_path)
    
    csv_path = os.path.join(test_dir, "Measurements.csv")
    assert os.path.exists(csv_path)
    
    with open(csv_path, 'r') as f:
        content = f.read()
        print(f"CSV Content ({len(content)} bytes):")
        print(content)
        assert "Apex_X" in content or "Apex" in content
        assert "10" in content or "10.0" in content
    print("DataManager CSV save OK")
    
    # 4. Burn-in
    print("Testing Burn-in...")
    burned_img = handler.burn_in_sites(root.sites)
    burned_path = os.path.join(test_dir, "burned_test.jpg")
    burned_img.save(burned_path)
    assert os.path.exists(burned_path)
    print("Burn-in save OK")
    
    print("All backend tests passed!")

if __name__ == "__main__":
    test_backend()
