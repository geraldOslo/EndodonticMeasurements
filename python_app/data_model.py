from dataclasses import dataclass, field
from typing import Dict, List, Optional
import pandas as pd
import os
import datetime

@dataclass
class Site:
    name: str
    x: float
    y: float
    
    def to_string(self):
        return f"{self.x:.2f}; {self.y:.2f}; "

@dataclass
class Root:
    quadrant: int = -1
    tooth: str = "-1"
    root_name: str = "-1"
    image_type: str = "Other"
    operator: str = "Unknown"
    timestamp: str = ""
    sites: Dict[str, Site] = field(default_factory=dict)
    observations: Dict[str, str] = field(default_factory=dict)
    comments: str = ""
    
    def is_identified(self):
        return self.quadrant > 0 and self.tooth != "-1" and self.root_name != "-1"

class DataManager:
    def __init__(self, output_dir):
        self.output_dir = output_dir
        self.csv_filename = "Measurements.csv"
        
    def save_root(self, root: Root, image_path: str):
        if not root.is_identified():
            raise ValueError("Root not fully identified")
            
        timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        root.timestamp = timestamp
        
        # Determine output directory from image path
        image_dir = os.path.dirname(image_path)
        file_path = os.path.join(image_dir, self.csv_filename)
        
        # TODO: Implement exact CSV formatting matching the Java plugin
        # For now, just dumping a dict for testing
        data = {
            "File": image_path,
            "Timestamp": timestamp,
            "Operator": root.operator,
            "Quadrant": root.quadrant,
            "Tooth": root.tooth,
            "Root": root.root_name,
            "Comments": root.comments
        }
        # Add observations
        data.update(root.observations)
        # Add sites
        for name, site in root.sites.items():
            data[f"{name}_X"] = site.x
            data[f"{name}_Y"] = site.y
            
        df = pd.DataFrame([data])
        
        if not os.path.exists(file_path):
            df.to_csv(file_path, index=False, sep=';')
        else:
            df.to_csv(file_path, mode='a', header=False, index=False, sep=';')
