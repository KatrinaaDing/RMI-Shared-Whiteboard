package whiteboardGUI.DrawComponent;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 *  This class contains the line component.
 */
public class Line extends DrawComponent {

    public Line (Point2D startPoint, Point2D endPoint, int width, Color color, String author) {
        super(width,color,author);
        this.shape = new Line2D.Double(startPoint, endPoint);
        this.authorPos = endPoint;
    }

}