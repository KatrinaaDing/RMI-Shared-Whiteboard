package whiteboardGUI.DrawComponent;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *  This class contains the rectangle component.
 */
public class Rectangle extends DrawComponent {

    public Rectangle (Point2D startPoint, Point2D endPoint, int strokeWidth, Color color, String author) {
        super(strokeWidth, color, author);
        Rectangle2D.Double s = new Rectangle2D.Double();
        s.setFrameFromDiagonal(startPoint, endPoint);
        this.shape = s;
        this.authorPos = endPoint;
    }

}
