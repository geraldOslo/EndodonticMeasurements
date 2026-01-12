package no.uio.odont.logic;

import java.awt.geom.Point2D;
import no.uio.odont.model.MeasurementRoot;
import no.uio.odont.model.MeasurementSite;

/**
 * Utility class for performing endodontic calculations.
 * 
 * @author Gerald Torgersen
 * @version 2.0
 * @date January 2026
 */
public class Calculations {

    /**
     * Calculates the distance between two points.
     */
    public static double distance(Point2D.Double p1, Point2D.Double p2) {
        return p1.distance(p2);
    }

    /**
     * Calculates the Schneider angle (α) at site 3 (curvature point) between site 4
     * and site 2.
     * α = 180° - β, where β is the angle 4-3-2.
     * 
     * @param p2 Site 2 (AGP)
     * @param p3 Site 3 (Curvature)
     * @param p4 Site 4 (Entrance)
     * @return The Schneider angle in degrees.
     */
    public static double calculateSchneiderAngle(Point2D.Double p2, Point2D.Double p3, Point2D.Double p4) {
        double a = p3.distance(p2); // Distance curvature to AGP
        double b = p4.distance(p2); // Distance entrance to AGP
        double c = p4.distance(p3); // Distance entrance to curvature

        // Law of cosines: b^2 = a^2 + c^2 - 2ac * cos(β)
        // cos(β) = (a^2 + c^2 - b^2) / (2ac)
        double cosBeta = (a * a + c * c - b * b) / (2 * a * c);
        double beta = Math.toDegrees(Math.acos(cosBeta));

        return 180.0 - beta;
    }

    // Additional calculation methods can be added here
}
