import java.util.ArrayList;

/**
 * Project 4 - Store class
 * This class creates a store object with three constructors, private fields storeName, messagesReceived,
 * and a list of Buyers that messaged this store.
 *
 * @author Kevin Zhang, Jalen Mann, Alimzhan Sultanov, Kyle Griffin, and PJ Henwood, lab sec LC2
 *
 * @version November 15, 2022
 *
 */
public class Store {

    /*
    This class was created specifically for the Seller class, it doesn't really have any complicated methods in it.
    */

    private String storeName;
    
    private int messagesReceived;

    private ArrayList<Buyer> userMessaged = new ArrayList<>();

    public Store(String storeName) {
        this.storeName = storeName;
    }

    public Store(String storeName, int messagesReceived) {
        this.storeName = storeName;
        this.messagesReceived = messagesReceived;
    }

    public Store(String storeName, int messagesReceived, ArrayList<Buyer> userMessaged) {
        this.storeName = storeName;
        this.messagesReceived = messagesReceived;
        this.userMessaged = userMessaged;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public int getMessagesReceived() {
        return messagesReceived;
    }

    public ArrayList<Buyer> getUserMessaged() {
        return userMessaged;
    }

    public void addMessagesReceived() {
        messagesReceived++;
    }

    public void addUserMessaged(Buyer b) {
        userMessaged.add(b);
    }
}
