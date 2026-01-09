
from PIL import Image
import os

test_dir = r"c:\Temp\EndodonticMeasurements\testimages"
files = os.listdir(test_dir)
for f in files:
    if f.lower().endswith('.tif') or f.lower().endswith('.jpg'):
        path = os.path.join(test_dir, f)
        try:
            img = Image.open(path)
            print(f"File: {f}")
            print(f"Info: {img.info}")
            if 'dpi' in img.info:
                print(f"DPI: {img.info['dpi']}")
            break
        except Exception as e:
            print(f"Error reading {f}: {e}")
