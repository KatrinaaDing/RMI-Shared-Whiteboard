package fileoperator;

import chatroom.message.Message;
import whiteboardGUI.DrawComponent.DrawComponent;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * This class is used to save and open room history
 */
public class FileOperator {
    private ArrayList<DrawComponent> drawComponents;
    private ArrayList<Message> messages;
    private String directoryUrl;
    private String roomName;
    private String timePrefix;
    private String fileName;

    /**
     * This constructor is for saving room history to a new file
     * @param drawComponents draw components to save
     * @param messages messages to save
     * @param directoryUrl directory to save
     * @param roomName room name
     */
    public FileOperator (ArrayList<DrawComponent> drawComponents, ArrayList<Message> messages, String directoryUrl, String roomName) {
        this.drawComponents = drawComponents;
        this.messages = messages;
        this.directoryUrl = directoryUrl;
        this.roomName = roomName;
        this.setTimePrefix();
    }

    /**
     * This constructor is for saving room history to an existing file and open history files
     * @param directoryUrl the history directory to open
     */
    public FileOperator (String directoryUrl)  {
        this.directoryUrl = directoryUrl; // this url include the name
        this.setTimePrefix();
    }


    /**
     * Write the draw components and messages to files
     *
     * @throws Exception if the directory is not valid
     */
    public void writeToFile() throws Exception {
        if (this.directoryUrl == null) {
            System.out.println("Please set the directoryUrl first.");
            return;
        }
        // create directory with room name
        File file = null;
        try {
            String fileName = this.directoryUrl+"/"+this.roomName+this.timePrefix;
            file = new File(fileName);
            if (file.exists()) {
                throw new Exception("Room history already exists. Please use another directory, or use 'Save' function");
            }
            file.mkdirs();
            setFileName(fileName);
        } catch (Exception e) {
            throw new Exception("Cannot create directory: " + e.getMessage());

        }
        // create files
        FileOutputStream drawingFile = new FileOutputStream(file.getPath() + "/drawComponents");
        FileOutputStream chatFile = new FileOutputStream(file.getPath() + "/messages");
        ObjectOutputStream drawingOut = new ObjectOutputStream(drawingFile);
        ObjectOutputStream chatOut = new ObjectOutputStream(chatFile);

        // write objects to files
        drawingOut.writeObject(this.drawComponents);
        chatOut.writeObject(this.messages);

        // close the stream
        drawingOut.close();
        chatOut.close();
    }

    /**
     * Save the current draw components and messages to an existing file
     *
     * @throws Exception if the directory is not valid
     */
    public void saveToExistingFile() throws Exception {
        if (this.directoryUrl == null) {
            System.out.println("Please set the directoryUrl first.");
            return;
        }
        // find the room history directory
        File file = null;
        setFileName(this.directoryUrl);
        try {
            file = new File(this.directoryUrl);
            if (!file.exists()) {
                throw new Exception("Room history file not exists. Please use 'Save As' function");
            }
        } catch (Exception e) {
            throw new Exception("Cannot create directory: " + e.getMessage());
        }
        // create files
        FileOutputStream drawingFile = new FileOutputStream(file.getPath() + "/drawComponents");
        FileOutputStream chatFile = new FileOutputStream(file.getPath() + "/messages");
        ObjectOutputStream drawingOut = new ObjectOutputStream(drawingFile);
        ObjectOutputStream chatOut = new ObjectOutputStream(chatFile);

        // write objects to files
        drawingOut.writeObject(this.drawComponents);
        chatOut.writeObject(this.messages);

        // close the stream
        drawingOut.close();
        chatOut.close();
    }

    /**
     * Read the draw components and messages from history files
     *
     * @throws Exception if the directory is not valid
     */
    public void readFromFile() throws Exception {
        if (this.directoryUrl == null) {
            System.out.println("Please set the directoryUrl first.");
            return;
        }
        // open files
        setFileName(this.directoryUrl);
        FileInputStream drawingFile = new FileInputStream(this.directoryUrl + "/drawComponents");
        FileInputStream chatFile = new FileInputStream(this.directoryUrl + "/messages");
        ObjectInputStream drawingIn = new ObjectInputStream(drawingFile);
        ObjectInputStream chatIn = new ObjectInputStream(chatFile);

        // read objects from file
        this.drawComponents = (ArrayList<DrawComponent>) drawingIn.readObject();
        this.messages = (ArrayList<Message>) chatIn.readObject();

        // close the stream
        drawingIn.close();
        chatIn.close();
    }

    // getters and setters

    /**
     * Get the draw components
     *
     * @return the draw components
     */
    public ArrayList<DrawComponent> getDrawComponents() {
        return new ArrayList<>(this.drawComponents);
    }

    /**
     * Get the messages
     *
     * @return the messages
     */
    public ArrayList<Message> getMessages() {
        return new ArrayList<>(this.messages);
    }

    /**
     * Get the file name
     *
     * @return the file name
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Set the directory name
     *
     * @param name the directory name
     */
    private void setFileName(String name) {
        this.fileName = name;
    }

    /**
     * Set the time prefix (to current time)
     */
    private void setTimePrefix() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        this.timePrefix = "_"+now.format(formatter);
    }


}
