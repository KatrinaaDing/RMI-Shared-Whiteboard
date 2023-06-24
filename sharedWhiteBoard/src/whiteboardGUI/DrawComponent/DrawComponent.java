/**
 *  This class contains the draw component.
 *
 * @author Ziqi Ding
 * Reference: https://www2.seas.gwu.edu/~simhaweb/lin/useful/DrawTool.java
 */
package whiteboardGUI.DrawComponent;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;

public abstract class DrawComponent implements Serializable {
    // Desired color.
    protected Color color;
    protected int stroke;
    protected Shape shape;

    protected String author;
    // Position of the author label.
    protected Point2D authorPos;

    public DrawComponent(int stroke, Color c, String author) {
        this.color = c;
        this.stroke = stroke;
        this.author = author;
    }

    /**
     * Draw the component on canvas
     * @param g The graphics object
     * @param showAuthor whether to show the author label
     */
    public void draw(Graphics2D g, boolean showAuthor) {
        // draw shape
        g.setColor(this.color);
        g.setStroke(new BasicStroke(this.stroke));
        g.draw(this.shape);
        // draw author label
        if (showAuthor)
            drawAuthorLabel(g, this.authorPos);
    };

    /**
     *  Method for drawing the author label
     * @param g The graphics object
     * @param position The position of the author label
     */
    protected void drawAuthorLabel(Graphics2D g, Point2D position) {
        // set color to 1/3 transparent
        Color transparentColor = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 255/3);
        g.setColor(transparentColor);
        Font font = new Font("TimesRoman", Font.PLAIN, 14);
        g.setFont(font);
        // draw author label
        g.drawString("<"+this.author+">", (int) position.getX() - 1, (int) position.getY() - 1);
    }

    // setters

    public void setColor(Color color) {
        this.color = color;
    }

}
