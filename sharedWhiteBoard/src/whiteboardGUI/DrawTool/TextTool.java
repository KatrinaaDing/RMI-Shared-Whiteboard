package whiteboardGUI.DrawTool;

import whiteboardGUI.DrawComponent.Text;
import whiteboardGUI.DrawingArea;
import whiteboardGUI.TextInputGUI;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;

/**
 * This class contains the tool class to draw text.
 */
public class TextTool extends DrawTool {

    // the text that is being inserted
    Text currText;
    protected Point2D startPoint;

    public TextTool(DrawingArea drawingArea, Color color, int strokeWidth, String author) {
        super(drawingArea, color, strokeWidth, author);
        this.toolName = "Text";
    }

    // no action required for mouse press and drag
    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        // open a text input window when mouseReleased
        this.startPoint = e.getPoint();
        new TextInputGUI(this);
    }

    // no preview for text
    @Override
    public void drawPreview(Graphics2D g) {}

    /**
     * Add the text component to current canvas and send to server
     *
     * @param text the text to be added
     */
    public void setText(String text) {
        Text newText = new Text(this.startPoint, this.strokeWidth, this.color, text, this.author);
        this.drawingArea.addComponent(newText);
        try {
            this.drawingArea.sendComponent(newText);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        this.currText = null;
    }

}
