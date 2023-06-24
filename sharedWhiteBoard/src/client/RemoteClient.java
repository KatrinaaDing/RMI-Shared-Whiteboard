package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import chatroom.message.Message;
import logger.WhiteboardLogger;
import remote.IRemoteClient;
import remote.IRemoteRoom;
import whiteboardGUI.DrawComponent.DrawComponent;
import whiteboardGUI.WhiteBoardGUI;

import java.rmi.registry.Registry;


/**
 * This class retrieves a reference to the remote object from the RMI registry. It
 * invokes the methods on the remote object as if it was a local object of the type of the
 * remote interface.
 */
public class RemoteClient extends UnicastRemoteObject implements IRemoteClient {
    private Registry registry;
    private String username;
    private String roomName;
    private IRemoteRoom remoteRoom;
    private WhiteBoardGUI whiteBoardGUI;

    public RemoteClient(String username) throws RemoteException{
        WhiteboardLogger.info("Creating new client");
        this.username = username;
    }

    /**
     * This method is called when the client is leaving the room.
     *
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void disconnect() throws RemoteException {
        try {
            this.remoteRoom.leave(username);
        } catch (RemoteException e) {
            this.kickedOut("Admin is disconnected");
        }
    }

    /**
     * This method is called by the server to notify the client that it has been kicked out of the room.
     *
     * @param msg the message to be displayed
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void kickedOut(String msg) throws RemoteException {
        try {
            this.registry.unbind("client/" + this.roomName + "/" + this.username);
        } catch (NotBoundException ex) {
            WhiteboardLogger.error("Not bound exception");
        } finally {
            this.whiteBoardGUI.kickedOut(msg);
        }
    }

    /**
     * This method is called by the server to display a message on the client's GUI.
     *
     * @param message the message to be displayed
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void displayMessage(Message message) throws RemoteException {
        this.whiteBoardGUI.addMessage(message);
    }

    /**
     * This method is called by the server to add a drawing to the client's GUI.
     * @param username the username of the user who drew the component
     * @param drawComponent the drawing component
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void addDrawComponent(String username, DrawComponent drawComponent) throws RemoteException {
        this.whiteBoardGUI.addDrawComponent(drawComponent);
    }

    /**
     * This method is called by the server to clear the draw board
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void clearBoard() throws RemoteException {
        this.whiteBoardGUI.clearBoard();
    }

    /**
     * This method is called when the client wants to send a message to the server.
     *
     * @param message the message to be sent
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void sendMessage(String message) throws RemoteException {
        try {
            this.remoteRoom.broadcastMessage(this.username, message);
        } catch (RemoteException e) {
            this.kickedOut("Admin is disconnected");
        }
    }

    /**
     * This method is called by the server to notify the client that someone left the room.
     *
     * @param msg the message to be displayed
     */
    @Override
    public void notifyLeave(Message msg) throws RemoteException {
        this.displayMessage(msg);
        this.whiteBoardGUI.removeUser(msg.getUsername());
    }

    /**
     * This method is called by the server to notify the client that someone joined the room.
     *
     * @param msg the message to be displayed
     */
    @Override
    public void notifyJoin(Message msg) throws RemoteException {
        this.displayMessage(msg);
        this.whiteBoardGUI.addUser(msg.getUsername());
    }

    /**
     * This method is called by the server to check if the client is still alive.
     *
     * @return true if the client is still alive
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }

    /**
     * This method is called by the client to connect to the server.
     * @param registry the registry of the server
     * @param roomName the name of the room
     * @throws RemoteException if the remote invocation fails
     * @throws Exception if the lookup fails
     */
    @Override
    public void connect(Registry registry, String roomName) throws RemoteException, Exception {
        this.registry = registry;
        this.roomName = roomName;

        IRemoteRoom remoteRoom = (IRemoteRoom) registry.lookup("room/" + roomName);
        this.remoteRoom = remoteRoom;
        registry.rebind("client/" + roomName + "/" + username, this);
        this.remoteRoom.join(username);
    }

    // getters and setters

    /**
     * Get the client's username.
     *
     * @return the username
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public String getUsername() throws RemoteException {
        return this.username;
    }

    /**
     * Get the room name.
     *
     * @return the room name
     * @throws RemoteException
     */
    @Override
    public IRemoteRoom getRemoteRoom() throws RemoteException {
        return this.remoteRoom;
    }

    /**
     * Set the client's GUI.
     * @param whiteBoardGUI the client's GUI
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public void setWhiteBoardGUI(WhiteBoardGUI whiteBoardGUI) throws RemoteException {
    	this.whiteBoardGUI = whiteBoardGUI;
    }


    /**
     * This method is called by the server to set the room history on the client's GUI.
     *
     * @param drawComponents the drawing components
     * @param messages the messages
     * @throws RemoteException if the remote invocation fails
     */
    @Override
    public synchronized void setRoomHistory(ArrayList<DrawComponent> drawComponents, ArrayList<Message> messages) throws RemoteException {
        this.whiteBoardGUI.setMessages(messages);
        this.whiteBoardGUI.setDrawingComponents(drawComponents);
    }

}
