package whiteboardGUI;

import whiteboardGUI.DrawTool.TextTool;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class represents a text input GUI for the whiteboard application.
 * It allows users to input text and interact with the text tool.
 */
public class TextInputGUI extends JFrame {
    private JTextPane textInput;
    private JPanel panel1;
    private JButton okButton;
    private JButton cancelButton;
    private TextTool textTool;

    /**
     * Constructs a new TextInputGUI with the specified text tool.
     *
     * @param textTool the text tool to be used with this TextInputGUI
     */
    public TextInputGUI(TextTool textTool) {
        setTitle("Text Input");
        setContentPane(panel1);
        this.textTool = textTool;
        setLocationRelativeTo(this);
        setSize(300,300);
        pack();
        setVisible(true);

        // cancel inserting text to the drawing area
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // confirm insert text to the drawing area
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textTool.setText(textInput.getText());
                dispose();
            }
        });
    }
}
