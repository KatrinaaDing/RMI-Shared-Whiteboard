package remote;

import chatroom.message.Message;
import whiteboardGUI.DrawComponent.DrawComponent;
import whiteboardGUI.WhiteBoardGUI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;

/**
 * This interface defines the methods that can be invoked remotely by the server.
 */
public interface IRemoteClient extends Remote {

    // methods for connection with the server
    public void disconnect() throws RemoteException, Exception;
    public void connect(Registry registry, String roomName) throws RemoteException, Exception;
    public void kickedOut(String msg) throws RemoteException;
    public boolean isAlive() throws RemoteException;

    // methods for sending messages
    public void sendMessage(String message) throws RemoteException;
    public void addDrawComponent(String username, DrawComponent drawComponent) throws RemoteException;

    // methods for receiving messages
    public void displayMessage(Message message) throws RemoteException;
    public void notifyLeave(Message msg) throws RemoteException;
    public void notifyJoin(Message msg) throws RemoteException;
    public void clearBoard() throws RemoteException;

    // getters and setters
    public String getUsername() throws RemoteException;
    public IRemoteRoom getRemoteRoom() throws  RemoteException;
    public void setWhiteBoardGUI(WhiteBoardGUI whiteBoardGUI) throws RemoteException;
    public void setRoomHistory(ArrayList<DrawComponent> drawComponents, ArrayList<Message> messages) throws RemoteException;
}
