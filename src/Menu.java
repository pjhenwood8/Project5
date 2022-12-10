import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;

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
        boolean serverConnection = false;
        Socket socket = null;
        while (!serverConnection) {
            int port;
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
        String email = "";
        String pass = "";
        String[] buyers = null;
        String[] sellers = null;
        String[] userArr = null;
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
                String userName;
                boolean invBuyer = true;
                switch (choice) {
                    case 0:
                        // If user wants to log in into existing acc, if it's successful, infinite loop breaks
                        do {
                            email = JOptionPane.showInputDialog(null, "Please enter your email:", title,
                                    JOptionPane.PLAIN_MESSAGE);
                        } while (email == null);
                        pwServer.write(email);
                        pwServer.println();
                        pwServer.flush();
                        do {
                            pass = JOptionPane.showInputDialog(null, "Please enter your password:", title,
                                    JOptionPane.PLAIN_MESSAGE);
                        } while (pass == null);
                        pwServer.write(pass);
                        pwServer.println();
                        pwServer.flush();
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
                if (loggingIn) {
                    try {
                        currUser = (User) ois.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (currUser != null) {
                        loggingIn = false;
                        loggedIn = true;
                        JOptionPane.showMessageDialog(null, "Successfully logged in as " + currUser.getUsername(), "Marketplace " +
                                "Messaging System", JOptionPane.INFORMATION_MESSAGE);
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
            }

            // Confirmation message, when user is able to log in

            while (loggedIn) {
                if (currUser != null) {
                    buyers = (String[]) ois.readObject();
                    sellers = (String[]) ois.readObject();
                    userArr = (String[]) ois.readObject();
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
                                    String[] listOfUsers = (String[]) ois.readObject();

                                    options = new String[listOfUsers.length + 2];
                                    options[0] = "Start new dialog";
                                    if (listOfUsers.length + 1 - 1 > 0)
                                        System.arraycopy(listOfUsers, 0, options, 1, listOfUsers.length + 1 - 1);
                                    options[options.length - 1] = "Exit";
                                    int receiveUser = JOptionPane.showOptionDialog(null,
                                            "Select user to view messages or start a dialog with a new user",
                                            title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                            options, options[options.length - 1]);
                                    pwServer.write(receiveUser);
                                    pwServer.flush();
                                    if (receiveUser == 0) {
                                        // dialog with new user
                                        String newUser = (String) JOptionPane.showInputDialog(null, "Select buyer to message",
                                                title, JOptionPane.QUESTION_MESSAGE, null, buyers,
                                                buyers[0]);         // Enter name of the new user
                                        pwServer.write(newUser);
                                        pwServer.println();
                                        pwServer.flush();
                                        int canMessage = bfrServer.read();
                                        if (canMessage == 0) {
                                            JOptionPane.showMessageDialog(null, "You've already messaged this" +
                                                    " user!", title, JOptionPane.ERROR_MESSAGE, null);
                                        } else if (canMessage == 1) {
                                            JOptionPane.showMessageDialog(null, "You can't write to this " +
                                                    "user because they are blocked", title, JOptionPane.ERROR_MESSAGE, null);
                                        } else {
                                            // user exists, user is Buyer, you didn't block each other
                                            String mes = JOptionPane.showInputDialog(null, "Write your hello message first!",
                                                    title, JOptionPane.PLAIN_MESSAGE);               // user enters the message he would want to send to new user
                                            pwServer.write(mes);
                                            pwServer.println();
                                            pwServer.flush();
                                            ArrayList<Message> temp = currUser.getMessages();  // creates new ArrayList with user messages
                                            temp.add(new Message(currUser.getUsername(), newUser, mes));    // adds new message to that ArrayList
                                            currUser.setMessages(temp);                        // updates the messages field on the user
                                            messageHistory = (ArrayList<Message>) ois.readObject();     // after the
                                            // messages field was updated, we update the messageHistory and print that out
                                            messageHist = new StringBuilder(String.format("Message " +
                                                    "History: %s - %s%n", currUser.getUsername(), newUser));
                                            for (Message message : messageHistory) {
                                                messageHist.append(message.toString());
                                            }
                                        }
                                    } else if (receiveUser >= 1 && receiveUser != options.length - 1) {
                                        // if user doesn't choose to start new dialog or exit the program receiveUser
                                        // is to view conversations you had before with other users
                                        while (true) {
                                            messageHistory = (ArrayList<Message>) ois.readObject();        // update messageHistory to print that out
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
                                                    "Message", "Export History to CSV", "Exit (Updates Messages)"};
                                            int optionChoice = JOptionPane.showOptionDialog(null,
                                                    messageHist + "\nSelect an option to proceed",
                                                    title, JOptionPane.YES_NO_OPTION,
                                                    JOptionPane.QUESTION_MESSAGE, null, options, options[4]);
                                            pwServer.write(optionChoice);
                                            pwServer.flush();
                                            if (optionChoice == 0) {            // writing new messages
                                                int canMessage = bfrServer.read();
                                                // you are presented with two options as described before
                                                // 1 - regular message          2 - upload a txt file
                                                if (canMessage == 2) {
                                                    options = new String[]{"Send Message", "Upload File"};
                                                    int fileOrText = JOptionPane.showOptionDialog(null, "Do you want " +
                                                                    "to send a message or upload a text file?", title,
                                                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                                            null, options, options[0]);
                                                    pwServer.write(fileOrText);
                                                    pwServer.flush();
                                                    if (fileOrText == 0) {       // regular message
                                                        String mes = JOptionPane.showInputDialog(null, "Write your message: ",
                                                                title, JOptionPane.PLAIN_MESSAGE);
                                                        pwServer.write(mes);
                                                        pwServer.println();
                                                        pwServer.flush();
                                                        ArrayList<Message> temp = currUser.getMessages();
                                                        temp.add(new Message(currUser.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                        currUser.setMessages(temp);        // updates the messages field of the user to the renewed messageHistory
                                                    } else if (fileOrText == 1) {      //uploading files
                                                        String fileName = JOptionPane.showInputDialog(null, "Enter " +
                                                                "name of txt file: ", title, JOptionPane.PLAIN_MESSAGE);    // enters name of the file
                                                        pwServer.write(fileName);
                                                        pwServer.println();
                                                        pwServer.flush();
                                                        // we read it as new line when writing all messages
                                                        int error = bfrServer.read();
                                                        if (error == 0) {
                                                            String mes = bfrServer.readLine();
                                                            ArrayList<Message> temp = currUser.getMessages();
                                                            temp.add(new Message(currUser.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                            currUser.setMessages(temp);                  // updates the messages field of the user
                                                        } else {
                                                            JOptionPane.showMessageDialog(null, "That file doesn't exist"
                                                                    , title, JOptionPane.ERROR_MESSAGE);
                                                        }
                                                    }
                                                } else {
                                                    JOptionPane.showMessageDialog(null, "You can't write to this " +
                                                            "user because they are blocked", title, JOptionPane.ERROR_MESSAGE, null);
                                                }
                                            } else if (optionChoice == 1) {          // editing messages
                                                messageHistory = (ArrayList<Message>) ois.readObject();
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
                                                    while (true) {
                                                        try {
                                                            choice = (int) JOptionPane.showInputDialog(null,
                                                                    messageHist + "\nSelect message to edit",
                                                                    title, JOptionPane.QUESTION_MESSAGE, null, messageNums,
                                                                    messageNums[0]); //user chooses which message available for him to edit he wants to edit
                                                            break;
                                                        } catch (NullPointerException npe) {
                                                            JOptionPane.showMessageDialog(null, "Please select a " +
                                                                    "message to edit", title, JOptionPane.WARNING_MESSAGE);
                                                        }
                                                    }
                                                    pwServer.write(choice);
                                                    pwServer.flush();
                                                    String msg = JOptionPane.showInputDialog(null,"Enter new " +
                                                            "message: ", title, JOptionPane.PLAIN_MESSAGE);
                                                    pwServer.write(msg);
                                                    pwServer.println();
                                                    pwServer.flush();
                                                    // user enters the message to which user wants to change his message
                                                    Message temp = userIsSender.get(choice - 1);       // we grab value form the userIsSender which stores only messages where main user is sender
                                                    for (Message message : messageHistory) {
                                                        if (message.getId() == temp.getId()) {
                                                            message.setMessage(msg);                   // when we find that message in the main message history, we change its text
                                                        }
                                                    }
                                                } else {
                                                    JOptionPane.showMessageDialog(null, "No messages to edit with" +
                                                                    " this user", title, JOptionPane.ERROR_MESSAGE);
                                                }
                                            } else if (optionChoice == 2) {             // deleting messages
                                                messageHistory = (ArrayList<Message>) ois.readObject();       // we save message history
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
                                                            messageHist + "\nSelect message to delete",
                                                            title, JOptionPane.QUESTION_MESSAGE, null, messageNums,
                                                            messageNums[0]);  // user chooses which message to delete
                                                    pwServer.write(choice);
                                                    pwServer.flush();
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
                                                pwServer.write(fileName);
                                                pwServer.println();
                                                pwServer.flush();
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
                                }// after everything is finished, we save the current messages field of the user to the messages.csv using this method
                            } else if (currUser instanceof Buyer) {
                                /*
                                if you are buyer, when trying to enter the Messaging part of the program, you will be presented with 3 options
                                1) Either you write to the store that interests you
                                When you write message to the message, your message will be sent to the owner of the store, and you will be redirected to the conversation with Seller
                                2) Or you write to a specific seller, just like you do message with a normal person
                                0) Exits to the menu
                                */
                                options = new String[]{"Write to Store", "Write to Seller", "Exit"};
                                int makeChoice;
                                while (true) {
                                    makeChoice =
                                            JOptionPane.showOptionDialog(null, "Select an option to proceed", title,
                                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                                    options, options[2]);
                                    if (makeChoice == -1) {
                                        JOptionPane.showMessageDialog(null, "Enter correct choice please", title, JOptionPane.ERROR_MESSAGE);
                                    }
                                    else {
                                        pwServer.write(makeChoice);
                                        pwServer.flush();
                                        break;
                                    }
                                }

                                // user chooses the option
                                if (makeChoice == 0) {           // user chooses to text the store
                                    ArrayList<Store> allStoresFromServer = (ArrayList<Store>) ois.readObject();
                                    String[] storeArr = new String[allStoresFromServer.size()];
                                    for (int i = 0; i < allStoresFromServer.size(); i++) {
                                        storeArr[i] = allStoresFromServer.get(i).getStoreName();
                                    }
                                    String store;
                                    while (true) {
                                        store =
                                                (String) JOptionPane.showInputDialog(null, "Select store to message", title,
                                                        JOptionPane.PLAIN_MESSAGE,null, storeArr, storeArr[0]);
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
                                    if (sellerObject != null) {
                                        // tells user who is owner of the store
                                        JOptionPane.showMessageDialog(null, "Store manager's username" +
                                                " is " + sellerObject.getUsername() + "\nPlease wait for his " +
                                                "message", title, JOptionPane.INFORMATION_MESSAGE);
                                    } else {
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

                                        oos.writeObject(options);
                                        oos.flush();

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
                                            int canMessage = bfrServer.read();
                                            if (canMessage == 0) {
                                                JOptionPane.showMessageDialog(null, "You've already messaged " +
                                                        "this user!", title, JOptionPane.ERROR_MESSAGE, null);
                                            } else if (canMessage == 1) {
                                                JOptionPane.showMessageDialog(null, "You can't write to this " +
                                                        "user because they are blocked", title, JOptionPane.ERROR_MESSAGE, null);
                                            } else {
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
                                                        break;
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
                                                        "Message", "Export History to CSV", "Exit (Updates Messages)"};

                                                int optionChoice;
                                                while (true) {
                                                    optionChoice = JOptionPane.showOptionDialog(null,
                                                            messageHist + "\nSelect an option to proceed",
                                                            title, JOptionPane.YES_NO_OPTION,
                                                            JOptionPane.QUESTION_MESSAGE, null, options, options[4]);

                                                    if (optionChoice == -1) {
                                                        JOptionPane.showMessageDialog(null, "Please select the " +
                                                                "correct choice", title, JOptionPane.ERROR_MESSAGE);
                                                    }
                                                    else {
                                                        pwServer.write(optionChoice);
                                                        pwServer.flush();
                                                        break;
                                                    }
                                                }



                                                if (optionChoice == 0) {                 //if user chooses to write a new message
                                                    int canMessage = bfrServer.read();
                                                    if (canMessage == 2) {
                                                        options = new String[]{"Send Message", "Upload File"};
                                                        int fileOrText;
                                                        while (true) {
                                                            fileOrText = JOptionPane.showOptionDialog(null, "Do you want " +
                                                                            "to send a message or upload a text file?", title,
                                                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                                                    null, options, options[0]);
                                                            if (fileOrText == -1) {
                                                                JOptionPane.showMessageDialog(null, "Enter correct choice");
                                                            } else {
                                                                pwServer.write(fileOrText);
                                                                pwServer.flush();
                                                                break;
                                                            }
                                                        }

                                                        if (fileOrText == 0) {              // if user sends regular message
                                                            while (true) {
                                                                String mes = JOptionPane.showInputDialog(null, "Write your message: ",
                                                                        title, JOptionPane.PLAIN_MESSAGE);
                                                                if (mes == null) {
                                                                    JOptionPane.showMessageDialog(null, "Please write a " +
                                                                            "message", title, JOptionPane.ERROR_MESSAGE);
                                                                } else if (mes.equals("")) {
                                                                    JOptionPane.showMessageDialog(null, "Message cannot " +
                                                                            "be an empty string", title, JOptionPane.ERROR_MESSAGE);
                                                                } else {
                                                                    pwServer.write(mes);
                                                                    pwServer.println();
                                                                    pwServer.flush();
                                                                    break;
                                                                }
                                                            }
                                                        } else if (fileOrText == 1) {            // if user sends txt file
                                                            // as a message
                                                            String fileName;
                                                            while (true) {
                                                                fileName = JOptionPane.showInputDialog(null, "Enter " +
                                                                        "name of txt file: ", title, JOptionPane.PLAIN_MESSAGE);    // enters name of the file
                                                                if (fileName == null) {
                                                                    JOptionPane.showMessageDialog(null, "Please write a " +
                                                                            "filename", title, JOptionPane.ERROR_MESSAGE);
                                                                } else if (fileName.equals("")) {
                                                                    JOptionPane.showMessageDialog(null, "Message cannot " +
                                                                            "be an empty string", title, JOptionPane.ERROR_MESSAGE);
                                                                } else {
                                                                    pwServer.write(fileName);
                                                                    pwServer.println();
                                                                    pwServer.flush();
                                                                    break;
                                                                }
                                                            }
                                                            String reportFromServer = bfrServer.readLine();
                                                            if (reportFromServer.equals("Success")) {
                                                                JOptionPane.showMessageDialog(null, "Your message was" +
                                                                                " successfully written from the given file",
                                                                        title, JOptionPane.INFORMATION_MESSAGE);
                                                            } else {
                                                                JOptionPane.showMessageDialog(null, "File provided " +
                                                                                "from you does not EXIST",
                                                                        title, JOptionPane.ERROR_MESSAGE);
                                                            }
                                                        }
                                                    } else {
                                                        JOptionPane.showMessageDialog(null, "You can't write to this " +
                                                                "user because they are blocked", title, JOptionPane.ERROR_MESSAGE, null);
                                                    }
                                                } else if (optionChoice == 1) {              //
                                                    // if user chooses to edit messages (for more detailed comments refer to line 210)
                                                    messageHistory = (ArrayList<Message>) ois.readObject();
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
                                                        while (true) {
                                                            choice = (int) JOptionPane.showInputDialog(null,
                                                                    messageHist + "\nSelect message to edit",
                                                                    title, JOptionPane.QUESTION_MESSAGE, null, messageNums,
                                                                    messageNums[0]); //user chooses which message available for him to edit he wants to edit
                                                            if (choice == -1) {
                                                                JOptionPane.showMessageDialog(null, "Enter the " +
                                                                        "correct choice", title, JOptionPane.ERROR_MESSAGE);
                                                            }
                                                            else {
                                                                pwServer.write(choice);
                                                                pwServer.flush();
                                                                break;
                                                            }
                                                        }

                                                        String msg;
                                                        while (true) {
                                                            msg = JOptionPane.showInputDialog(null,"Enter new " +
                                                                    "message: ", title, JOptionPane.PLAIN_MESSAGE);
                                                            if (msg == null) {
                                                                JOptionPane.showMessageDialog(null, "Enter the message",
                                                                        title, JOptionPane.ERROR_MESSAGE);
                                                            }
                                                            else if (msg.equals("")) {
                                                                JOptionPane.showMessageDialog(null, "Message cannot " +
                                                                                "be an empty line", title, JOptionPane.ERROR_MESSAGE);
                                                            }
                                                            else {
                                                                pwServer.write(msg);
                                                                pwServer.println();
                                                                pwServer.flush();
                                                                break;
                                                            }
                                                        }
                                                    } else {
                                                        JOptionPane.showMessageDialog(null, "No messages to edit with" +
                                                                " this user", title, JOptionPane.ERROR_MESSAGE);
                                                    }
                                                } else if (optionChoice == 2) {                 // if user chooses
                                                    // to delete the message (more detailed comments on the line 258)
                                                    messageHistory = (ArrayList<Message>) ois.readObject();
                                                    int i = 0;
                                                    messageHist = new StringBuilder(String.format("Message " +
                                                                    "History: %s - " +
                                                                    "%s%n----------------------------------------%n",
                                                            currUser.getUsername(), listOfUsers[receiveUser - 1]));
                                                    while (i < messageHistory.size()) {
                                                        // printing every message with a number next to it
                                                        messageHist.append(String.format("[%d] " + messageHistory.get(i).toString(), i + 1));
                                                        i++;
                                                    }
                                                    if (i > 0) {
                                                        Integer[] messageNums = new Integer[i];
                                                        for (int j = 0; j < i; j++) {
                                                            messageNums[j] = j + 1;
                                                        }
                                                        while (true) {
                                                            choice = (int) JOptionPane.showInputDialog(null,
                                                                    messageHist + "\nSelect message to delete",
                                                                    title, JOptionPane.QUESTION_MESSAGE, null, messageNums,
                                                                    messageNums[0]);  // user chooses which message to delete
                                                            if (choice == -1) {
                                                                JOptionPane.showMessageDialog(null, "Enter the " +
                                                                        "correct choice", title, JOptionPane.ERROR_MESSAGE);
                                                            }
                                                            else {
                                                                pwServer.write(choice);
                                                                pwServer.flush();
                                                                break;
                                                            }
                                                        }
                                                    } else {
                                                        JOptionPane.showMessageDialog(null, "No messages to delete",
                                                                title, JOptionPane.ERROR_MESSAGE);
                                                    }
                                                } else if (optionChoice == 3) {            // if he chooses to export messages to the csv file
                                                    String fileName;
                                                    while (true) {
                                                        fileName = JOptionPane.showInputDialog(null, "Enter " +
                                                                "name of the file to which you want to export your " +
                                                                "message history", title, JOptionPane.QUESTION_MESSAGE); // enters the file name
                                                        if (fileName == null) {
                                                            JOptionPane.showMessageDialog(null, "You need to enter " +
                                                                    "filename", title, JOptionPane.ERROR_MESSAGE);
                                                        }
                                                        else if (fileName.equals("")) {
                                                            JOptionPane.showMessageDialog(null, "Filename can't an " +
                                                                            "empty string", title, JOptionPane.ERROR_MESSAGE);
                                                        }
                                                        else {
                                                            pwServer.write(fileName);
                                                            pwServer.println();
                                                            pwServer.flush();
                                                            break;
                                                        }
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
                                if (alphabetical == 0 || alphabetical == 1 || alphabetical == 2) {
                                    String stats = bfrServer.readLine().replaceAll("\\\\n","\n");
                                    // shows user statistics in alphabetical order & reverse alphabetical order
                                    JOptionPane.showMessageDialog(null, stats, title,
                                            JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    break;
                                }
                            }
                            break;
                        case 2:
                            // options for the user's account
                            while (true) {
                                if (currUser != null) {
                                    title = String.format("%s - Account Details%n", currUser.getUsername());
                                    String userInfo = String.format("Email: %s%nPassword: %s%n", email,
                                            pass);
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
                                        pwServer.flush();
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
                                                    if (newAccountInfo.isEmpty()) {
                                                        JOptionPane.showMessageDialog(null, "That email was not valid\n " +
                                                                "(Email didn't contain an @ and . or email was " +
                                                                "already in use", "title", JOptionPane.WARNING_MESSAGE);
                                                    }
                                                } while (newAccountInfo.isEmpty());
                                                // shows that the user's email was changed
                                                JOptionPane.showMessageDialog(null, String.format("Email " +
                                                        "changed to: %s%n", newAccountInfo), title, JOptionPane.INFORMATION_MESSAGE);
                                                email = newAccountInfo;
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
                                                    pass = newAccountInfo;
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
                                            loggedIn = false;
                                        }
                                    } else if (choice == 2) {
                                        // user selects block/unblock users
                                        String blockedUsers = bfrServer.readLine().replaceAll("\\\\n", "\n");
                                        // Asks if user wants to block or unblock a user
                                        options = new String[]{"Block New User", "Unblock User", "Exit"};
                                        choice = JOptionPane.showOptionDialog(null, blockedUsers, title,
                                                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
                                                options[2]);
                                        pwServer.write(choice);
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
                                                String action = bfrServer.readLine();
                                                if (action.equals("yes")) {
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
                                                String[] blockedUsersArr = (String[]) ois.readObject();
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
                                                    action = bfrServer.readLine();
                                                    if (action.equals("yes")) {
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
                                                break;
                                        }
                                    } else if (choice == 3 && currUser instanceof Seller) {
                                        String storeName;
                                        String userStores = bfrServer.readLine().replaceAll("\\\\n", "\n");
                                        do {
                                            // if user is seller allow user to create store
                                            storeName = JOptionPane.showInputDialog(null,
                                                    userStores, title, JOptionPane.QUESTION_MESSAGE);
                                        } while (storeName == null);
                                        pwServer.write(storeName);
                                        pwServer.println();
                                        pwServer.flush();
                                        JOptionPane.showMessageDialog(null, "Store " + storeName + " was added",
                                                title, JOptionPane.INFORMATION_MESSAGE);
                                        break;
                                    } else {
                                        break;
                                    }
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
        bfrServer.close();
        pwServer.close();
        oos.close();
        ois.close();
    }
}

