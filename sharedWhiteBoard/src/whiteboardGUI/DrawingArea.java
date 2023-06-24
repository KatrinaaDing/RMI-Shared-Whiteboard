package whiteboardGUI;

import whiteboardGUI.DrawComponent.DrawComponent;
import whiteboardGUI.DrawTool.DrawTool;
import whiteboardGUI.DrawTool.LineTool;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 *  This class contains the drawing area (canvas) component for the Whiteboard GUI.
 */
public class DrawingArea extends JPanel {
    private final Color BACKGROUND_COLOR = Color.WHITE;
    private ArrayList<DrawComponent> drawComponents = new ArrayList<DrawComponent>();
    private DrawTool drawTool;
    private boolean isClear = false;
    private boolean showAuthor = true;
    WhiteBoardGUI whiteBoardGUI;

    public DrawingArea(WhiteBoardGUI whiteBoardGUI) {
        // set up GUI
        setSize(200,200);
        this.whiteBoardGUI = whiteBoardGUI;
        // default draw tool is line
        this.drawTool = new LineTool(this, Color.BLACK, 2, "");
        this.setBackground(Color.WHITE);
        this.setDoubleBuffered(false);
        setVisible(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // get starting point
                drawTool.mousePressed(e);
            }

        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                drawTool.mouseDragged(e);
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    drawTool.mouseReleased(e);
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
                repaint();
            }

        });
    }

    /**
     *  This method is called when the drawing area needs to be repainted.
     *  It will draw all the components in the drawComponents list and the preview shape.
     *
     * @param g the graphics object to be drawn on
     */
    @Override
    protected synchronized void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // draw white on entire draw area to clear
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, getSize().width, getSize().height);
        if (isClear) {
            isClear = false;
        } else {
            for (DrawComponent drawComponent : drawComponents) {
                drawComponent.draw(g2, showAuthor);
            }
            drawTool.drawPreview(g2);
        }
    }

    /**
     *  This method clears the drawing area.
     */
    public void clear() {
        isClear = true;
        this.drawComponents.clear();
        repaint();
    }

    /**
     * This method sends the draw component to the server and broadcast to each participant.
     *
     * @param drawComponent the draw component to be sent
     * @throws RemoteException
     */
    public void sendComponent(DrawComponent drawComponent) throws RemoteException {
        this.whiteBoardGUI.notifyDraw(drawComponent);
    }

    /**
     * This method adds a draw component to the drawComponents list and redraw canvas
     *
     * @param drawComponent
     */
    public synchronized void addComponent(DrawComponent drawComponent) {
        this.drawComponents.add(drawComponent);
        repaint();
    }

    /**
     * This method exports the drawing area as an image to current folder.
     */
    public void createImage() {
        // Create a BufferedImage and draw components on it
        BufferedImage image = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        paintComponent(g2d);

        // create new file to write the image
    	File file = new File("output.png");
        try {
            ImageIO.write(image, "png", file);
            // notify success saving
            JOptionPane.showMessageDialog(null, "Image exported successfully.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving the canvas as image: "+ e.getMessage());
        }
    }

    // setters and getters
    /**
     * Set the draw tool to be used
     *
     * @param drawTool the draw tool to set
     */
    public void setDrawTool(DrawTool drawTool) {
        this.drawTool = drawTool;
    }

    /**
     * Set the stroke width of the draw tool
     *
     * @param width the width of the stroke
     */
    public void setStrokeWidth(int width) {
        this.drawTool.setStrokeWidth(width);
    }

    /**
     * Set the color of the draw tool
     *
     * @param color the color of the stroke
     */
    public void setColor(Color color) {
        this.drawTool.setColor(color);
    }

    /**
     * Set the draw components to be drawn on the canvas.
     * This is usually invoked when the client joins the room or the admin opens a history of a draw board.
     *
     * @param drawComponents the draw components to be drawn
     */
    public void setDrawingComponents(ArrayList<DrawComponent> drawComponents) {
        this.drawComponents = drawComponents;
    }

    /**
     * Get the draw tool currently using
     *
     * @return the draw tool
     */
    public DrawTool getDrawTool() {
        return drawTool;
    }

    /**
     * Get the draw components currently on the draw board
     *
     * @return the draw components
     */
    public ArrayList<DrawComponent> getDrawingComponents() {
        return this.drawComponents;
    }

    /**
     * Toggle if to show the author of each drawing on draw board
     *
     * @param showAuthor if to show the author
     */
    public void setShowAuthor(boolean showAuthor) {
    	this.showAuthor = showAuthor;
    }
}
