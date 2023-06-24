package whiteboardGUI;

import chatroom.message.Message;
import fileoperator.FileOperator;
import logger.WhiteboardLogger;
import remote.IRemoteClient;
import remote.IRemoteRoom;
import whiteboardGUI.DrawComponent.DrawComponent;
import whiteboardGUI.DrawTool.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The primary panel GUI for the whiteboard application
 */
public class WhiteBoardGUI extends JFrame {

    // Swing GUI components
    private JButton openButton;
    private JButton saveButton;
    private JTextField textField1;
    private JButton sendButton;
    private JTextArea messageList;
    private JList userList;
    private JComboBox toolSelector;
    private JComboBox strokeWidthSelector;
    private JPanel whiteboardPanel;
    private JButton saveAsButton;
    private JPanel drawboard;
    private JPanel chatroom;
    private JButton setColorButton;
    private JLabel roomLabel;
    private JButton leaveButton;
    private JPanel drawPanel;
    private JLabel strokeLabel;
    private JButton kickButton;
    private JCheckBox showAuthorCheckBox;
    private JButton clearButton;
    private JButton exportImageButton;
    private DrawingArea drawingArea;

    // main properties
    private IRemoteClient client;
    private IRemoteRoom server;
    private boolean isAdmin;
    private String username;
    private ArrayList<Message> messages = new ArrayList<>();
    private DefaultListModel<String> usersModel = new DefaultListModel<>();
    private String historyUrl;

    /**
     * Instantiates a new White board gui for a room participant.
     *
     * @param client the client service
     * @throws RemoteException the remote exception
     */
    public WhiteBoardGUI(IRemoteClient client) throws RemoteException {
        this(false);
        // set user
        this.client = client;
        this.client.setWhiteBoardGUI(this);
        defaultSetup();
    }

    /**
     * Instantiates a new White board gui for a room admin (creator).
     *
     * @param server the remote room service
     * @throws RemoteException the remote exception
     */
    public WhiteBoardGUI(IRemoteRoom server) throws RemoteException {
        this(true);
        // set user
        this.server = server;
        setTitle("[" + server.getAdmin() + "] Whiteboard APP");
        roomLabel.setText("Room name: " + server.getRoomName());
        defaultSetup();
    }

    /**
     * A default constructor for a new White board gui.
     *
     * @param isAdmin if the current user is room admin
     */
    public WhiteBoardGUI(boolean isAdmin) {
        this.isAdmin = isAdmin;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        createUIComponents();
        setSize(700,500);
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(whiteboardPanel);

        this.pack();

        // Invoked when a draw tool is selected.
        toolSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                setDrawTool(cb.getSelectedItem().toString());
            }
        });

        // Select a color when the "select color" button is clicked.
        setColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // display color selector
                Color color = JColorChooser.showDialog(new JFrame(),"Select a Color", Color.black);

                setColorButton.setBackground(color);
                setColorButton.setForeground(color);
                setColorButton.setOpaque(true);
                drawingArea.setColor(color);
            }
        });

        // Leave the room when the "leave" button is clicked.
        leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (isAdmin) {
                        // if admin, end the meeting
                        server.endMeeting();
                    } else {
                        // if participant, leave the room
                        client.disconnect();
                    }
                } catch (RemoteException remoteException) {
                    remoteException.printStackTrace();

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                dispose();
                System.exit(0);
            }
        });

        // Invoked when a stroke width is selected.
        strokeWidthSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                setStrokeWidth(cb.getSelectedItem().toString());
            }
        });

        // Send a message when the "send" button is clicked.
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textField1.getText().equals("")) {
                    popMsg("Cannot send empty message");
                    return;
                }
                try {
                    if (isAdmin) {
                        // if admin, broadcast the message
                        server.broadcastMessage(server.getAdmin(), textField1.getText());
                    } else {
                        // if participant, send the message to the server
                        client.sendMessage(textField1.getText());
                    }
                    // clear the text field
                    textField1.setText("");

                } catch (RemoteException remoteException) {
                    remoteException.printStackTrace();
                }
            }
        });

        // Kick a user when the "kick" button is clicked. Only admin can invoke this action.
        kickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedUser = (String) userList.getSelectedValue();
                // validate selected value
                if (selectedUser == null) {
                    popMsg("Please select a user to remove");
                    return;
                } else if (selectedUser.contains("<admin>")) {
                    popMsg("Cannot remove admin");
                    return;
                }
                try {
                    // kick the user
                    server.kick(selectedUser);
                } catch (RemoteException remoteException) {
                    remoteException.printStackTrace();
                }
            }
        });

        // Save the current drawing to a file when the "save as" button is clicked.
        saveAsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // open file path insert dialog
                new FilePathInputGUI(drawingArea.whiteBoardGUI, drawingArea.getDrawingComponents(), messages);
            }
        });

        // Open a room history when the "open" button is clicked.
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // check if there is any drawing or message
                if (drawingArea.getDrawingComponents().size() > 0 || messages.size() > 1) {
                    int result = JOptionPane.showConfirmDialog(WhiteBoardGUI.this,
                            "Opening a new file will clear the current drawing and messages. Do you want to continue?",
                            "Warning",
                            JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                // open file chooser
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));
                fileChooser.setDialogTitle("Specify a directory to save the room history");
                // disable "all files" option
                fileChooser.setAcceptAllFileFilterUsed(false);
                // only allow directory selection
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int userSelection = fileChooser.showOpenDialog(WhiteBoardGUI.this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    String url = fileChooser.getSelectedFile().getPath();
                    try {
                        // read files
                        FileOperator fileOperator = new FileOperator(url);
                        fileOperator.readFromFile();
                        // broadcast the history
                        server.broadcastHistory(fileOperator.getDrawComponents(), fileOperator.getMessages());
                        // enable save function
                        setHistoryUrl(fileOperator.getFileName());
                    } catch (Exception ex) {
                        popMsg("History failed to load. Please try again");
                    }
                }
            }
        });

        // Save the current drawings and chats to files when the "save" button is clicked.
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FileOperator fileOperator = new FileOperator(drawingArea.getDrawingComponents(), messages, historyUrl, "");
                    fileOperator.saveToExistingFile();
                    setHistoryUrl(fileOperator.getFileName());
                    JOptionPane.showMessageDialog(WhiteBoardGUI.this, "File saved successfully.");
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(WhiteBoardGUI.this, "Cannot get your room name:" + ex.getMessage());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(WhiteBoardGUI.this, "File save failed:" + ex.getMessage());
                }
            }
        });

        // Show author name to each drawing when the "show author" checkbox is clicked.
        showAuthorCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawingArea.setShowAuthor(showAuthorCheckBox.isSelected());
                drawingArea.repaint();
            }
        });

        // Clear all the drawings when the "clear" button is clicked.
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // confirm clear
                int result = JOptionPane.showConfirmDialog(null,
                        "Are you sure want to remove all the drawings?",
                        "Warning",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        server.broadcastClear();
                    } catch (RemoteException remoteException) {
                        remoteException.printStackTrace();
                    }
                }
            }
        });

        // Export the current drawing to an image when the "export image" button is clicked.
        exportImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    drawingArea.createImage();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }
    /**
     * Default setup for the white board gui.
     */
    private void createUIComponents() {
        drawingArea = new DrawingArea(this);
        drawPanel.setLayout(new BorderLayout());
        drawPanel.add(drawingArea, BorderLayout.CENTER);
        drawPanel.add(drawingArea);
    }

    /**
     * Set the stroke width of the current draw tool based on the given size.
     *
     * @param size the size of the stroke width, can be "small", "large", or "normal"
     */
    private void setStrokeWidth(String size) {
        switch (size) {
            case "small" -> drawingArea.setStrokeWidth(1);
            case "large" -> drawingArea.setStrokeWidth(4);
            default -> drawingArea.setStrokeWidth(2);
        }
    }

    /**
     * Change the current draw tool.
     *
     * @param tool the name of the draw tool
     */
    private void setDrawTool(String tool) {
        DrawTool currDrawTool = drawingArea.getDrawTool();
        DrawTool newDrawTool = null;
        String author = this.getAuthor();
        switch (tool) {
            case "rectangle":
                newDrawTool = new RectangleTool(drawingArea, currDrawTool.getColor(), currDrawTool.getStrokeWidth(), author);
                break;
            case "circle":
                newDrawTool = new CircleTool(drawingArea, currDrawTool.getColor(), currDrawTool.getStrokeWidth(), author);
                break;
            case "text":
                newDrawTool = new TextTool(drawingArea, currDrawTool.getColor(), currDrawTool.getStrokeWidth(), author);
                break;
            case "oval":
                newDrawTool = new OvalTool(drawingArea, currDrawTool.getColor(), currDrawTool.getStrokeWidth(), author);
                break;
            case "freehand":
                newDrawTool = new FreehandTool(drawingArea, currDrawTool.getColor(), currDrawTool.getStrokeWidth(), author);
                break;
            case "line":
            default:
                newDrawTool = new LineTool(drawingArea, currDrawTool.getColor(), currDrawTool.getStrokeWidth(), author);
        }
        if (tool.equals("text")) {
            strokeLabel.setText("Font Size");
        } else {
            strokeLabel.setText("Stroke Width");
        }

        this.drawingArea.setDrawTool(newDrawTool);
        strokeWidthSelector.setSelectedItem("normal");
        this.setStrokeWidth("normal");
    }

    /**
     * Notify the server to broadcast the drawing to all participants.
     *
     * @param drawComponent the drawed component to broadcast
     * @throws RemoteException the remote exception
     */
    public void notifyDraw(DrawComponent drawComponent) throws RemoteException {
        if (isAdmin) {
            this.server.broadcastDrawing(this.server.getAdmin(), drawComponent);
        } else {
            try {
                this.client.getRemoteRoom().broadcastDrawing(this.client.getUsername(),drawComponent);
            } catch (RemoteException e) {
                this.client.kickedOut("Admin is disconnected");
            }
        }
    }

    /**
     * Add message to local message list.
     *
     * @param message the message
     */
    public synchronized void addMessage(Message message) {
        ArrayList<Message> newMessages = new ArrayList<>(this.messages);
        newMessages.add(message);
        setMessages(newMessages);
    }

    /**
     * Add draw component to local drawing component list.
     *
     * @param drawComponent the draw component
     */
    public synchronized void addDrawComponent(DrawComponent drawComponent) {
        drawingArea.addComponent(drawComponent);
    }

    /**
     * Sets users.
     *
     * @param users the users to set
     */
    public synchronized void setUsers(ArrayList<String> users) {
        DefaultListModel<String> newModel = new DefaultListModel<String>();
        try {
            // add admin before other users
            if (this.isAdmin) {
                newModel.addElement(this.server.getAdmin() + " <admin> (YOU)");
            } else {
                newModel.addElement(this.client.getRemoteRoom().getAdmin() + " <admin>");
            }
            // add other users
            for (String user : users) {
                if (user.equals(this.username))
                    newModel.addElement(user + " (YOU)");
                else
                    newModel.addElement(user);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        // update the user list
        this.usersModel = newModel;
        this.userList.setModel(newModel);
    }

    /**
     * Add new user to local user list.
     *
     * @param user the user to add
     */
    public synchronized void addUser(String user) {
        this.usersModel.addElement(user);
        userList.setModel(this.usersModel);
    }

    /**
     * Remove user from the local user list.
     *
     * @param user the user to remove
     */
    public synchronized void removeUser(String user) {
        this.usersModel.removeElement(user);
        userList.setModel(this.usersModel);
    }

    /**
     * Sets messages to the chat window.
     * @param messages the messages to set
     */
    public synchronized void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
        ArrayList<String> chats = new ArrayList<String>();
        for (Message message : messages) {
            chats.add(message.toString());
        }
        messageList.setText(String.join("\n", chats));
    }

    /**
     * Sets all drawing components to the drawing area.
     * @param drawComponents the drawing components to set
     */
    public synchronized void setDrawingComponents(ArrayList<DrawComponent> drawComponents) {
        this.drawingArea.setDrawingComponents(drawComponents);
        this.drawingArea.repaint();
    }

    /**
     * Clear the drawing area
     */
    public void clearBoard() {
        this.drawingArea.clear();
    }

    /**
     * Client mode setup for the GUI.
     *
     * @param username the username
     * @throws RemoteException the remote exception
     */
    public void clientSetup(String username) throws RemoteException {
        this.username = username;
        setTitle("[" + this.client.getUsername() + "] Whiteboard APP");
        roomLabel.setText(client.getRemoteRoom().getRoomName());

        // only admin can kick member, save, save as, open, clear canvas
        kickButton.setVisible(false);
        userList.setEnabled(false);
        saveButton.setVisible(false);
        saveAsButton.setVisible(false);
        openButton.setVisible(false);
        clearButton.setVisible(false);

        // retrieve users, messages and drawing components from server
        setUsers(client.getRemoteRoom().getUsers());
        setMessages(client.getRemoteRoom().getMessages());
        this.drawingArea.setDrawingComponents(client.getRemoteRoom().getDrawComponents());

        this.setVisible(true);
    }

    /**
     * Room admin mode setup for the GUI.
     *
     * @param username the username
     * @throws RemoteException the remote exception
     */
    public void serverSetup(String username) throws RemoteException {
        this.username = username;
        setTitle("[" + this.server.getAdmin() + "] Whiteboard APP");
        roomLabel.setText(server.getRoomName());
        leaveButton.setText("End Meeting");
        setUsers(this.server.getUsers());
        // by default save button is disabled
        saveButton.setEnabled(false);
        this.addMessage(new Message(username, "Welcome to the room!"));

        this.setVisible(true);
    }

    /**
     * Gets author (also the current user) of the drawing.
     *
     * @return the author
     */
    public String getAuthor() {
        String author = "";
        try {
            author = isAdmin ? this.server.getAdmin() : this.client.getUsername();
        } catch (RemoteException e) {
            popMsg("Error getting author");
            e.printStackTrace();
        }
        return author;
    }

    /**
     * Default setup for GUI.
     */
    public void defaultSetup() {
        // set default tool
        toolSelector.setSelectedItem("line");
        setDrawTool("line");
        strokeWidthSelector.setSelectedItem("normal");
        setStrokeWidth("normal");
        showAuthorCheckBox.setSelected(true);

    }

    /**
     * Invoked when the current participant is kicked out.
     *
     * @param msg the msg to display
     */
    public void kickedOut(String msg) {
        // only a client can be kicked out
        if (isAdmin) {
            popMsg("Admin cannot be kicked out");
            WhiteboardLogger.error("Admin cannot be kicked out");
            throw new RuntimeException("Admin cannot be kicked out");
        }
        popMsg(msg);
        this.dispose();

        // stop the app after a short delay to allow unbinding from the registry
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 800);
    }

    /**
     * Gets the room name.
     *
     * @return the room name
     * @throws RemoteException the remote exception
     */
    public String getRoomName() throws RemoteException {
         return isAdmin ? this.server.getRoomName() : this.client.getRemoteRoom().getRoomName();
    }

    /**
     * Gets the history file url.
     *
     * @return the history url
     */
    public void setHistoryUrl(String url) {
        this.historyUrl = url;
        saveButton.setEnabled(true);
    }


    /**
     * pop up a message
     * @param msg message to show
     */
    private void popMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}


