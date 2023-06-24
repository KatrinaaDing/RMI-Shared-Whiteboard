package server;

import chatroom.ChatRoom;
import chatroom.message.Message;
import logger.WhiteboardLogger;
import remote.IRemoteClient;
import remote.IRemoteRoom;
import whiteboardGUI.DrawComponent.DrawComponent;
import whiteboardGUI.WhiteBoardGUI;

import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class implements the remote interface IRemoteRoom. It is used to create a room
 * on the server side.
 */
public class RemoteRoom extends UnicastRemoteObject implements IRemoteRoom {

    private final String adminName;
    private final String roomName;
    private final ChatRoom chatRoom;
    private ArrayList<DrawComponent> drawComponents = new ArrayList<DrawComponent>();
    private Registry registry;
    private WhiteBoardGUI whiteBoardGUI;
    private ArrayList<String> users = new ArrayList<String>();

    public RemoteRoom(String adminName, String roomName) throws RemoteException {
        this.adminName = adminName;
        this.roomName = roomName;
        this.chatRoom = new ChatRoom(adminName);
        Message joinMsg = new Message(adminName, "Welcome to the chatroom!");
        this.chatRoom.addMessage(joinMsg);
    }

    /**
     * This method is used for a participant to join a room.
     *
     * @param username the username of the participant
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public synchronized void join(String username) throws RemoteException {
        // add the client
        this.users.add(username);
        // get all active clients
        Message msg = new Message(username, createNotification("join", username));
        ArrayList<IRemoteClient> activeClients = getConnections();
        // notify all active clients
        ArrayList<String> newUsers = new ArrayList<String>();
        for (IRemoteClient client : activeClients) {
            client.notifyJoin(msg);
            newUsers.add(client.getUsername());
        }

        // update server records
        this.chatRoom.addMessage(msg);
        this.whiteBoardGUI.setMessages(this.chatRoom.getMessages());
        this.whiteBoardGUI.setUsers(newUsers);
        this.users = newUsers;
    }

    /**
     * This method is used for a participant to request to join a room.
     *
     * @param username the username of the participant
     * @return true if the request is accepted, false otherwise
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public boolean requestJoin(String username) throws RemoteException {
        int result = JOptionPane.showConfirmDialog(
                whiteBoardGUI,"[" + username + "] want to join your room",
                "Join Request",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * This method is used for a participant to leave a room.
     *
     * @param username the username of the participant
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public synchronized void leave(String username) throws RemoteException {
        // remove and unbind the client
        this.users.remove(username);
        try {
            this.registry.unbind("client/" + this.roomName + "/" + username);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        // notify all active clients
        Message leaveMsg = new Message(username, createNotification("leave", username));
        ArrayList<IRemoteClient> activeClients = getConnections();
        ArrayList<String> newUsers = new ArrayList<String>();
        for (IRemoteClient client : activeClients) {
            new Thread( () -> {
                try {
                    client.notifyLeave(leaveMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }).start();
            newUsers.add(client.getUsername());
        }
        // update server records
        this.chatRoom.addMessage(leaveMsg);
        this.whiteBoardGUI.setMessages(this.chatRoom.getMessages());
        this.whiteBoardGUI.setUsers(newUsers);
        this.users = newUsers;
    }

    /**
     * This method is used for the admin to kick a participant
     * @param username the username of the participant
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public synchronized void kick(String username) throws RemoteException {
        // remove the client
        this.users.remove(username);
        // unbind the client
        try {
            IRemoteClient client = (IRemoteClient) registry.lookup("client/" + this.roomName + "/" + username);
            try {
                client.kickedOut("You are removed by the admin");
            } catch (RemoteException e) {
                WhiteboardLogger.info("The client is not active.");
            }
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }

        // notify other clients
        Message leaveMsg = new Message(username, createNotification("kick", username));
        ArrayList<IRemoteClient> activeClients = getConnections();
        ArrayList<String> newUsers = new ArrayList<String>();
        for (IRemoteClient client : activeClients) {
            new Thread(() -> {
                try {
                    client.notifyLeave(leaveMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }).start();
            newUsers.add(client.getUsername());
        }
        // update server records
        this.chatRoom.addMessage(leaveMsg);
        this.whiteBoardGUI.setMessages(this.chatRoom.getMessages());
        this.whiteBoardGUI.setUsers(newUsers);
        this.users = newUsers;
    }

    /**
     * This method is used for the admin to end a meeting
     * @throws Exception if the remote method call fails
     */
    @Override
    public void endMeeting() throws Exception {
        // notify all active clients
        ArrayList<IRemoteClient> activeClients = getConnections();
        for (IRemoteClient client : activeClients) {
            new Thread(() -> {
                try {
                    client.kickedOut("The meeting is ended by the admin.");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        // close the room
        this.registry.unbind("room/" + this.roomName);
        WhiteboardLogger.info("Room " + this.roomName + " is closed.");
    }

    /**
     * This method is used for the server to broadcast a drawing to all participants
     * @param username the username of the participant who add the drawing
     * @param drawComponent the drawing to be broadcast
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public synchronized void broadcastDrawing(String username, DrawComponent drawComponent) throws RemoteException {
        // update server records
        this.drawComponents.add(drawComponent);
        // notify all active clients
        ArrayList<IRemoteClient> activeClients = getConnections();
        for (IRemoteClient client : activeClients) {
            new Thread(() -> {
                try {
                    client.addDrawComponent(username, drawComponent);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        // update GUI
        this.whiteBoardGUI.setDrawingComponents(this.drawComponents);
    }

    /**
     * This method is to create a notification message according to type
     * @param type the type of the notification
     * @param username the username of the participant
     * @return the notification message
     */
    private String createNotification(String type, String username) {
        switch (type) {
            case "join":
                return username + " joins the room";
            case "leave":
                return username + " leaves the room";
            case "kick":
                return username + " is removed by admin";
            case "disconnect":
                return username + " is disconnected";
            default:
                return "Unknown notification type";
        }
    }

    /**
     * This method is used for the server to broadcast a message to all participants
     * @param sender the username of the participant who send the message
     * @param message the message to be broadcast
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public synchronized void broadcastMessage(String sender, String message) throws RemoteException {
        // update server records
        Message newMessage = new Message(sender, message);
        // notify all active clients
        ArrayList<IRemoteClient> activeClients = getConnections();
        for (IRemoteClient client : activeClients) {
            new Thread(() -> {
                try {
                    client.displayMessage(newMessage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        this.chatRoom.addMessage(newMessage);
        // update GUI
        this.whiteBoardGUI.setMessages(this.chatRoom.getMessages());
    }

    /**
     * This method is used for the server to broadcast a room history to all participants
     * @param drawComponents the history of drawings
     * @param messages the history of messages
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public synchronized void broadcastHistory(ArrayList<DrawComponent> drawComponents, ArrayList<Message> messages) throws RemoteException {
        // notify all active clients
        ArrayList<IRemoteClient> activeClients = getConnections();
        for (IRemoteClient client : activeClients) {
            new Thread(() -> {
                try {
                    client.setRoomHistory(drawComponents, messages);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        // set server state
        setRoomHistory(drawComponents, messages);
        // update GUI
        this.whiteBoardGUI.setDrawingComponents(this.drawComponents);
        this.whiteBoardGUI.setMessages(this.chatRoom.getMessages());
    }

    /**
     * This methods is used for admin to clean the board for all participants in the room
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public void broadcastClear() throws RemoteException {
        // notify all active clients
        ArrayList<IRemoteClient> activeClients = getConnections();
        for (IRemoteClient client : activeClients) {
            new Thread(() -> {
                try {
                    client.clearBoard();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        // update GUI
        this.drawComponents.clear();
        this.whiteBoardGUI.clearBoard();
    }

    /**
     * This method is used for the server to get all active connections
     * @return the list of active connections
     * @throws RemoteException if the remote method call fails
     */
    private ArrayList<IRemoteClient> getConnections() throws RemoteException {
        // create placeholder for active and inactive clients
        ArrayList<IRemoteClient> activeClients = new ArrayList<IRemoteClient>();
        ArrayList<String> disconnectedClients = new ArrayList<String>();

        // try to connect to each participant
        for (String user : this.users) {
            try {
                IRemoteClient client = (IRemoteClient) registry.lookup("client/" + this.roomName + "/" + user);
                if(!client.isAlive()){
                    throw new RemoteException("Client is not alive");
                }
                // if connection to client succeeds, add to active clients
                activeClients.add(client);
            } catch (Exception e){
                // if connection to client fails, remove client from list of users
                WhiteboardLogger.info(user + " has disconnected");
                disconnectedClients.add(user);
                // unbind the client from the registry
                try {
                    this.registry.unbind("client/" + this.roomName + "/"+ user);
                } catch (NotBoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        // if any disconnected users, notify all clients for each of them
        if (disconnectedClients.size() > 0) {
            ArrayList<String> newUsers = new ArrayList<String>();

            for (IRemoteClient client : activeClients) {
                // add active username to new users list
                newUsers.add(client.getUsername());
                // notify clients for each inactive client
                for (String username : disconnectedClients) {
                    Message leaveMsg = new Message(username, createNotification("disconnect", username));
                    new Thread(() -> {
                        try {
                            client.notifyLeave(leaveMsg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    // update server records
                    this.chatRoom.addMessage(leaveMsg);
                    this.whiteBoardGUI.setMessages(this.chatRoom.getMessages());
                }
            }
            // update users list
            this.users = newUsers;
            this.whiteBoardGUI.setUsers(newUsers);
        }

        return activeClients;
    }

    // getters and setters

    /**
     * Get the admin name of the room
     * @return the admin name of the room
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public String getAdmin() throws RemoteException {
        return this.adminName;
    }

    /**
     * Get the room information
     * @return the room information
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public String getRoomInfo() throws RemoteException {
        // users list doesn't contain admin, so add 1 to the size
        return this.roomName + " " + this.adminName + " " + (this.users.size() + 1);
    }

    /**
     * Get the room name
     * @return the room name
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public String getRoomName()  throws RemoteException{
        return this.roomName;
    }

    /**
     * Get the list of usernames in the room
     * @return the list of usernames in the room
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public ArrayList<String> getUsers() throws RemoteException {
        return this.users;
    }

    /**
     * Get the list of messages in the room
     * @return the list of messages in the room
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public ArrayList<Message> getMessages() throws RemoteException {
        return this.chatRoom.getMessages();
    }

    /**
     * Get the list of drawings in the room
     * @return the list of drawings in the room
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public ArrayList<DrawComponent> getDrawComponents() throws RemoteException {
        return this.drawComponents;
    }

    /**
     * Get the whiteboard GUI
     * @param whiteBoardGUI the whiteboard GUI
     */
    @Override
    public void setWhiteBoardGUI(WhiteBoardGUI whiteBoardGUI){
        this.whiteBoardGUI = whiteBoardGUI;
    }

    /**
     * Get the current bound registry
     * @param registry the current bound registry
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public void setRegistry(Registry registry) throws RemoteException {
        this.registry = registry;
    }

    /**
     * Set a history (list of drawings + list of messages) for the room
     * @param drawComponents the history of drawings
     * @param messages the history of messages
     * @throws RemoteException if the remote method call fails
     */
    @Override
    public synchronized void setRoomHistory(ArrayList<DrawComponent> drawComponents, ArrayList<Message> messages) throws RemoteException {
        this.drawComponents = drawComponents;
        this.chatRoom.setMessages(messages);
    }


}
