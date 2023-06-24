package server;

import logger.WhiteboardLogger;
import whiteboardGUI.LoginGUI;

import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * This class starts the Whiteboard application.
 * Reference: The code are modified from the sample code given in lectures.
 */
public class RMIRoomServer {

    public static void main(String[] args) {
        // set default port and host
        int port = 1099;
        String host = "localhost";

        // get custom port from command line
        try{
            port = Integer.parseInt(args[1]);
        } catch (Exception e) {
            WhiteboardLogger.error("Invalid port number. Using default port: " + port);
        }
        // get custom host from command line
        try {
            host = args[0];
        } catch (Exception e) {
            WhiteboardLogger.error("Invalid host address. Using default host: localhost");
        }
        // set the timeout for RMI
        System.setProperty("sun.rmi.transport.proxy.connectTimeout", "5000");
        // start the login GUI
        try {
            new LoginGUI(host, port);
        } catch (RemoteException e) {
            WhiteboardLogger.error("Failed to connect to registry.");
            System.exit(0);
        }
    }

}
