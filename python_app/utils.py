import math

def calculate_distance(p1, p2):
    """Calculates the Euclidean distance between two points (x, y)."""
    return math.sqrt((p2[0] - p1[0])**2 + (p2[1] - p1[1])**2)

def calculate_angle(p1, p2, p3):
    """Calculates the angle at p2 formed by p1-p2-p3."""
    a = calculate_distance(p2, p3)
    b = calculate_distance(p1, p3)
    c = calculate_distance(p1, p2)
    
    try:
        angle_rad = math.acos((a**2 + c**2 - b**2) / (2 * a * c))
        return math.degrees(angle_rad)
    except ValueError:
        return 0.0
