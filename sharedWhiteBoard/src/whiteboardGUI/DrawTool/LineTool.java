package whiteboardGUI.DrawTool;

import whiteboardGUI.DrawComponent.Line;
import whiteboardGUI.DrawingArea;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;

/**
 *  This class contains the line tool class.
 */
public class LineTool extends DrawTool {
    // the line that is being drawn
    Line currLine;
    protected Point2D startPoint, endPoint;

    public LineTool(DrawingArea drawingArea, Color color, int strokeWidth, String author) {
        super(drawingArea, color, strokeWidth, author);
        this.toolName = "Line";
    }

    // set the start point when the mouse is pressed
    @Override
    public void mousePressed(MouseEvent e) {
        //save coord x,y where mouse was pressed
        this.startPoint = e.getPoint();
        this.endPoint = this.startPoint;
    }

    // set the end point when the mouse is dragged and draw the previewed line
    @Override
    public void mouseDragged(MouseEvent e) {
        //coord x,y where drag ended
        this.endPoint = e.getPoint();
        this.currLine = new Line(this.startPoint, this.endPoint, this.strokeWidth, this.color, this.author);
    }

    // draw the line when the mouse is released and send the line to the server
    @Override
    public void mouseReleased(MouseEvent e) throws RemoteException {
        this.endPoint = e.getPoint();
        Line newLine = new Line(this.startPoint, this.endPoint, this.strokeWidth, this.color, this.author);
        this.drawingArea.addComponent(newLine);
        this.drawingArea.sendComponent(newLine);
        this.currLine = null;
    }

    // draw the previewed line
    @Override
    public void drawPreview(Graphics2D g) {
        if (this.currLine != null) {
            this.currLine.draw(g, false);
        }
    }


}
