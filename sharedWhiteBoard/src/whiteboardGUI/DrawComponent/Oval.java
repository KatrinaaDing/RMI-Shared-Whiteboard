package whiteboardGUI.DrawComponent;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 *  This class contains the oval component.
 */
public class Oval extends DrawComponent {

    public Oval (Point2D startPoint, Point2D endPoint, int strokeWidth, Color color, String author) {
        super(strokeWidth, color,author);
        Ellipse2D oval = new Ellipse2D.Double();
        oval.setFrameFromDiagonal(startPoint, endPoint);
        this.shape = oval;
        this.authorPos = endPoint;
    }
}
