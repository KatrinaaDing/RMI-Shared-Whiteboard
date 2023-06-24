package whiteboardGUI.DrawTool;

import whiteboardGUI.DrawComponent.FreehandShape;
import whiteboardGUI.DrawingArea;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;

/**
 * This class contains the freehand tool class.
 */
public class FreehandTool extends DrawTool {
    // the freehand shape that is being drawn
    FreehandShape currShape;
    protected Point2D lastPoint;

    public FreehandTool(DrawingArea drawingArea, Color color, int strokeWidth, String author) {
        super(drawingArea, color, strokeWidth, author);
        this.toolName = "Freehand";
    }

    // initialize the freehand shape on mouse press
    @Override
    public void mousePressed(MouseEvent e) {
        this.lastPoint = e.getPoint();
        this.currShape = new FreehandShape(this.strokeWidth, this.color, this.author);
        this.currShape.addPoint(this.lastPoint);
    }

    // add points to the freehand shape on mouse drag
    @Override
    public void mouseDragged(MouseEvent e) {
        this.currShape.addPoint(e.getPoint());
    }

    // add the last point to the freehand shape on mouse release and send the shape to the server
    @Override
    public void mouseReleased(MouseEvent e) throws RemoteException {
        this.lastPoint = e.getPoint();
        this.currShape.addPoint(this.lastPoint);
        this.drawingArea.addComponent(this.currShape);
        this.drawingArea.sendComponent(this.currShape);
        this.currShape = null;
    }

    // draw the previewed freehand shape
    @Override
    public void drawPreview(Graphics2D g) {
        if (this.currShape != null) {
            this.currShape.draw(g, false);
        }
    }


}
