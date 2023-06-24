package chatroom.message;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This class is used to store the message information
 */
public class Message implements Serializable {
    private String username;
    private String message;
    private LocalDateTime timestamp;

    public Message(String username, String message) {
        this.username = username;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    // getters

    /**
     * Get the message
      * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the username of the sender
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Format the message
     * @return the formatted message
     */
    @Override
    public String toString() {
        String date = this.timestamp.toString().split("T")[0];
        String time = this.timestamp.toString().split("T")[1].split("\\.")[0];
        return "[" + username + "] "+ date + " " + time + "\n" + message;
    }
}
