package whiteboardGUI.DrawComponent;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * This class contains the circle component.
 */
public class Circle extends DrawComponent {

    public Circle (Point2D startPoint, Point2D endPoint, int strokeWidth, Color color, String author) {
        super(strokeWidth, color, author);
        int x = (int) Math.min(startPoint.getX(), endPoint.getX());
        int y = (int) Math.min(startPoint.getY(), endPoint.getY());
        int width = (int) Math.abs(startPoint.getX() - endPoint.getX());
        this.shape = new Ellipse2D.Double(x, y, width, width);
        this.authorPos = endPoint;
    }
}
