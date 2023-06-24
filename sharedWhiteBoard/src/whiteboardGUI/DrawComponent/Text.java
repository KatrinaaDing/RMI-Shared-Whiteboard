package whiteboardGUI.DrawComponent;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 *  This class contains the text component.
 */
public class Text extends DrawComponent {
    // the text content
    private String content;
    private Point2D startPoint;

    public Text (Point2D startPoint, int strokeWidth, Color color, String content, String author) {
        super(strokeWidth, color, author);
        this.startPoint = startPoint;
        this.content = content;
        this.shape = null;
    }

    // methods for drawing the text
    @Override
    public void draw(Graphics2D g2, boolean showAuthor) {
        // draw author label
        if (showAuthor)
            drawAuthorLabel(g2, this.startPoint);

        // draw text
        g2.setColor(this.color);
        Font font = new Font("TimesRoman", Font.PLAIN, stroke * 8);
        g2.setFont(font);
        int x = (int) this.startPoint.getX();
        int y = (int) this.startPoint.getY();
        for (String line : this.content.split("\n"))
            g2.drawString(line, x, y += g2.getFontMetrics().getHeight());
    }
}
