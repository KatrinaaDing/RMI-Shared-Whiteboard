package whiteboardGUI.DrawComponent;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * This class contains the freehand shape component.
 */
public class FreehandShape extends DrawComponent{

    private ArrayList<Point2D> points = new ArrayList<>();
    public FreehandShape(int strokeWidth, Color color, String author) {
        super(strokeWidth, color, author);
    }

    public void addPoint(Point2D point) {
        this.points.add(point);
    }
    @Override
    public void draw(Graphics2D g2, boolean showAuthor) {
        // draw author label
        if (showAuthor)
            drawAuthorLabel(g2, this.points.get(this.points.size() - 1));

        // draw freehand shape
        g2.setColor(this.color);
        g2.setStroke(new BasicStroke(this.stroke));
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);
            g2.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
        }
    }


}
