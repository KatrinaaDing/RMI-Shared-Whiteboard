package chatroom;

import chatroom.message.Message;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is used to store the chat room information
 */
public class ChatRoom {

    private String admin;
    private ArrayList<Message> messages;

    public ChatRoom(String admin) {
        this.admin = admin;
        this.messages = new ArrayList<Message>();
    }

    /**
     * Add a message to the chat room
     * @param message the message to be added
     */
    public synchronized void addMessage(Message message) {

        messages.add(message);
    }

    /**
     * Get the messages in the chat room
     * @return the messages
     */
    public ArrayList<Message> getMessages() {
        return this.messages;
    }

    /**
     * Set the messages in the chat room
     * @param messages the messages to be set
     */
    public synchronized void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}
