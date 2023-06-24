package whiteboardGUI.DrawTool;

import whiteboardGUI.DrawComponent.Circle;
import whiteboardGUI.DrawingArea;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;

/**
 *  This class contains the circle tool class.
 */
public class CircleTool extends DrawTool{
    // the circle that is being drawn
    Circle currCir;
    protected Point2D startPoint, endPoint;

    public CircleTool(DrawingArea drawingArea, Color color, int strokeWidth, String author) {
        super(drawingArea, color, strokeWidth, author);
        this.toolName = "Circle";
    }

    // set the start point when the mouse is pressed
    @Override
    public void mousePressed(MouseEvent e) {
        this.startPoint = e.getPoint();
        this.endPoint = this.startPoint;
    }

    // set the end point when the mouse is dragged and draw the previewed circle
    @Override
    public void mouseDragged(MouseEvent e) {
        this.endPoint = e.getPoint();
        this.currCir = new Circle(this.startPoint, this.endPoint, this.strokeWidth, this.color, this.author);
    }

    // draw the circle when the mouse is released and send the circle to the server
    @Override
    public void mouseReleased(MouseEvent e) throws RemoteException {
        this.endPoint = e.getPoint();
        Circle newCir = new Circle(this.startPoint, this.endPoint, this.strokeWidth, this.color, this.author);
        this.drawingArea.addComponent(newCir);
        this.drawingArea.sendComponent(newCir);
        this.currCir = null;
    }

    // draw the previewed circle
    @Override
    public void drawPreview(Graphics2D g) {
        if (this.currCir != null) {
            this.currCir.draw(g, false);
        }
    }


}
