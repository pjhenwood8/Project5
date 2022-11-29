import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Project 4 - Message
 * This class creates a message class that contains an id for a message, number of total messages, time, sender,
 * receiver, message string, and booleans if deleted. Contains constructors, getters and setters, and toString method.
 *
 * @author Kevin Zhang, Jalen Mann, Alimzhan Sultanov, Kyle Griffin, and PJ Henwood, lab sec LC2
 *
 * @version November 15, 2022
 *
 */
public class Message {
    private final int id;                     // unique field for every message
    private static int count = 0;            // count is needed to assign unique value of ID to each message
    private final String time;                // time at which message was sent
    private String sender;                    // username of the sender
    private String receiver;                    // username of the receiver
    private String message;                     // content of the message itself
    private boolean delBySender;                // indicates whether message was deleted by the sender of the message
    private boolean delByReceiver;              // indicates whether message was deleted by the receiver of the message

    /*
    This constructor which has a lot of value is used to assign values from the file to the Message object. We use it when we read the file, and parse through
    already existing messages
    * */
    public Message(int id, String time, String sender, String receiver, String message, boolean delBySender, boolean delByReceiver) {
        count++;
        this.id = id;
        this.time = time;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.delBySender = delBySender;
        this.delByReceiver = delByReceiver;
    }

    /*
    This constructor is used when CREATING NEW MESSAGE, since time for the new message can be calculated, and other parameters are set to default values, when
    just creating the new message
    * */
    public Message(String sender, String receiver, String message) {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");       // this lines are responsible for saving current time into the field
        this.time = myDateObj.format(myFormatObj);
        count++;
        this.id = count;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        delByReceiver = false;      //default values
        delBySender = false;        //default values
    }

    //Most of the methods here are default getters and setters, nothing that much to see
    public boolean isDelBySender() {
        return delBySender;
    }

    public boolean isDelByReceiver() {
        return delByReceiver;
    }

    public void setDelBySender(boolean delBySender) {
        this.delBySender = delBySender;
    }

    public void setDelByReceiver(boolean delByReceiver) {
        this.delByReceiver = delByReceiver;
    }

    public int getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // we use toString() method in the Menu.java in the main method, when we want to display Message object as a nice-looking
    // feed lines.  Arrow indicated the sender and receiver of the message.
    public String toString() {
        return String.format("%s   (%s -> %s)%n%s%n", time, sender, receiver, message);
    }
}
