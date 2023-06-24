package whiteboardGUI;

import client.RemoteClient;
import logger.WhiteboardLogger;
import remote.IRemoteClient;
import remote.IRemoteRoom;
import server.RemoteRoom;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * LoginGUI is the first GUI that user will see when they open the app.
 */
public class LoginGUI extends javax.swing.JFrame {
    // GUI components
    private JTextField roomNameInput;
    private JButton joinRoomButton;
    private JPanel HomePanel;
    private JTextField usernameInput;
    private JButton createRoomButton;
    private JList roomList;
    private JButton refreshListButton;
    private JLabel joinStatus;

    // main properties
    private final int MAX_ROOM_SIZE = 50;
    private Registry registry;
    private DefaultListModel<String> roomListModel;
    private ArrayList<String> roomNames;

    public LoginGUI(String host, int port) throws RemoteException {
        // get all the available rooms from registry
        this.registry = LocateRegistry.getRegistry(host, port);
        this.roomListModel = getRooms();
        roomList.setModel(roomListModel);

        // set up the GUI
        setContentPane(HomePanel);
        setTitle ("Whiteboard App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400,500);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);

        // invoked when user click the "join room" button
        joinRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // validate and get the input
                if (!validateInput()) return;
                String roomName =  roomNameInput.getText();
                String username = usernameInput.getText();

                // look for the room and pop up error message if not found
                IRemoteRoom remoteRoom = null;
                try {
                    remoteRoom = (IRemoteRoom) registry.lookup("room/" + roomName);
                    ArrayList<String> users = remoteRoom.getUsers();
                    if (users.contains(username) || username.equals(remoteRoom.getAdmin())) {
                        popMsg("ERROR: Username already exists. Please use another name.");
                        return;
                    } else if (users.size() == MAX_ROOM_SIZE) {
                        popMsg("ERROR: Room is full. Please try again later.");
                        return;
                    }

                } catch (RemoteException ex) {
                    popMsg("ERROR: Connection error. Please try again.");
                    return;
                } catch (NotBoundException ex) {
                    popMsg("ERROR: Room not exists. Please try again.");
                    return;
                }

                // setting a background worker to join the room, avoid freezing the UI
                IRemoteRoom finalRemoteRoom = remoteRoom;
                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        try {
                            // hide the action buttons and show the status
                            joinStatus.setText("Requesting to join room...");
                            joinRoomButton.setVisible(false);
                            createRoomButton.setVisible(false);
                            // request remote room
                            return requestJoin(username, finalRemoteRoom);
                        } catch (ConnectException ex) {
                            popMsg("ERROR: Room not exists. Please try again.");
                            return false;
                        } catch (Exception ex) {
                            popMsg("ERROR: " + ex.getMessage());
                            ex.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            // get the result from the worker
                            boolean result = get();
                            if (result) {
                                // if admin agree joining in, create a client and connect to the room
                                IRemoteClient client = new RemoteClient(username);
                                WhiteBoardGUI whiteBoardGUI = new WhiteBoardGUI(client);
                                client.connect(registry, roomName);
                                whiteBoardGUI.clientSetup(username);

                                // add a listener to the window, when user close the window, disconnect the client
                                whiteBoardGUI.addWindowListener(new WindowAdapter() {
                                    public void windowClosing(WindowEvent ev) {
                                    try {
                                        client.disconnect();
                                    } catch (Exception ex) {
                                        throw new RuntimeException(ex);
                                    }
                                    }
                                });
                                WhiteboardLogger.info("Client: connected to remote room");
                                dispose();
                            } else {
                                // if admin reject the join in request, show the buttons again
                                joinStatus.setText("");
                                joinRoomButton.setVisible(true);
                                createRoomButton.setVisible(true);
                                popMsg("Admin rejected your request to join.");
                            }
                        } catch (InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                };

                // Start the worker thread
                worker.execute();
            }
        });

        // invoked when user click the "create room" button
        createRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // validate and get the input
                if (!validateInput()) return;
                String roomName =  roomNameInput.getText();
                String username = usernameInput.getText();
                if (roomNames.contains(roomName)) {
                    popMsg("ERROR: Room already exists. Please use another name.");
                    return;
                }

                // create a room and bind it to the registry
                IRemoteRoom remoteRoom = null;
                try{
                    remoteRoom = new RemoteRoom(username, roomName);
                    registry.rebind("room/"+roomName, remoteRoom);
                    remoteRoom.setRegistry(registry);

                    WhiteboardLogger.info("Room server "+roomName+" ready");
                    WhiteBoardGUI whiteBoardGUI = new WhiteBoardGUI(remoteRoom);
                    remoteRoom.setWhiteBoardGUI(whiteBoardGUI);
                    whiteBoardGUI.serverSetup(username);
                    IRemoteRoom finalRemoteRoom = remoteRoom;
                    // add a listener to the window, when user close the window, end the meeting
                    whiteBoardGUI.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent ev) {
                            try {
                                finalRemoteRoom.endMeeting();
                            } catch (Exception ex) {
                                WhiteboardLogger.error("Room server failed closing");
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                    // close the login window
                    dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    popMsg("ERROR: " + ex.getMessage());
                }

            }
        });

        // invoked when user select a room (for joining) from the list
        roomList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String roomName = (String) roomList.getSelectedValue();
                if (roomName != null)
                    roomNameInput.setText(roomName.split(" ")[1]);
            }
        });

        // invoked when user click the "refresh" button
        refreshListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // refresh the room list
                try {
                    roomListModel = getRooms();
                    roomList.clearSelection();
                    roomList.setModel(roomListModel);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Get the rooms and their information from the registry
     * @return a list model of the rooms
     */
    private DefaultListModel<String> getRooms() throws RemoteException {
        DefaultListModel<String> roomsModel = new DefaultListModel<String>();
        ArrayList<String> newRoomNames = new ArrayList<String>();
        for (String name : this.registry.list()) {
            if (name.startsWith("room/")) {
                newRoomNames.add(name.split("/")[1]);
                try {
                    roomsModel.addElement(getRoomInfo(name));
                } catch (NotBoundException e) {
                    WhiteboardLogger.error(e.toString());
                } catch (ConnectException e) {
                    // if the room is not connected to the registry, unbind it
                    WhiteboardLogger.info("Found a room that is not connected to the registry. Unbinding the room.");
                    try {
                        this.registry.unbind(name);
                    } catch (NotBoundException ex) {
                        WhiteboardLogger.error("Cannot unbind the non-connected room.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // update the room names
        this.roomNames = newRoomNames;
        return roomsModel;
    }

    /**
     * Request to join a room
     * @param username the username of the client
     * @param remoteRoom the remote room
     * @return true if the request is accepted, false otherwise
     */
    private boolean requestJoin(String username, IRemoteRoom remoteRoom) throws RemoteException {
        return remoteRoom.requestJoin(username);
    }

    /**
     * Validate input of username and room name
     * @return true if the input is valid, false otherwise
     */
    private boolean validateInput() {
        String roomName =  roomNameInput.getText();
        String username = usernameInput.getText();
        if (roomName.isEmpty() || username.isEmpty()) {
            popMsg("Please enter a room name and a username");
            return false;
        }
        if (roomName.contains(" ") || username.contains(" ")) {
            popMsg("Room name and username cannot contain space");
            return false;
        }
        if (username.contains("admin") || username.contains("host")) {
            popMsg("Invalid name");
            return false;
        }
        return true;
    }

    /**
     * Get the information of a room available in the registry
     * @param roomName the name of the room
     * @return the information of the room
     */
    private String getRoomInfo(String roomName) throws NotBoundException, RemoteException {
        WhiteboardLogger.info("getting info of: " + roomName);
        IRemoteRoom remoteRoom = (IRemoteRoom) registry.lookup(roomName);
        String[] info = remoteRoom.getRoomInfo().split(" ");
        info[0] = "Room: " + info[0];
        info[1] = "Admin: " + info[1];
        info[2] = "Participants: " + info[2];

        return String.format("%-20s %-20s %-20s", info[0], info[1], info[2]);
    }

    /**
     * pop up message
     * @param msg message to show
     */
    private void popMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}
