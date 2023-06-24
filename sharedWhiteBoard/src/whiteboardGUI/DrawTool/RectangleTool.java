package whiteboardGUI.DrawTool;

import whiteboardGUI.DrawComponent.Rectangle;
import whiteboardGUI.DrawingArea;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;

/**
 *  This class contains the rectangle tool class.
 */
public class RectangleTool extends DrawTool {
    // the rectangle that is being drawn
    Rectangle currRect;
    protected Point2D startPoint, endPoint;

    public RectangleTool(DrawingArea drawingArea, Color color, int strokeWidth, String author) {
        super(drawingArea, color, strokeWidth, author);
        this.toolName = "Rectangle";
    }

    // set the start point when the mouse is pressed
    @Override
    public void mousePressed(MouseEvent e) {
        this.startPoint = e.getPoint();
        this.endPoint = this.startPoint;
    }

    // set the end point when the mouse is dragged and draw the previewed rectangle
    @Override
    public void mouseDragged(MouseEvent e) {
        this.endPoint = e.getPoint();
        this.currRect = new Rectangle(this.startPoint, this.endPoint, this.strokeWidth, this.color, this.author);
    }

    // draw the rectangle when the mouse is released and send the rectangle to the server
    @Override
    public void mouseReleased(MouseEvent e) throws RemoteException {
        this.endPoint = e.getPoint();
        Rectangle newRect = new Rectangle(this.startPoint, this.endPoint, this.strokeWidth, this.color, this.author);
        this.drawingArea.addComponent(newRect);
        this.drawingArea.sendComponent(newRect);
        this.currRect = null;
    }

    // draw the previewed rectangle
    @Override
    public void drawPreview(Graphics2D g) {
        if (this.currRect != null) {
            this.currRect.draw(g, false);
        }
    }

}
