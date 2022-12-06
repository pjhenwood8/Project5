import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Project 4 - Menu
 * This program prompts the user to log in or create an account.
 * After logging in the user can then edit their account, message
 * other users, or view stats about themselves.
 *
 * @author Kevin Zhang, Jalen Mann, Alimzhan Sultanov, Kyle Griffin, and PJ Henwood, lab sec LC2
 *
 * @version November 15, 2022
 *
 */

public class Menu {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        boolean online = true;
        ArrayList<User> users = readUsers("login.csv");                 // Each line in the "login.csv" file is a User object, using special method we read whole file into an ArrayList of Users
        ArrayList<Store> stores = readStores("stores.csv", users);      // We do the same thing with the stores objects
        boolean serverConnection = false;
        Socket socket = null;
        while (!serverConnection) {
            int port = 0;
            do {
                try {
                    port = Integer.parseInt(JOptionPane.showInputDialog(null,
                            "Enter port number (4242) ", "Client", JOptionPane.QUESTION_MESSAGE));
                } catch (NumberFormatException n2) {
                    return;
                }
            } while (port == 0);
            try {
                socket = new Socket("localhost", port);
                serverConnection = true;
            } catch (ConnectException ce) {
                JOptionPane.showMessageDialog(null,
                        "A connection couldn't be made", "Client", JOptionPane.ERROR_MESSAGE);
            }
        }
        BufferedReader bfrServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pwServer = new PrintWriter(socket.getOutputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        while (online) {
            String title = "Welcome to the Marketplace Messaging System!";
            String[] options;
            boolean loggingIn = true;
            boolean loggedIn = false;
            User currUser = null;
            while (loggingIn) {                                                  // An infinite loop that breaks when
                // user is able to log in
                // User is presented with 3 options: log in into existing acc, create new acc, or exit the program
                options = new String[]{"Login", "Create Account", "Exit"};
                int choice = JOptionPane.showOptionDialog(null, "Please select an option to proceed", title,
                        JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[2]);
                pwServer.write(choice);
                pwServer.flush();
                String email = "";
                String pass = "";
                String userName = "";
                boolean invBuyer = true;
                switch (choice) {
                    case 0:
                        // If user wants to log in into existing acc, if it's successful, infinite loop breaks
                        email = JOptionPane.showInputDialog(null, "Please enter your email:", title,
                                JOptionPane.PLAIN_MESSAGE);
                        pwServer.write(email);
                        pwServer.println();
                        pwServer.flush();
                        pass = JOptionPane.showInputDialog(null, "Please enter your password:", title,
                                JOptionPane.PLAIN_MESSAGE);
                        pwServer.write(pass);
                        pwServer.println();
                        pwServer.flush();
                        if (currUser != null) {
                            loggingIn = false;                  // to end an infinite loop
                        }
                        break;
                    case 1:
                        do {
                            email = JOptionPane.showInputDialog(null, "A valid email contains an @ sign and has no commas\n" +
                                    "Please enter a valid email:", title, JOptionPane.PLAIN_MESSAGE);
                            if (email.contains(",") || !email.contains("@")) {
                                JOptionPane.showMessageDialog(null, "That email was not valid", title, JOptionPane.WARNING_MESSAGE);
                                email = "";
                            } else {
                                pwServer.write(email);
                                pwServer.println();
                                pwServer.flush();
                                System.out.printf("Sent to server:\n%s\n", email);
                            }
                        } while (email.isEmpty());

                        //Loops until a valid username is inputted, a valid username being a username that is
                        //not previously used and does not have a comma
                        do {
                            userName = JOptionPane.showInputDialog(null, "A valid username contains no commas\nPlease enter a valid" +
                                    " username: ", title, JOptionPane.PLAIN_MESSAGE);

                            if (userName.contains(",") || userName.equals("")) {
                                JOptionPane.showMessageDialog(null, "That user name was not valid", title, JOptionPane.WARNING_MESSAGE);
                                userName = "";
                            } else {
                                pwServer.write(userName);
                                pwServer.println();
                                pwServer.flush();
                                System.out.printf("Sent to server:\n%s\n", userName);
                            }
                        } while (userName.isEmpty());

                        //Loops until a password is inputted
                        do {
                            pass = JOptionPane.showInputDialog(null, "Please enter a password: ", title, JOptionPane.PLAIN_MESSAGE);
                            if (pass == null || pass.equals("")) {
                                JOptionPane.showMessageDialog(null, "That password is not valid", title,
                                        JOptionPane.WARNING_MESSAGE);
                                pass = "";
                            } else {
                                pwServer.write(pass);
                                pwServer.println();
                                pwServer.flush();
                                System.out.printf("Sent to server:\n%s\n", pass);
                            }
                        } while (pass.isEmpty());

                        //Loops until buyer/seller is inputted
                        while(invBuyer){
                            options = new String[]{"Buyer", "Seller"};
                            choice = JOptionPane.showOptionDialog(null, "Are you a buyer or seller?", title,
                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                            pwServer.write(choice);
                            pwServer.flush();
                            if (choice == 0 || choice == 1) {
                                invBuyer = false;
                            }
                        }
                        break;
                    default:                    // To exit program
                        loggingIn = false;
                        online = false;           // When online is false, program stops working
                        break;
                }
                try {
                    currUser = (User) ois.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (currUser != null) {
                    loggingIn = false;
                } else {
                    if (choice == 0) {
                        JOptionPane.showMessageDialog(null, "Your email or password was incorrect", "Login",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "That account couldn't be created because that email " +
                                        "or username is already in use", "Login", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }

            // Confirmation message, when user is able to log in
            JOptionPane.showMessageDialog(null, "Successfully logged in as " + currUser.getUsername(), "Marketplace " +
                    "Messaging System", JOptionPane.INFORMATION_MESSAGE);
            loggedIn = true;

            int numOfBuyers = 0;
            int numOfSellers = 0;
            for (User u : users) {
                if (u instanceof Buyer) {
                    numOfBuyers++;
                } else if (u instanceof Seller) {
                    numOfSellers++;
                }
            }
            String[] buyers = new String[numOfBuyers];
            int n = 0;
            for (User u : users) {
                if (u instanceof Buyer) {
                    buyers[n] = u.getUsername();
                    n++;
                }
            }
            String[] sellers = new String[numOfSellers];
            n = 0;
            for (User u : users) {
                if (u instanceof Seller) {
                    sellers[n] = u.getUsername();
                    n++;
                }
            }
            String[] userArr = new String[users.size()];
            n = 0;
            for (User u : users) {
                userArr[n] = u.getUsername();
                n++;
            }

            while (loggedIn) {
                if (currUser != null) {

                    /*
                    When user logs in, he is presented with 4 options:
                    1) Messages is the part of the program where user is able to send messages to either Customers or Sellers, depending on who is User itself
                    2) View statistics about user like most used words, or stores to which he messages the most
                    3) Account is for changing your password or email
                    0) Exit is to log off from the program
                     */
                    options = new String[]{"Messages", "Statistics", "Account", "Exit"};
                    int choice = JOptionPane.showOptionDialog(null, "Select an option to proceed", "Main Menu",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[3]);
                    pwServer.write(choice);
                    pwServer.flush();
                    switch (choice) {
                        case 0:                     // If user chooses to Message another person
                            title = String.format("%s - Message Log%n", currUser.getUsername());
                            StringBuilder messageHist;
                            if (currUser instanceof Seller) {                     //If user is Seller then this part of the code will run for him in the message section
                                while (true) {
                                    ArrayList<Message> messageHistory;
                                    /*
                                    parseUsers(user) parse through messages.csv and collects a list of users, with which
                                    user had conversations before. That way we are able to avoid situation where messages not
                                    related to the user are being used.
                                    */
                                    String[] listOfUsers = parseUsers(currUser);
                                    options = new String[listOfUsers.length + 2];
                                    options[0] = "Start new dialog";
                                    if (listOfUsers.length + 1 - 1 >= 0)
                                        System.arraycopy(listOfUsers, 0, options, 1, listOfUsers.length + 1 - 1);
                                    options[options.length - 1] = "Exit";
                                    int receiveUser = JOptionPane.showOptionDialog(null,
                                            "Select user to view messages or start a dialog with a new user",
                                            title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                            options, options[options.length - 1]);
                                    if (receiveUser == 0) {
                                        // dialog with new user
                                        String newUser = (String) JOptionPane.showInputDialog(null, "Select buyer to message",
                                                title, JOptionPane.QUESTION_MESSAGE, null, buyers,
                                                buyers[0]);         // Enter name of the new user
                                        boolean alreadyMessaged = false;
                                        for (String u : listOfUsers) {
                                            if (u.equals(newUser)) {
                                                alreadyMessaged = true;                 // if you already messaged the user before, it will show this message that you already messaged him before
                                                JOptionPane.showMessageDialog(null, "You've already messaged this" +
                                                        " user!", title, JOptionPane.ERROR_MESSAGE, null);
                                            }
                                        }
                                        boolean flag = true;           // This flag is responsible for identifying if sender and receiver are the same type
                                        // This flag is responsible for showing if user exists
                                        boolean flag2 = true;          // This flag is responsible for checking if user to which you are texting blocked you, or you blocked that user before
                                        for (User value : users) {
                                            if (value.getUsername().equals(newUser)) {
                                                if (value instanceof Seller) {
                                                    JOptionPane.showMessageDialog(null, "You can't write to " +
                                                            "Seller, because you are Seller yourself", title, JOptionPane.ERROR_MESSAGE, null);
                                                    flag = false;       // means that user to which you are trying to text is also an instance of Seller, which should be possible
                                                                        // you should only be able to text Buyers as a Seller
                                                } else if (currUser.getBlockedUsers().contains(value) || value.getBlockedUsers().contains(currUser)) {
                                                    JOptionPane.showMessageDialog(null, "You can't write to this " +
                                                            "user because they are blocked", title, JOptionPane.ERROR_MESSAGE, null);
                                                    flag2 = false;      // flag2 = false; means that one user blocked the other
                                                }
                                            }
                                        }
                                        if (flag && flag2 && !alreadyMessaged) {     // this code runs if
                                            // user exists, user is Buyer, you didn't block each other
                                            String mes = JOptionPane.showInputDialog(null, "Write your hello message first!",
                                                    title, JOptionPane.PLAIN_MESSAGE);               // user enters the message he would want to send to new user
                                            ArrayList<Message> temp = currUser.getMessages();  // creates new ArrayList with user messages
                                            temp.add(new Message(currUser.getUsername(), newUser, mes));    // adds new message to that ArrayList
                                            currUser.setMessages(temp);                        // updates the messages field on the user
                                            messageHistory = parseMessageHistory(currUser, newUser);     // after the
                                            // messages field was updated, we update the messageHistory and print that out
                                            messageHist = new StringBuilder(String.format("Message " +
                                                    "History: %s - %s%n", currUser.getUsername(), newUser));
                                            for (Message message : messageHistory) {
                                                messageHist.append(message.toString());
                                            }
                                        }
                                    } else if (receiveUser >= 1 && receiveUser != options.length - 1) {           // if user doesn't choose to start
                                        // new dialog or exit the program
                                                                             // receiveUser is to view conversations you had before with other users
                                        while (true) {
                                            messageHistory = parseMessageHistory(currUser, listOfUsers[receiveUser - 1]);        // update messageHistory to print that out
                                            messageHist = new StringBuilder(String.format("Message " +
                                                    "History: %s - " +
                                                            "%s%n----------------------------------------%n",
                                                            currUser.getUsername(), listOfUsers[receiveUser - 1]));
                                            for (Message value : messageHistory) {
                                                if (value.getMessage().contains("\\n")) {        // this part of the code is here in case if message has multiple lines in it
                                                    String ansMes = value.getMessage().replaceAll("\\\\n", "\n");  // it replaces signs of new lines, to actual new lines
                                                    String ans = String.format("%s   (%s -> %s)%n%s%n", value.getTime(), value.getSender(), value.getReceiver(), ansMes);  // same implementation as in Message class, but with specific message string
                                                    messageHist.append(ans);
                                                } else
                                                    messageHist.append(value);
                                                // if it's regular one line message,
                                                // then it uses basic toString() method of Message class
                                            }
                                            /* User is presented with 5 options of what they can in the Message part with specific user
                                               1) Write new message
                                               when writing new message you are presented with 2 options
                                                    1) Either write a regular message
                                                        you type a message, and it sends it to the receiver
                                                    2) Upload a txt file, contents of which will be included in the message
                                                        it will read through the file content and add new lines where needed
                                               2) Edit message
                                               when editing messages you can only edit messages that you send to another user, YOU CAN"T EDIT ANOTHER USERS MESSAGES
                                               3) Delete message
                                               when deleting messages, message won't be deleted for another user. There are special boolean fields in the messages.csv
                                               that are responsible for not showing message if it was deleted from either side of the conversation
                                               Compared to EDIT MESSAGE, you can delete whether message you wish for, because it's only one-sided
                                               0) Exit, it returns you back to the userList of all users you had conversations before
                                               -1) If you want to export your current message history, all you need is to enter -1 and write name of the csv file you want to save your changes to
                                            */
                                            options = new String[]{"Write Message", "Edit Message", "Delete " +
                                                    "Message", "Export History to CSV", "Exit"};
                                            int optionChoice = JOptionPane.showOptionDialog(null,
                                                    messageHist.toString() + "\nSelect an option to proceed",
                                                    title, JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE, null, options, options[4]);
                                            if (optionChoice == 0) {            // writing new messages
                                                // you are presented with two options as described before
                                                // 1 - regular message          2 - upload a txt file
                                                options = new String[]{"Send Message", "Upload File"};
                                                int fileOrText = JOptionPane.showOptionDialog(null, "Do you want " +
                                                        "to send a message or upload a text file?", title,
                                                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                                        null, options, options[0]);
                                                if (fileOrText == 0) {       // regular message
                                                    String mes = JOptionPane.showInputDialog(null, "Write your message: ",
                                                            title, JOptionPane.PLAIN_MESSAGE);
                                                    ArrayList<Message> temp = currUser.getMessages();
                                                    temp.add(new Message(currUser.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                    currUser.setMessages(temp);        // updates the messages field of the user to the renewed messageHistory
                                                } else if (fileOrText == 1) {      //uploading files
                                                    String fileName = JOptionPane.showInputDialog(null, "Enter " +
                                                                    "name of txt file: ", title, JOptionPane.PLAIN_MESSAGE);    // enters name of the file
                                                    String mes;
                                                    try {
                                                        ArrayList<String> tempArr = new ArrayList<>();
                                                        BufferedReader bfr = new BufferedReader(new FileReader(fileName));
                                                        String st;
                                                        while ((st = bfr.readLine()) != null) {
                                                            tempArr.add(st);                     // reads whole file and saves lines to ArrayList
                                                        }
                                                        mes = String.join("\\n",tempArr);              // combine all lines in the file by \\n which shows up as \n in the messages.csv file
                                                                                                               // we read it as new line when writing all messages
                                                        ArrayList<Message> temp = currUser.getMessages();
                                                        temp.add(new Message(currUser.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                        currUser.setMessages(temp);                  // updates the messages field of the user
                                                    }
                                                    catch (FileNotFoundException e) {         // if user enters file that does not exist
                                                        JOptionPane.showMessageDialog(null, "That file could not be " +
                                                                "found", "Error", JOptionPane.ERROR_MESSAGE);
                                                    }
                                                }
                                                saveMessages(currUser);
                                            } else if (optionChoice == 1) {          // editing messages
                                                messageHistory = parseMessageHistory(currUser,
                                                        listOfUsers[receiveUser - 1]);
                                                ArrayList<Message> userIsSender = new ArrayList<>();         // here only messages that are sends by the current user will be saved
                                                int i = 0;
                                                int z = 0;
                                                messageHist = new StringBuilder(String.format("Message " +
                                                                "History: %s - " +
                                                                "%s%n----------------------------------------%n",
                                                        currUser.getUsername(), listOfUsers[receiveUser - 1]));
                                                while (z < messageHistory.size()) {
                                                    if (messageHistory.get(z).getSender().equals(currUser.getUsername())) {      // checks if message is sent by the main user
                                                        userIsSender.add(messageHistory.get(z));
                                                        // if message is sent by the main user, the number will appear next to it
                                                        messageHist.append(String.format("[%d] " + messageHistory.get(z).toString(), i + 1));
                                                        i++;
                                                    }
                                                    z++;
                                                }
                                                if (i > 0) {
                                                    Integer[] messageNums = new Integer[i];
                                                    for (int j = 0; j < i; j++) {
                                                        messageNums[j] = j + 1;
                                                    }
                                                    choice = (int) JOptionPane.showInputDialog(null,
                                                            messageHist.toString() + "\nSelect message to edit",
                                                            title, JOptionPane.QUESTION_MESSAGE, null, messageNums,
                                                            messageNums[0]); //user chooses which message available for him to edit he wants to edit
                                                    String msg = JOptionPane.showInputDialog(null,"Enter new " +
                                                            "message: ", title, JOptionPane.PLAIN_MESSAGE);
                                                    // user enters the message to which user wants to change his message
                                                    Message temp = userIsSender.get(choice - 1);       // we grab value form the userIsSender which stores only messages where main user is sender
                                                    for (Message message : messageHistory) {
                                                        if (message.getId() == temp.getId()) {
                                                            message.setMessage(msg);                   // when we find that message in the main message history, we change its text
                                                        }
                                                    }
                                                    saveMessages(currUser);
                                                } else {
                                                    JOptionPane.showMessageDialog(null, "No messages to edit with" +
                                                                    " this user", title, JOptionPane.ERROR_MESSAGE);
                                                }
                                            } else if (optionChoice == 2) {             // deleting messages
                                                messageHistory = parseMessageHistory(currUser,
                                                        listOfUsers[receiveUser - 1]);       // we save message history
                                                ArrayList<Message> userIsSender = new ArrayList<>();         // I guess here I was kind of lazy, so userIsSender now stores every message in it
                                                // because in deleting messages it doesn't really matter if you aren't creator of the message
                                                // since you can delete whether message you wish for
                                                int i = 0;
                                                messageHist = new StringBuilder(String.format("Message " +
                                                                "History: %s - " +
                                                                "%s%n----------------------------------------%n",
                                                        currUser.getUsername(), listOfUsers[receiveUser - 1]));
                                                while (i < messageHistory.size()) {
                                                    userIsSender.add(messageHistory.get(i));              // adding every message into the userIsSender arraylist
                                                    // printing every message with a number next to it
                                                    messageHist.append(String.format("[%d] " + messageHistory.get(i).toString(), i + 1));
                                                    i++;
                                                }
                                                if (i > 0) {
                                                    Integer[] messageNums = new Integer[i];
                                                    for (int j = 0; j < i; j++) {
                                                        messageNums[j] = j + 1;
                                                    }
                                                    choice = (int) JOptionPane.showInputDialog(null,
                                                            messageHist.toString() + "\nSelect message to delete",
                                                            title, JOptionPane.QUESTION_MESSAGE, null, messageNums,
                                                            messageNums[0]);  // user chooses which message to delete
                                                    Message temp = userIsSender.get(choice - 1);        // we assign the message user chose to Message temp variable
                                                    ArrayList<Message> allUserMessages = currUser.getMessages();
                                                    for (int j = 0; j < allUserMessages.size(); j++) {
                                                        if (allUserMessages.get(j).getId() == temp.getId()) {         // finding temp message in the main allUserMessages ArrayList
                                                            if (temp.getSender().equals(currUser.getUsername()))          // if main user was sender
                                                                allUserMessages.get(j).setDelBySender(true);          // then message becomes invisible to the sender
                                                            else                                                      // if main user was receiver
                                                                allUserMessages.get(j).setDelByReceiver(true);        // then message becomes invisible to the receiver
                                                            currUser.setMessages(allUserMessages);                        // updates the messages field of the user after deleting the message
                                                            break;
                                                        }
                                                    }
                                                    currUser.refreshMessages();            // refreshMessages is used to remove some messages in the messages field of the user, because we need to be
                                                    // manually remove some messages in the messages field. setMessages isn't enough, because it doesn't actually remove messages
                                                    // it only updates its values
                                                } else {
                                                    JOptionPane.showMessageDialog(null, "No messages to delete",
                                                            title, JOptionPane.ERROR_MESSAGE);
                                                }
                                            } else if (optionChoice == 3) {            // exporting messages
                                                String fileName = JOptionPane.showInputDialog(null, "Enter " +
                                                        "name of the file to which you want to export your " +
                                                        "message history", title, JOptionPane.QUESTION_MESSAGE); // enters the file name
                                                PrintWriter pw = new PrintWriter(new FileOutputStream(fileName,false));
                                                for (Message msg : messageHistory) {
                                                    // this line writes Message object in the same manner as it does in main "messages.csv" file
                                                    String ans = String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", msg.getId(), msg.getTime(), msg.getSender(), msg.getReceiver(), msg.getMessage(), msg.isDelBySender(), msg.isDelByReceiver());
                                                    pw.write(ans);
                                                    pw.println();
                                                    pw.flush();
                                                }
                                                // confirmation that history was saved
                                                JOptionPane.showMessageDialog(null, "Your message history was successfully " +
                                                        "saved to " + fileName, title, JOptionPane.INFORMATION_MESSAGE);
                                            } else {
                                                break;   // if user chooses to exit, we break from infinite loop
                                            }
                                        }
                                    } else {
                                        break;                                          // If user chooses to exit, we break the infinite loop, and user is able to choose statistics or account settings
                                    }
                                }
                                saveMessages(currUser);                          // after everything is finished, we save the current messages field of the user to the messages.csv using this method
                            } else if (currUser instanceof Buyer) {
                                /*
                                if you are buyer, when trying to enter the Messaging part of the program, you will be presented with 3 options
                                1) Either you write to the store that interests you
                                When you write message to the message, your message will be sent to the owner of the store, and you will be redirected to the conversation with Seller
                                2) Or you write to a specific seller, just like you do message with a normal person
                                0) Exits to the menu
                                */
                                options = new String[]{"Write to Store", "Write to Seller", "Exit"};
                                int makeChoice =
                                        JOptionPane.showOptionDialog(null, "Select an option to proceed", title,
                                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                options, options[2]);

                                pwServer.write(makeChoice);
                                pwServer.println();
                                pwServer.flush();

                                // user chooses the option
                                if (makeChoice == 0) {           // user chooses to text the store
                                    StringBuilder listStores = new StringBuilder(String.format("List of Stores: " +
                                            "%n----------------------------------------%n"));
                                    String[] storeArr = new String[stores.size()];
                                    ArrayList<Store> allStoresFromServer = (ArrayList<Store>) ois.readObject();
                                    for (int i = 0; i < stores.size(); i++) {
                                        storeArr[i] = allStoresFromServer.get(i).getStoreName();
                                        listStores.append(stores.get(i).getStoreName());
                                    }
                                    String store;
                                    while (true) {
                                        store =
                                                (String) JOptionPane.showInputDialog(null, "Select store to message", title,
                                                        JOptionPane.PLAIN_MESSAGE,null, storeArr, storeArr[0]);
                                        System.out.println(store);
                                        if (store == null) {
                                            JOptionPane.showMessageDialog(null, "You should select one store!", title,
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                        else {
                                            pwServer.write(store);
                                            pwServer.println();
                                            pwServer.flush();
                                            break;
                                        }
                                    }


                                    // user enters the name of the store
                                    boolean flag = false;                 // responsible for showing if store exists
                                    flag = true;
                                    String msg;
                                    while (true) {
                                        msg = JOptionPane.showInputDialog(null, "Write your message: ",
                                                title, JOptionPane.PLAIN_MESSAGE);           // main user writes the message to the store
                                        if (msg == null) {
                                            JOptionPane.showMessageDialog(null, "Enter the message!",
                                                    title, JOptionPane.ERROR_MESSAGE);
                                        }
                                        else if (msg.equals("")) {
                                            JOptionPane.showMessageDialog(null, "Your message should " +
                                                            "contain at least one character",
                                                    title, JOptionPane.ERROR_MESSAGE);
                                        }
                                        else {
                                            pwServer.write(msg);
                                            pwServer.println();
                                            pwServer.flush();
                                            break;
                                        }
                                    }

                                    User sellerObject = (User) ois.readObject();

                                    ArrayList<Message> temp = currUser.getMessages();
                                    temp.add(new Message(currUser.getUsername(), sellerObject.getUsername(), msg));                  // writes
                                    // new message
                                    currUser.setMessages(temp);               // update the messages field of the user
                                    // tells user who is owner of the store
                                    JOptionPane.showMessageDialog(null, "Store manager's username" +
                                            " is " + sellerObject + "Please wait for his " +
                                            "message", title, JOptionPane.INFORMATION_MESSAGE);

                                    if (!flag) {              // flag is false when store doesn't exist
                                        JOptionPane.showMessageDialog(null, "That store doesn't exist!", title,
                                                JOptionPane.ERROR_MESSAGE);
                                    }
                                                                // updates the messages.csv file with the changes that have been made to the messages field of the user
                                } else if (makeChoice == 1) {                          // if user chooses to
                                    // write to the specific Seller directly
                                    while (true) {
                                        ArrayList<Message> messageHistory;
                                        String[] listOfUsers = (String[]) ois.readObject();      // List of users
                                        // with whom he had conversations before
                                        options = new String[listOfUsers.length + 2];
                                        options[0] = "Start new dialog";
                                        for (int i = 1; i < listOfUsers.length + 1; i++) {
                                            options[i] = listOfUsers[i - 1];
                                        }
                                        options[options.length - 1] = "Exit";
                                        int receiveUser;
                                        while (true) {
                                            receiveUser = JOptionPane.showOptionDialog(null,
                                                    "Select user to view messages or start a dialog with a new user",
                                                    title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                    options, options[options.length - 1]);
                                            if (receiveUser == -1) {
                                                JOptionPane.showMessageDialog(null, "You should write your choice!",
                                                        title, JOptionPane.ERROR_MESSAGE);
                                            }
                                            else {
                                                pwServer.write(receiveUser);
                                                pwServer.println();
                                                pwServer.flush();
                                                break;
                                            }
                                        }

                                        if (receiveUser == 0) {                                          // dialog with new user

                                            String[] listOfSellers = (String[]) ois.readObject();

                                            String newUser;
                                            while (true) {
                                                newUser = (String) JOptionPane.showInputDialog(null, "Select " +
                                                                "seller to message",
                                                        title, JOptionPane.QUESTION_MESSAGE, null, listOfSellers,
                                                        listOfSellers[0]);         // Enter name of the new user
                                                if (newUser == null) {
                                                    JOptionPane.showMessageDialog(null, "You need to select one " +
                                                            "seller to message", title, JOptionPane.ERROR_MESSAGE);
                                                }
                                                else {
                                                    pwServer.write(newUser);
                                                    pwServer.println();
                                                    pwServer.flush();
                                                    break;
                                                }
                                            }

                                            boolean alreadyMessaged = false;
                                            for (String u : listOfUsers) {
                                                if (u.equals(newUser)) {
                                                    alreadyMessaged = true;
                                                    JOptionPane.showMessageDialog(null, "You've already messaged " +
                                                            "this user!", title, JOptionPane.ERROR_MESSAGE, null);
                                                }
                                            }
                                            boolean flag = true;
                                            // same logic as it was in the line 123, read comments there
                                            boolean flag2 = true;

                                            User value = (User) ois.readObject();

                                            if (value instanceof Buyer) {
                                                JOptionPane.showMessageDialog(null, "You can't write to " +
                                                        "Seller, because you are Seller yourself", title, JOptionPane.ERROR_MESSAGE, null);
                                                flag = false;
                                            } else if (currUser.getBlockedUsers().contains(value) || value.getBlockedUsers().contains(currUser)) {
                                                JOptionPane.showMessageDialog(null, "You can't write to this " +
                                                        "user because they are blocked", title, JOptionPane.ERROR_MESSAGE, null);
                                                flag2 = false;
                                            }

                                            if (flag && flag2 && !alreadyMessaged) {
                                                while (true) {
                                                    String mes = JOptionPane.showInputDialog(null, "Write your hello message first!",
                                                            title, JOptionPane.PLAIN_MESSAGE);               // user enters the message he would want to send to new user
                                                    if (mes == null) {
                                                        JOptionPane.showMessageDialog(null, "You should enter your " +
                                                                "message", title, JOptionPane.ERROR_MESSAGE);
                                                    }
                                                    else {
                                                        pwServer.write(mes);
                                                        pwServer.println();
                                                        pwServer.flush();
                                                    }
                                                }
                                            }
                                        } else if (receiveUser >= 1 && receiveUser != options.length - 1) {
                                            // if user chooses to continue conversation with the user he had conversation before
                                            while (true) {
                                                messageHistory = (ArrayList<Message>) ois.readObject();

                                                messageHist = new StringBuilder(String.format("Message " +
                                                                "History: %s - " +
                                                                "%s%n----------------------------------------%n",
                                                        currUser.getUsername(), listOfUsers[receiveUser - 1]));
                                                for (Message message : messageHistory) {                            // prints out every message
                                                    if (message.getMessage().contains("\\n")) {
                                                        String ansMes = message.getMessage().replaceAll("\\\\n", "\n");
                                                        String ans = String.format("%s   (%s -> %s)%n%s%n", message.getTime(), message.getSender(), message.getReceiver(), ansMes);
                                                        messageHist.append(ans);
                                                    } else
                                                        messageHist.append(message);
                                                }
                                                /*
                                                 read comments on the line 166, identical features
                                                */
                                                options = new String[]{"Write Message", "Edit Message", "Delete " +
                                                        "Message", "Export History to CSV", "Exit"};

                                                int optionChoice;
                                                while (true) {
                                                    optionChoice = JOptionPane.showOptionDialog(null,
                                                            messageHist.toString() + "\nSelect an option to proceed",
                                                            title, JOptionPane.YES_NO_OPTION,
                                                            JOptionPane.QUESTION_MESSAGE, null, options, options[4]);

                                                    if (optionChoice == -1) {
                                                        JOptionPane.showMessageDialog(null, "Please select the " +
                                                                "correct choice", title, JOptionPane.ERROR_MESSAGE);
                                                    }
                                                    else {
                                                        pwServer.write(optionChoice);
                                                        pwServer.println();
                                                        pwServer.flush();
                                                        break;
                                                    }
                                                }



                                                if (optionChoice == 0) {                 //if user chooses to write a new message
                                                    options = new String[]{"Send Message", "Upload File"};
                                                    int fileOrText = JOptionPane.showOptionDialog(null, "Do you want " +
                                                                    "to send a message or upload a text file?", title,
                                                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                                            null, options, options[0]);
                                                    if (fileOrText == 0) {              // if user sends regular message
                                                        while (true) {
                                                            String mes = JOptionPane.showInputDialog(null, "Write your message: ",
                                                                    title, JOptionPane.PLAIN_MESSAGE);
                                                            if (mes == null) {
                                                                JOptionPane.showMessageDialog(null, "Please write a " +
                                                                                "message", title, JOptionPane.ERROR_MESSAGE);
                                                            }
                                                            else if (mes.equals("")) {
                                                                JOptionPane.showMessageDialog(null, "Message cannot " +
                                                                                "be an emply string", title, JOptionPane.ERROR_MESSAGE);
                                                            }
                                                            else {
                                                                pwServer.write(mes);
                                                                pwServer.println();
                                                                pwServer.flush();
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    else if (fileOrText == 1) {            // if user sends txt file
                                                        // as a message
                                                        String fileName;
                                                        while (true) {
                                                            fileName = JOptionPane.showInputDialog(null, "Enter " +
                                                                    "name of txt file: ", title, JOptionPane.PLAIN_MESSAGE);    // enters name of the file
                                                            if (fileName == null) {
                                                                JOptionPane.showMessageDialog(null, "Please write a " +
                                                                        "filename", title, JOptionPane.ERROR_MESSAGE);
                                                            }
                                                            else if (fileName.equals("")) {
                                                                JOptionPane.showMessageDialog(null, "Message cannot " +
                                                                        "be an emply string", title, JOptionPane.ERROR_MESSAGE);
                                                            }
                                                            else {
                                                                pwServer.write(fileName);
                                                                pwServer.println();
                                                                pwServer.flush();
                                                                break;
                                                            }
                                                            String reportFromServer = bfrServer.readLine();
                                                            if (reportFromServer.equals("Success")) {
                                                                JOptionPane.showMessageDialog(null, "Your message was" +
                                                                        " successfully writen from the given file",
                                                                        title, JOptionPane.INFORMATION_MESSAGE);
                                                            }
                                                            else {
                                                                JOptionPane.showMessageDialog(null, "File provided " +
                                                                                "from you does not EXIST",
                                                                        title, JOptionPane.ERROR_MESSAGE);
                                                            }
                                                        }
                                                    }
                                                } else if (optionChoice == 1) {              //
                                                    // if user chooses to edit messages (for more detailed comments refer to line 210)
                                                    messageHistory = parseMessageHistory(currUser,
                                                            listOfUsers[receiveUser - 1]);
                                                    ArrayList<Message> userIsSender = new ArrayList<>();
                                                    int i = 0;
                                                    int z = 0;
                                                    messageHist = new StringBuilder(String.format("Message " +
                                                                    "History: %s - " +
                                                                    "%s%n----------------------------------------%n",
                                                            currUser.getUsername(), listOfUsers[receiveUser - 1]));
                                                    while (z < messageHistory.size()) {
                                                        if (messageHistory.get(z).getSender().equals(currUser.getUsername())) {
                                                            userIsSender.add(messageHistory.get(z));
                                                            messageHist.append(String.format("[%d] " + messageHistory.get(i).toString(), i + 1));
                                                            i++;
                                                        }
                                                        z++;
                                                    }
                                                    if (i > 0) {
                                                        Integer[] messageNums = new Integer[i];
                                                        for (int j = 0; j < i; j++) {
                                                            messageNums[j] = j + 1;
                                                        }
                                                        choice = (int) JOptionPane.showInputDialog(null,
                                                                messageHist.toString() + "\nSelect message to edit",
                                                                title, JOptionPane.QUESTION_MESSAGE, null, messageNums,
                                                                messageNums[0]); //user chooses which message available for him to edit he wants to edit
                                                        String msg = JOptionPane.showInputDialog(null,"Enter new " +
                                                                "message: ", title, JOptionPane.PLAIN_MESSAGE);
                                                        // user enters the message to which user wants to change his message
                                                        Message temp = userIsSender.get(choice - 1);       // we grab value form the userIsSender which stores only messages where main user is sender
                                                        for (Message message : messageHistory) {
                                                            if (message.getId() == temp.getId()) {
                                                                message.setMessage(msg);                   // when we find that message in the main message history, we change its text
                                                            }
                                                        }
                                                        saveMessages(currUser);
                                                    } else {
                                                        JOptionPane.showMessageDialog(null, "No messages to edit with" +
                                                                " this user", title, JOptionPane.ERROR_MESSAGE);
                                                    }
                                                }else if (optionChoice == 2) {                 // if user chooses
                                                    // to delete the message (more detailed comments on the line 258)
                                                    messageHistory = parseMessageHistory(currUser, listOfUsers[receiveUser - 1]);
                                                    ArrayList<Message> userIsSender = new ArrayList<>();
                                                    int i = 0;
                                                    messageHist = new StringBuilder(String.format("Message " +
                                                                    "History: %s - " +
                                                                    "%s%n----------------------------------------%n",
                                                            currUser.getUsername(), listOfUsers[receiveUser - 1]));
                                                    while (i < messageHistory.size()) {
                                                        userIsSender.add(messageHistory.get(i));              // adding every message into the userIsSender arraylist
                                                        // printing every message with a number next to it
                                                        messageHist.append(String.format("[%d] " + messageHistory.get(i).toString(), i + 1));
                                                        i++;
                                                    }
                                                    if (i > 0) {
                                                        Integer[] messageNums = new Integer[i];
                                                        for (int j = 0; j < i; j++) {
                                                            messageNums[j] = j + 1;
                                                        }
                                                        choice = (int) JOptionPane.showInputDialog(null,
                                                                messageHist.toString() + "\nSelect message to delete",
                                                                title, JOptionPane.QUESTION_MESSAGE, null, messageNums,
                                                                messageNums[0]);  // user chooses which message to delete
                                                        Message temp = userIsSender.get(choice - 1);        // we assign the message user chose to Message temp variable
                                                        ArrayList<Message> allUserMessages = currUser.getMessages();
                                                        for (int j = 0; j < allUserMessages.size(); j++) {
                                                            if (allUserMessages.get(j).getId() == temp.getId()) {         // finding temp message in the main allUserMessages ArrayList
                                                                if (temp.getSender().equals(currUser.getUsername()))          // if main user was sender
                                                                    allUserMessages.get(j).setDelBySender(true);          // then message becomes invisible to the sender
                                                                else                                                      // if main user was receiver
                                                                    allUserMessages.get(j).setDelByReceiver(true);        // then message becomes invisible to the receiver
                                                                currUser.setMessages(allUserMessages);                        // updates the messages field of the user after deleting the message
                                                                break;
                                                            }
                                                        }
                                                        currUser.refreshMessages();            // refreshMessages is used to remove some messages in the messages field of the user, because we need to be
                                                        // manually remove some messages in the messages field. setMessages isn't enough, because it doesn't actually remove messages
                                                        // it only updates its values
                                                    } else {
                                                        JOptionPane.showMessageDialog(null, "No messages to delete",
                                                                title, JOptionPane.ERROR_MESSAGE);
                                                    }
                                                } else if (optionChoice == 3) {            // if he chooses to export messages to the csv file
                                                    String fileName = JOptionPane.showInputDialog(null, "Enter " +
                                                            "name of the file to which you want to export your " +
                                                            "message history", title, JOptionPane.QUESTION_MESSAGE); // enters the file name
                                                    PrintWriter pw = new PrintWriter(new FileOutputStream(fileName,false));
                                                    for (Message msg : messageHistory) {
                                                        // this line writes Message object in the same manner as it does in main "messages.csv" file
                                                        String ans = String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", msg.getId(), msg.getTime(), msg.getSender(), msg.getReceiver(), msg.getMessage(), msg.isDelBySender(), msg.isDelByReceiver());
                                                        pw.write(ans);
                                                        pw.println();
                                                        pw.flush();
                                                    }
                                                    // confirmation that history was saved
                                                    JOptionPane.showMessageDialog(null, "Your message history was successfully " +
                                                            "saved to " + fileName, title, JOptionPane.INFORMATION_MESSAGE);
                                                } else {
                                                    break;                                          // If user chooses to exit, we break the infinite loop, and user is able to choose statistics or account settings
                                                }
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                    saveMessages(currUser);                        // saves changed to the messages.csv after finishing the messages part of the program
                                } else  {
                                    break;          // breaks out of infinite loop if user chooses to exit
                                }
                            }
                            break;
                        case 1:
                            // this is Statistics part of the code
                            while (true) {
                                title = String.format("%s - Statistics%n", currUser.getUsername());
                                /*
                                User is presented with 4 options
                                */
                                options = new String[]{"Alphabetical", "Reverse Alphabetical", "Most Common " +
                                        "Words", "Exit"};
                                // Assigns alphabetical to user choice as an int.
                                int alphabetical = JOptionPane.showOptionDialog(null, "Select option to show statistics",
                                                title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                                null, options, options[3]);
                                pwServer.write(alphabetical);
                                pwServer.flush();
                                if (currUser instanceof Buyer) {
                                    if (alphabetical == 0) {
                                        String stats = bfrServer.readLine().replaceAll("\\\\n","\n");
                                        System.out.println(stats);
                                        // shows user statistics in alphabetical order
                                        JOptionPane.showMessageDialog(null, stats, title, JOptionPane.INFORMATION_MESSAGE);
                                    }
                                    else if (alphabetical == 1) {
                                        String stats = bfrServer.readLine().replaceAll("\\\\n","\n");
                                        // shows user statistics in reverse alphabetical order
                                        JOptionPane.showMessageDialog(null, stats, title, JOptionPane.INFORMATION_MESSAGE);
                                    }
                                    else if (alphabetical == 2) {
                                        ArrayList<Message> allMessages = new ArrayList<>();
                                        String word = "";
                                        String secondWord = "";
                                        String thirdWord = "";
                                        int count;
                                        int maxCount = 0;
                                        int secondCount = 0;
                                        int thirdCount = 0;
                                        for (User u1 : users) {
                                            if (u1 != currUser) {
                                                allMessages.addAll(parseMessageHistory(currUser, u1.getUsername())); // adds all messages to an arrayList
                                            }
                                        }
                                        String message = "";
                                        for (Message m : allMessages) {
                                            message += m.getMessage() + " "; // creates a string with all every word in all messages
                                        }
                                        String[] wordArr = message.split(" "); // creates a string array for every word
                                        // Gets the most commonly used word and the number of times it is used
                                        for (int k = 0; k < wordArr.length; k++) {
                                            count = 1;
                                            for (int l = k + 1; l < wordArr.length; l++) {
                                                if (wordArr[k].equals(wordArr[l])) {
                                                    count++;
                                                }

                                            }
                                            if (count > maxCount) {
                                                maxCount = count;
                                                word = wordArr[k];
                                            }
                                        }
                                        // Gets the second most commonly used word and the number of times it is used
                                        String[] newWordArr = new String[wordArr.length - maxCount];
                                        int i = 0;
                                        for (String s : wordArr) {
                                            if (!s.equals(word)) {
                                                newWordArr[i] = s;
                                                i++;
                                            }
                                        }
                                        for (int k = 0; k < newWordArr.length; k++) {
                                            count = 1;
                                            for (int l = k + 1; l < newWordArr.length; l++) {
                                                if (newWordArr[k].equals(newWordArr[l])) {
                                                    count++;
                                                }

                                            }
                                            if (count > secondCount) {
                                                secondWord = newWordArr[k];
                                                secondCount = count;
                                            }
                                        }
                                        // Gets the third most commonly used word and the number of times it is used
                                        String[] new2WordArr = new String[newWordArr.length - secondCount];
                                        i = 0;
                                        for (String s : newWordArr) {
                                            if (!s.equals(secondWord)) {
                                                new2WordArr[i] = s;
                                                i++;
                                            }
                                        }
                                        for (int k = 0; k < new2WordArr.length; k++) {
                                            count = 1;
                                            for (int l = k + 1; l < new2WordArr.length; l++) {
                                                if (new2WordArr[k].equals(new2WordArr[l])) {
                                                    count++;
                                                }

                                            }
                                            if (count > thirdCount) {
                                                thirdCount = count;
                                                thirdWord = new2WordArr[k];
                                            }
                                        }
                                        // Prints the first, second, and third most commonly used words
                                        String commonWords = "The most common word in Messages is " + word + " " +
                                                "said " + maxCount + " times\n" + "The second most common word in " +
                                                "Messages is " + secondWord + " said " + secondCount + " times\n" +
                                                "The third most common word in Messages is " + thirdWord + " said "
                                                + thirdCount + " times";
                                        JOptionPane.showMessageDialog(null, commonWords, title, JOptionPane.INFORMATION_MESSAGE);
                                    } else {
                                        break;
                                    }
                                } else if (currUser instanceof Seller) {
                                    if (alphabetical == 0) {
                                        String sortMessages = bfrServer.readLine().replaceAll("\\\\n", "\n");
                                        JOptionPane.showMessageDialog(null, sortMessages, title,
                                                JOptionPane.INFORMATION_MESSAGE);
                                    } else if (alphabetical == 1) {
                                        String sortMessages = bfrServer.readLine().replaceAll("\\\\n", "\n");
                                        JOptionPane.showMessageDialog(null, sortMessages.toString(), title,
                                                JOptionPane.INFORMATION_MESSAGE);
                                    } else if (alphabetical == 2) {
                                        String commonWords = bfrServer.readLine().replaceAll("\\\\n", "\n");
                                        JOptionPane.showMessageDialog(null, commonWords, title, JOptionPane.INFORMATION_MESSAGE);
                                    } else {
                                        break;
                                    }
                                }
                            }
                            break;
                        case 2:
                            // options for the user's account
                            boolean exit = false;
                            while (true) {
                                assert currUser != null;
                                title = String.format("%s - Account Details%n", currUser.getUsername());
                                String userInfo = String.format("Email: %s%nPassword: %s%n", currUser.getEmail(),
                                        currUser.getPassword());
                                if (currUser instanceof Seller) {
                                    // shows 4 options if user is a seller
                                    options = new String[]{"Edit Account", "Delete Account", "Block/Unblock User", "Create New Store", "Exit"};

                                } else {
                                    // shows 3 options if user is a buyer
                                    options = new String[]{"Edit Account", "Delete Account", "Block/Unblock User", "Exit"};
                                }
                                choice = JOptionPane.showOptionDialog(null, userInfo + "Please choose an option " +
                                                "to proceed", title, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                                        null, options, options[0]);
                                pwServer.write(choice);
                                pwServer.flush();
                                if (choice == 0) {
                                    // user selects edit account
                                    options = new String[]{"Change Email", "Change Password", "Exit"};
                                    choice = JOptionPane.showOptionDialog(null, "Choose an option to proceed"
                                            , title, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                                            null, options, options[2]);
                                    String newAccountInfo;
                                    pwServer.write(choice);
                                    switch (choice) {
                                        case 0:
                                            // user selects change email
                                            do {
                                                // user types new email
                                                newAccountInfo = JOptionPane.showInputDialog(null,
                                                        "Enter new email: ", title, JOptionPane.PLAIN_MESSAGE);
                                                pwServer.write(newAccountInfo);
                                                pwServer.println();
                                                pwServer.flush();

                                                newAccountInfo = bfrServer.readLine();
                                                System.out.println(newAccountInfo);
                                                if (newAccountInfo.isEmpty()) {
                                                    JOptionPane.showMessageDialog(null, "That email was not valid\n " +
                                                                    "(Email didn't contain an @ and . or email was " +
                                                                    "already in use", "title", JOptionPane.WARNING_MESSAGE);
                                                }
                                            } while (newAccountInfo.isEmpty());
                                            // shows that the user's email was changed
                                            JOptionPane.showMessageDialog(null, String.format("Email " +
                                                    "changed to: %s%n", newAccountInfo), title, JOptionPane.INFORMATION_MESSAGE);
                                            break;
                                        case 1:
                                            // user selects change password
                                            // user types new password
                                            newAccountInfo = JOptionPane.showInputDialog(null,
                                                    "Enter new password: ", title, JOptionPane.PLAIN_MESSAGE);
                                            pwServer.write(newAccountInfo);
                                            pwServer.println();
                                            pwServer.flush();
                                            if (newAccountInfo != null) {
                                                // changes user's password to new password
                                                currUser.setPassword(newAccountInfo);
                                                // shows that the user's password was changed
                                                JOptionPane.showMessageDialog(null, String.format("Password " +
                                                        "changed to: %s%n", newAccountInfo), title, JOptionPane.INFORMATION_MESSAGE);
                                            } else {
                                                break;
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                } else if (choice == 1) {
                                    // user selects delete account
                                    options = new String[]{"Yes", "No"};
                                    // makes sure user wants to delete their account
                                    choice = JOptionPane.showOptionDialog(null, "Are you sure you want to " +
                                                    "delete this user?", title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                            null, options, options[0]);
                                    pwServer.write(choice);
                                    pwServer.flush();
                                    if (choice == 0) {
                                        // user selects Y, their account is deleted (Their messages to other users will remain)
                                        JOptionPane.showMessageDialog(null, String.format("User [%s] " +
                                                        "successfully deleted%n", currUser.getUsername()), title,
                                                JOptionPane.INFORMATION_MESSAGE);
                                        currUser.removeUser();
                                        choice = 3;
                                        currUser = null;
                                    }
                                } else if (choice == 2) {
                                    // user selects block/unblock users
                                    String blockedUsers = bfrServer.readLine();
                                    // Asks if user wants to block or unblock a user
                                    options = new String[]{"Block New User", "Unblock User", "Exit"};
                                    choice = JOptionPane.showOptionDialog(null, blockedUsers, title,
                                            JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
                                            options[2]);
                                    pwServer.write(choice);
                                    pwServer.println();
                                    pwServer.flush();
                                    switch (choice) {
                                        case 0:
                                            // user selects block user
                                            String blockUsername = (String) JOptionPane.showInputDialog(null, "Enter name" +
                                                    " of user to block: ", title, JOptionPane.PLAIN_MESSAGE, null,
                                                    userArr, userArr[0]);
                                            pwServer.write(blockUsername);
                                            pwServer.println();
                                            pwServer.flush();
                                            // user enters username of user to block
                                            if (blockUsername == null) {
                                                break;
                                            }
                                            int action = bfrServer.read();
                                            if (action == 0) {
                                                // if that user exist they are blocked
                                                JOptionPane.showMessageDialog(null, blockUsername + " blocked",
                                                        title, JOptionPane.INFORMATION_MESSAGE);
                                            } else {
                                                // if they are already blocked tell user
                                                JOptionPane.showMessageDialog(null, "That user is already blocked",
                                                        title, JOptionPane.WARNING_MESSAGE);
                                            }
                                            break;
                                        case 1:
                                            // user select unblock user
                                            String[] blockedUsersArr = null;
                                            try {
                                                blockedUsersArr = (String[]) ois.readObject();
                                            } catch (ClassNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                            if (blockedUsersArr.length > 0) {
                                                String unblockUsername = (String) JOptionPane.showInputDialog(null, "Enter " +
                                                                "name of user to block: ", title, JOptionPane.PLAIN_MESSAGE, null,
                                                        blockedUsersArr, blockedUsersArr[0]);
                                                pwServer.write(unblockUsername);
                                                pwServer.println();
                                                pwServer.flush();
                                                // user enters username of user to unblock
                                                if (unblockUsername == null) {
                                                    break;
                                                }
                                                action = bfrServer.read();
                                                if (action == 0) {
                                                    // if that user is currently blocked they are unblocked
                                                    JOptionPane.showMessageDialog(null, unblockUsername + " unblocked",
                                                            title, JOptionPane.INFORMATION_MESSAGE);
                                                } else {
                                                    // if they aren't blocked tell user
                                                    JOptionPane.showMessageDialog(null, "That user is not blocked",
                                                            title, JOptionPane.INFORMATION_MESSAGE);
                                                }
                                            } else {
                                                JOptionPane.showMessageDialog(null, "This user has no other users " +
                                                        "blocked", title, JOptionPane.WARNING_MESSAGE);
                                            }
                                            break;
                                        default:
                                            exit = true;
                                            break;
                                    }
                                } else if (choice == 3 && currUser instanceof Seller) {
                                        // if user is seller allow user to create store
                                        String userStores = bfrServer.readLine();
                                        String storeName = JOptionPane.showInputDialog(null,
                                                userStores, title, JOptionPane.QUESTION_MESSAGE);
                                        pwServer.write(storeName);
                                        pwServer.println();
                                        pwServer.flush();
                                        ((Seller) currUser).createStore(storeName); // adds new store
                                        break;
                                } else {
                                    break;
                                }
                            }
                            break;
                        default:
                            loggedIn = false;
                            break;
                    }
                }
            }
            if (currUser != null) {
                // user logs out
                JOptionPane.showMessageDialog(null, "Successfully Logged out", "Marketplace Messaging", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // user leaves program
                JOptionPane.showMessageDialog(null, "Thank you for using the messaging service", "Marketplace Messaging", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }


    /*
    This method is responsible for parse through all Users, and selecting only those with whom our User had conversations with
    If user doesn't have conversation initiated with certain users, it won't add their username to the returning array
    */
    public static String[] parseUsers(User user) {
        ArrayList<Message> messages = user.getMessages();
        ArrayList<String> temp = new ArrayList<>();
        for (Message message : messages) {
            if (message.getSender().equals(user.getUsername())) {
                if (!temp.contains(message.getReceiver()))
                    temp.add(message.getReceiver());
            } else {
                if (!temp.contains(message.getSender()))
                    temp.add(message.getSender());
            }
        }
        String[] answer = new String[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            answer[i] = temp.get(i);
        }
        return answer;
    }

    //this method parses mainClient messages field, and selects only messages that has thirdParty's username in it.
    //this method allows us to view private message history with specific User, whose username is passed in as "thirdParty"
    public static ArrayList<Message> parseMessageHistory(User mainClient, String thirdParty) {
        ArrayList<Message> messages = mainClient.getMessages();
        ArrayList<Message> temp = new ArrayList<>();
        for (Message message : messages) {
            if (message.getSender().equals(thirdParty) || message.getReceiver().equals(thirdParty)) {
                temp.add(message);
            }
        }
        return temp;
    }


    public static void writeUsers(String filename, ArrayList<User> users) {
        File f = new File(filename);
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(f, false))) {
            for (User u : users) {
                pw.print("\"" + u.getUsername() + "\",\"" + u.getEmail() + "\",\"" + u.getPassword());
                if (u instanceof Buyer) {
                    pw.print("\",\"b\",\"");
                } else {
                    pw.print("\",\"s\",\"");
                }
                if (u.getBlockedUsers().size() > 0) {
                    ArrayList<User> blockedUsers = u.getBlockedUsers();
                    ArrayList<String> blockedUsernames = new ArrayList<>();
                    for (User bUser : blockedUsers) {
                        blockedUsernames.add(bUser.getUsername());
                    }
                    for (int i = 0; i < blockedUsernames.size(); i++) {
                        if (i != blockedUsers.size() - 1) {
                            pw.print(blockedUsernames.get(i) + ",");
                        } else {
                            pw.print(blockedUsernames.get(i) + "\"");
                        }
                    }
                } else {
                    if (u instanceof Seller) {
                        if (((Seller) u).getStores().size() > 0) {
                            pw.print("\"");
                        } else {
                            pw.print("\",");
                        }
                    } else {
                        pw.print("\"");
                    }
                }
                if (u instanceof Seller) {
                    if (((Seller) u).getStores().size() > 1) {
                        for (int i = 0; i < ((Seller) u).getStores().size(); i++) {
                            if (i != ((Seller) u).getStores().size() - 1) {
                                pw.print(",\"" + ((Seller) u).getStores().get(i) + ",");
                            } else {
                                pw.print(((Seller) u).getStores().get(i) + "\"");
                            }
                        }
                    } else if (((Seller) u).getStores().size() == 1) {
                        pw.print(",\"" + ((Seller) u).getStores().get(0) + "\"");
                    } else {
                        pw.print("\"\"");
                    }
                }
                pw.println();
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "That file could not be found", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* this is very useful method that is used to read singular lines in the files
    it's functionality is fairly basic:
    For example we receive an line ---          "abc","34234","I like apples, bananas, watermelon"
    As an output it will give us an ArrayList abc(for example) where
    abc.get(0)     ->   abc
    abc.get(1)     ->   34234
    abc.get(2)     ->   I like apples, bananas, watermelon
    We couldn't use regular split() function, so we created this one to help us solve the problem
    */
    private static ArrayList<String> customSplitSpecific(String s)
    {
        ArrayList<String> words = new ArrayList<>();
        boolean notInsideComma = true;
        int start =0;
        for(int i=0; i<s.length()-1; i++)
        {
            if(s.charAt(i) == ',' && notInsideComma)
            {
                words.add(s.substring(start + 1,i - 1));
                start = i+1;
            }
            else if(s.charAt(i)=='"')
                notInsideComma=!notInsideComma;
        }
        words.add(s.substring(start + 1, s.length() - 1));
        return words;
    }

    /*
    This method is used to conveniently call blockUser method from the User.java
    * */
    public static void addBlockedUsers(ArrayList<User> users) {
        for (User u : users) {
            ArrayList<String> blockedUsernames = u.getBlockedUsernames();
            for (String bUser : blockedUsernames) {
                u.blockUser(bUser, users);
            }
        }
    }

    // This method is used to save changed made to the user to the "messages.csv" file
    public static void saveMessages(User user) throws IOException {
        ArrayList<Message> allMessages = user.getMessages();
        ArrayList<String> temp = new ArrayList<>();
        BufferedReader bfr = new BufferedReader(new FileReader("messages.csv"));
        String st;
        while ((st = bfr.readLine())!=null) {
            ArrayList<String> mesInfo = user.customSplitSpecific(st);
            if (!(mesInfo.get(2).equals("\"" + user.getUsername() + "\"") || mesInfo.get(3).equals("\"" + user.getUsername()+ "\"")))
                temp.add(st);
        }

        PrintWriter pw = new PrintWriter(new FileOutputStream("messages.csv",false));
        for (String s : temp) {                // here we write all messages that are not related to our user first
            pw.write(s);
            pw.println();
            pw.flush();
        }
        // only after we write those that might have been changed
        for (Message msg : allMessages) {            // here you can the format in which we write those messages to the messages.csv file
            String ans = String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", msg.getId(), msg.getTime(), msg.getSender(), msg.getReceiver(), msg.getMessage(), msg.isDelBySender(), msg.isDelByReceiver());
            pw.write(ans);
            pw.println();
            pw.flush();
        }
    }
public static User login(BufferedReader bfr, PrintWriter pw) {
    // Initialize variables
        ArrayList<String[]> users = new ArrayList<>();
        ArrayList<String> tempArrayList = new ArrayList<>();
        String[] tempArray;
        ArrayList<String> transferList;
        boolean invEmail;
        String email, pass;
        String title = "Login";

        //Add users from file to arraylist
        /*try {
            //BufferedReader bfr = new BufferedReader(new FileReader("login.csv"));
            String line = bfr.readLine();
            while (line != null) {
                tempArrayList.add(line);
                line = bfr.readLine();
            }
            for (String s : tempArrayList) {
                transferList = customSplitSpecific(s);
                tempArray = new String[transferList.size()];
                for (int j = 0; j < tempArray.length; j++) {
                    tempArray[j] = transferList.get(j);
                }
                users.add(tempArray);
            }
            bfr.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "There was an Error", "Error", JOptionPane.ERROR_MESSAGE);
        } */
    //Loops forever until a valid email and password are entered, or the escape sequence is ran
        while (true) {
            email = JOptionPane.showInputDialog(null, "Please enter your email:", title, JOptionPane.PLAIN_MESSAGE);
            pw.write(email);
            pw.println();
            pw.flush();
            pass = JOptionPane.showInputDialog(null, "Please enter your password:", title,
                    JOptionPane.PLAIN_MESSAGE);
            pw.write(pass);
            pw.println();
            pw.flush();

            invEmail = true;
            for (String[] user : users) {
                if (email.equals(user[1])) {
                    invEmail = false;
                    if (pass.equals(user[2])) {
                        if (user[3].equals("b"))
                            return new Buyer(user[0], email, pass);
                        if (user[3].equals("s"))
                            return new Seller(user[0], email, pass);
                    }
                }
            }

            //if the email or password does not match an existing account it is printed
            if (invEmail) {
                JOptionPane.showMessageDialog(null, "Your email was incorrect", "Login", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Your password was incorrect", "Login",
                        JOptionPane.WARNING_MESSAGE);
            }
            int option;
            do {
                String[] options = new String[]{"Yes", "No"};
                option = JOptionPane.showOptionDialog(null, "Would you like to try again?", title,
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (option == 1) {
                    return null;
                }
            } while (option != 0);
        }
    }

    public static User createAccount(String file) {
        //Variables are initialized
        ArrayList<String[]> userFile = new ArrayList<>();
        User user = null;
        String email = "";
        String pass = "";
        String userType = "";
        String userName = "";
        ArrayList<String> tempArrayList = new ArrayList<>();
        String[] tempArray;
        ArrayList<String> transferList;
        boolean repeatUser = false;
        boolean repeatEmail = false;
        boolean invUsername = true;
        boolean invEmail = true;
        boolean invPass = true;
        boolean invBuyer = true;
        String title = "Create new Account";
        //The array userFile is filled with all the information of users from login.csv
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(file));
            String line = bfr.readLine();
            while (line != null) {
                tempArrayList.add(line);
                line = bfr.readLine();
            }
            for (String s : tempArrayList) {
                transferList = customSplitSpecific(s);
                tempArray = new String[transferList.size()];
                for (int j = 0; j < tempArray.length; j++) {
                    tempArray[j] = transferList.get(j);
                }
                userFile.add(tempArray);
            }
            bfr.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "There was an Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
        //Loops until a valid email is inputted, a valid email being an email that is
        //not previously used and does not have and @ sign or comma
        while(invEmail){
            email = JOptionPane.showInputDialog(null, "A valid email contains an @ sign and has no commas\n" +
                            "Please enter a valid email:", title, JOptionPane.PLAIN_MESSAGE);
            for (String[] strings : userFile) {
                if (email.equals(strings[1])) {
                    repeatEmail = true;
                    break;
                }
            }
            if (email.contains(",") || !email.contains("@")) {
                JOptionPane.showMessageDialog(null, "That email was not valid", title, JOptionPane.WARNING_MESSAGE);
            } else if (repeatEmail) {
                JOptionPane.showMessageDialog(null, "Someone else has that email please enter a different one", title,
                        JOptionPane.WARNING_MESSAGE);
                repeatEmail = false;
            } else {
                invEmail = false;
            }
        }
        //Loops until a valid username is inputted, a valid username being a username that is
        //not previously used and does not have a comma
        while(invUsername){
            userName = JOptionPane.showInputDialog(null, "A valid username contains no commas\nPlease enter a valid" +
                    " username: ", title, JOptionPane.PLAIN_MESSAGE);
            for (String[] strings : userFile) {
                if (userName.equals(strings[0])) {
                    repeatUser = true;
                    break;
                }
            }
            if (userName.contains(",") || userName.equals("")) {
                JOptionPane.showMessageDialog(null, "That user name was not valid", title, JOptionPane.WARNING_MESSAGE);
            } else if (repeatUser) {
                JOptionPane.showMessageDialog(null, "Someone else has that user name please enter a different one.", title, JOptionPane.WARNING_MESSAGE);
                repeatUser = false;
            } else {
                invUsername = false;
            }
        }
        //Loops until a password is inputted
        while(invPass){
            pass = JOptionPane.showInputDialog(null, "Please enter a password: ", title, JOptionPane.PLAIN_MESSAGE);
            if (pass == null || pass.equals("")) {
                JOptionPane.showMessageDialog(null, "That password was not valid", title, JOptionPane.WARNING_MESSAGE);
            } else {
                invPass = false;
            }
        }
        //Loops until buyer/seller is inputted
        while(invBuyer){
            String[] options = new String[]{"Buyer", "Seller"};
            int choice = JOptionPane.showOptionDialog(null, "Are you a buyer or seller?", title,
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (choice == 0) {
                userType = "b";
                user = new Buyer(userName, email, pass);
                invBuyer = false;
            }else if (choice == 1) {
                userType = "s";
                user = new Seller(userName, email, pass);
                invBuyer = false;
            }
        }
        //The new user is written into login.csv
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(file, false));
                for (String[] strings : userFile) {
                    pw.println("\"" + strings[0] + "\"" + "," + "\"" + strings[1] + "\"" + "," + "\"" + strings[2] + "\"" + "," + "\"" + strings[3] + "\"" + "," + "\"" + strings[4] + "\"");
            }
            pw.println("\"" + userName + "\"" + "," + "\"" + email + "\"" + "," + "\"" + pass + "\"" + "," + "\"" + userType + "\"" + ",\"\"");
            pw.close();
        } catch (IOException e ) {
            JOptionPane.showMessageDialog(null, "There was an Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
        //A new user is returned using the information inputted
        return user;
    }

    /*
        reads the login.csv file and assigns each line to a User object. User object will consist of username,
        email, password, a list of blocked user, if they are a buyer or seller, and sellers will have stores.
        Then assigns users to an arraylist.
     */
    public static ArrayList<User> readUsers(String filename) throws FileNotFoundException {
        File f = new File(filename);
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader bfr = null;
        ArrayList<User> users = new ArrayList<>();
        if (!f.exists()) {
            JOptionPane.showMessageDialog(null, "That file could not be found", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                bfr = new BufferedReader(new FileReader(f));
                String read = bfr.readLine();
                while (read != null) {
                    lines.add(read);
                    read = bfr.readLine();
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "There was an Error", "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    if (bfr != null) {
                        bfr.close();
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "There was an Error", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        }
        for (String line : lines) {
            if (!line.isEmpty()) {
                ArrayList<String> user = customSplitSpecific(line);
                String username = user.get(0);
                String email = user.get(1);
                String password = user.get(2);
                boolean isBuyer = user.get(3).equalsIgnoreCase("b");
                String blockedUsers = user.get(4);
                ArrayList<String> blockedUsernames = new ArrayList<>();
                do {

                    if (!blockedUsers.contains(",")) {
                        blockedUsernames.add(blockedUsers);
                        blockedUsers = "";
                    } else {
                        blockedUsernames.add(blockedUsers.substring(0, blockedUsers.indexOf(",")));
                        blockedUsers = blockedUsers.substring(blockedUsers.indexOf(",") + 1);
                    }
                } while (!blockedUsers.isEmpty());
                if (isBuyer) {
                    users.add(new Buyer(username, email, password, blockedUsernames));
                } else {
                    Seller seller = new Seller(username, email, password, blockedUsernames);
                    String strStores = user.get(5);
                    if (strStores != null && !strStores.isEmpty()) {
                        do {
                            if (!strStores.contains(",")) {
                                seller.createStore(strStores);
                                strStores = "";
                            } else {
                                seller.createStore(strStores.substring(0, strStores.indexOf(",")));
                                strStores = strStores.substring(strStores.indexOf(",") + 1);
                            }
                        } while (!strStores.isEmpty());
                    }
                    users.add(seller);
                }
            }
        }
        return users;
    }

    /*
        reads the stores.csv file and assigns each line to a Store object. Store object will consist of store name,
        number of times this store was messaged, and users that messaged this store. Then assigns stores to an Arraylist.
     */
    public static ArrayList<Store> readStores(String filename, ArrayList<User> users) throws FileNotFoundException {
        File f = new File(filename);
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader bfr = null;
        ArrayList<Store> stores = new ArrayList<>();
        if (!f.exists()) {
            throw new FileNotFoundException("File doesn't exist");
        } else {
            try {
                bfr = new BufferedReader(new FileReader(f));
                String read = bfr.readLine();
                while (read != null) {
                    lines.add(read);
                    read = bfr.readLine();
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "There was an Error", "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                try {
                    if (bfr != null) {
                        bfr.close();
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "There was an Error", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        }
        for (String line : lines) {
            if (!line.isEmpty()) {
                ArrayList<String> strStores = customSplitSpecific(line);
                String storeName = strStores.get(0);
                int messagesReceived = Integer.parseInt(strStores.get(1));
                String messagedUsers = strStores.get(2);
                ArrayList<String> messageUsers = new ArrayList<>();
                ArrayList<Buyer> buyers = new ArrayList<>();
                do {

                    if (!messagedUsers.contains(",")) {
                        messageUsers.add(messagedUsers);
                        messagedUsers = "";
                    } else {
                        messageUsers.add(messagedUsers.substring(0, messagedUsers.indexOf(",")));
                        messagedUsers = messagedUsers.substring(messagedUsers.indexOf(",") + 1);
                    }
                } while (!messagedUsers.isEmpty());
                for (String s : messageUsers) {
                    for (User u : users) {
                        if (u instanceof Buyer && u.getUsername().equals(s)) {
                            buyers.add((Buyer) u);
                        }
                    }
                }
                stores.add(new Store(storeName, messagesReceived, buyers));
            }
        }
        return stores;
    }

    // Writes stores to stores.csv. writes store names, number of times store was messaged. and users that messaged this store
    public static void writeStores(String filename, ArrayList<Store> stores) {
        File f = new File(filename);
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(f, false))) {
            for (Store s : stores) {
                pw.print("\"" + s.getStoreName() + "\",\"" + s.getMessagesReceived() + "\",\"");
                if (s.getUserMessaged().size() > 0) {
                    ArrayList<String> userMessaged = new ArrayList<>();
                    for (Buyer b : s.getUserMessaged()) {
                        userMessaged.add(b.getUsername());
                    }
                    for (int i = 0; i < userMessaged.size(); i++) {
                        if (i != userMessaged.size() - 1) {
                            pw.print(userMessaged.get(i) + ",");
                        } else {
                            pw.print(userMessaged.get(i) + "\"");
                        }
                    }
                } else {
                    pw.print("\"");
                }
                pw.println();
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "That file could not be found", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // returns an arraylist of messages that went to store or mainClient
    public static ArrayList<Message> parseStoreMessages(User mainClient, String thirdParty) {
        ArrayList<Message> messages = mainClient.getMessages();
        ArrayList<Message> temp = new ArrayList<>();
        for (Message message : messages) {
            if (message.getSender().equals(thirdParty)) {
                temp.add(message);
            }
        }
        return temp;
    }
}

