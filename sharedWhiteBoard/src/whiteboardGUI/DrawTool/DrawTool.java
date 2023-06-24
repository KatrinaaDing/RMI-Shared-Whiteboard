package whiteboardGUI.DrawTool;

import whiteboardGUI.DrawingArea;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;

/**
 *  This class contains the draw tool abstract class.
 *
 * Reference: https://www2.seas.gwu.edu/~simhaweb/lin/useful/DrawTool.java
 */
public abstract class DrawTool {
    // the drawing area that the tool is used on
    protected DrawingArea drawingArea;
    protected String toolName;
    protected Color color;
    protected int strokeWidth;
    // for displaying the author of the drawing
    protected String author;

    public DrawTool(DrawingArea drawingArea, Color color, int strokeWidth, String author) {
        this.drawingArea = drawingArea;
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.author = author;
    }

    public abstract void mousePressed(MouseEvent e);
    public abstract void mouseDragged(MouseEvent e);
    public abstract void mouseReleased(MouseEvent e) throws RemoteException;
    public abstract void drawPreview(Graphics2D g);

    // getters and setters
    public void setColor(Color color) {
        this.color = color;
    }
    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
    public Color getColor() {
        return this.color;
    }
    public int getStrokeWidth() {
        return this.strokeWidth;
    }


}
