package whiteboardGUI.DrawTool;

import whiteboardGUI.DrawComponent.Oval;
import whiteboardGUI.DrawingArea;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;

/**
 *  This class contains the oval tool class.
 */
public class OvalTool extends DrawTool {
    // the oval that is being drawn
    Oval currOval;
    protected Point2D startPoint, endPoint;

    public OvalTool(DrawingArea drawingArea, Color color, int strokeWidth, String author) {
        super(drawingArea, color, strokeWidth, author);
        this.toolName = "Oval";
    }

    // set the start point when the mouse is pressed
    @Override
    public void mousePressed(MouseEvent e) {
        this.startPoint = e.getPoint();
        this.endPoint = this.startPoint;
    }

    // set the end point when the mouse is dragged and draw the previewed oval
    @Override
    public void mouseDragged(MouseEvent e) {
        this.endPoint = e.getPoint();
        this.currOval = new Oval(this.startPoint, this.endPoint, this.strokeWidth, this.color, this.author);
    }

    // draw the oval when the mouse is released and send the oval to the server
    @Override
    public void mouseReleased(MouseEvent e) throws RemoteException {
        this.endPoint = e.getPoint();
        Oval newOval  = new Oval(this.startPoint, this.endPoint, this.strokeWidth, this.color, this.author);
        this.drawingArea.addComponent(newOval);
        this.drawingArea.sendComponent(newOval);
        this.currOval = null;
    }

    // draw the previewed oval
    @Override
    public void drawPreview(Graphics2D g) {
        if (this.currOval != null) {
            this.currOval.draw(g, false);
        }
    }

}
