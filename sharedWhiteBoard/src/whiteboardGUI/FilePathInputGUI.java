package whiteboardGUI;

import chatroom.message.Message;
import fileoperator.FileOperator;
import whiteboardGUI.DrawComponent.DrawComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * This class is used to get the file path from the user and save the file to the specified path.
 */
public class FilePathInputGUI extends JFrame {
    // GUI components
    private JTextField filePathInput;
    private JPanel panel1;
    private JButton cancelButton;
    private JButton saveButton;

    // main properties
    private WhiteBoardGUI whiteBoardGUI;
    private ArrayList<DrawComponent> drawComponents;
    private ArrayList<Message> messages;
    public FilePathInputGUI(WhiteBoardGUI whiteBoardGUI,ArrayList<DrawComponent> drawComponents, ArrayList<Message> messages) {
        this.whiteBoardGUI = whiteBoardGUI;
        this.drawComponents = drawComponents;
        this.messages = messages;

        // set up GUI
        setVisible(true);
        setContentPane(panel1);
        setSize(400, 200);
        setLocationRelativeTo(whiteBoardGUI);
        pack();

        // save file to the specified path
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filePath = filePathInput.getText();
                if (filePath.length() == 0) {
                    JOptionPane.showMessageDialog(whiteBoardGUI, "Please enter a file name.");
                } else {
                    // save the file
                    try {
                        String roomName = whiteBoardGUI.getRoomName();
                        FileOperator fileOperator = new FileOperator(drawComponents, messages, filePath, roomName);
                        fileOperator.writeToFile();
                        JOptionPane.showMessageDialog(whiteBoardGUI, "File saved successfully.");
                        whiteBoardGUI.setHistoryUrl(fileOperator.getFileName());
                        dispose();
                    } catch (RemoteException ex) {
                        JOptionPane.showMessageDialog(whiteBoardGUI, "Cannot get your room name:" + ex.getMessage());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(whiteBoardGUI, "File save failed:" + ex.getMessage());
                    }

                }
            }
        });

        // cancel saving file
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
}
