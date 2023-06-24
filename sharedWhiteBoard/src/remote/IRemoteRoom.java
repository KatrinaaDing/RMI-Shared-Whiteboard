package remote;

import chatroom.message.Message;
import whiteboardGUI.DrawComponent.DrawComponent;
import whiteboardGUI.WhiteBoardGUI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;

/**
 * This interface defines the methods that can be invoked remotely by the client.
 */
public interface IRemoteRoom extends Remote {
    // methods for participants management
    public void join(String username) throws RemoteException;
    public boolean requestJoin(String username) throws RemoteException;
    public void leave(String username) throws RemoteException;
    public void kick(String username) throws RemoteException;
    public void endMeeting() throws Exception;

    // methods for communication
    public void broadcastDrawing(String username, DrawComponent drawComponent) throws RemoteException;
    public void broadcastMessage(String sender, String message) throws RemoteException;
    public void broadcastHistory(ArrayList<DrawComponent> drawComponents, ArrayList<Message> messages) throws RemoteException;
    public void broadcastClear() throws RemoteException;

    // setters and getters
    public void setRegistry(Registry registry) throws RemoteException;
    public void setWhiteBoardGUI(WhiteBoardGUI whiteBoardGUI) throws RemoteException;
    public void setRoomHistory(ArrayList<DrawComponent> drawComponents, ArrayList<Message> messages) throws RemoteException;
    public String getAdmin() throws RemoteException;
    public String getRoomName() throws RemoteException;
    public ArrayList<String> getUsers() throws RemoteException;
    public ArrayList<Message> getMessages() throws RemoteException;
    public ArrayList<DrawComponent> getDrawComponents() throws RemoteException;
    public String getRoomInfo() throws RemoteException;
}
