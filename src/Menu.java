import java.io.*;
import java.util.*;

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
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        boolean online = true;
        String response;
        ArrayList<User> users = readUsers("login.csv");                 // Each line in the "login.csv" file is a User object, using special method we read whole file into an ArrayList of Users
        ArrayList<Store> stores = readStores("stores.csv", users);      // We do the same thing with the stores objects
        addBlockedUsers(users);
        while (online) {
            System.out.println("Welcome to the Marketplace Messaging System");
            System.out.println("--------------------------------------------");
            boolean LoggingIn = true;
            boolean loggedIn = false;
            User user = null;
            User currUser = null;
            System.out.println("Please enter the number corresponding with your option:");
            while (LoggingIn) {                                                  // An infinite loop that breaks when user is able to log in
                System.out.println("[1] Login\n[2] Create Account\n[3] Exit");   // User is presented with 3 options: log in into existing acc, create new acc, or exit the program
                response = scanner.nextLine();
                switch (response) {
                    case "1":                                        // If user wants to log in into existing acc, if it's successful, infinite loop breaks
                        user = login(scanner);
                        if (user != null)
                            LoggingIn = false;                  // to end an infinite loop
                        break;
                    case "2":
                        user = createAccount(scanner, "login.csv");          // After creating acc, user is already counted as logged-in user
                        if (user != null) {
                            LoggingIn = false;              // breaks infinite loop
                            currUser = user;                // user is logged in
                            users.add(user);                // add user to the ArrayList of all users
                        }
                        break;
                    case "3":                    // To exit program
                        user = null;
                        LoggingIn = false;
                        online = false;           // When online is false, program stops working
                        break;
                    default:
                        System.out.println("Please enter a valid input");
                        user = null;
                }
            }
            if (user != null) {
                System.out.println("Successfully logged in as " + user.getUsername());         // Confirmation message, when user is able to log in
                System.out.println();
                for (User u : users) {
                    if (u.getUsername().equalsIgnoreCase(user.getUsername())) {
                        currUser = u;
                        loggedIn = true;
                    }
                }
            }
            writeUsers("login.csv",users);          // Updates login.csv file, after finishing the logging in process, in case if new users are created
            while (loggedIn) {
                if (currUser != null) {
                    try {
                        System.out.println("--Main Menu--");
                        /*
                        When user logs in, he is presented with 4 options:
                        1) Messages is the part of the program where user is able to send messages to either Customers or Sellers, depending on who is User itself
                        2) View statistics about user like most used words, or stores to which he messages the most
                        3) Account is for changing your password or email
                        0) Exit is to log off from the program
                         */
                        System.out.println("[1] Messages\n[2] Statistics\n[3] Account\n[0] Exit");
                        int choice = scanner.nextInt();
                        scanner.nextLine();
                        switch (choice) {
                            case 1:                     // If user chooses to Message another person
                                System.out.printf("%s - Message Log%n", currUser.getUsername());
                                System.out.println("--------------");
                                if (currUser instanceof Seller) {                     //If user is Seller then this part of the code will run for him in the message section
                                    while (true) {
                                        ArrayList<Message> messageHistory;
                                        /*
                                        parseUsers(user) parse through messages.csv and collects a list of users, with which
                                        user had conversations before. That way we are able to avoid situation where messages not
                                        related to the user are being used.
                                        */
                                        String[] listOfUsers = parseUsers(user);
                                        for (int i = 0; i < listOfUsers.length; i++) {
                                            System.out.printf("[%d] %s%n", i + 1, listOfUsers[i]);          // Displays every user in the listOfUsers list
                                        }
                                        System.out.printf("[%d] %s%n", 0, "Start new dialog");           // We provide an option to start new dialog
                                        System.out.printf("[%d] %s%n", -1, "Exit");                      // We provide an option to exit the messages part of the program, to view statistics or change account settings
                                        int receiveUser = Integer.parseInt(scanner.nextLine());          // He makes the choice
                                        if (receiveUser == -1) {
                                            break;                                          // If user chooses to exit, we break the infinite loop, and user is able to choose statistics or account settings
                                        }
                                        if (receiveUser == 0) {                                          // dialog with new user
                                            System.out.println("List of Available Buyers to Message: ");
                                            for (User u : users) {
                                                if (u instanceof Buyer) {
                                                    System.out.println(u.getUsername());
                                                }
                                            }
                                            System.out.println();
                                            System.out.println("Enter name of user:");
                                            String newUser = scanner.nextLine();         // Enter name of the new user
                                            boolean alreadyMessaged = false;
                                            for (String u : listOfUsers) {
                                                if (u.equals(newUser)) {
                                                    alreadyMessaged = true;                 // if you already messaged the user before, it will show this message that you already messaged him before
                                                    System.out.println("You already messaged this user");
                                                }
                                            }
                                            boolean flag = true;           // This flag is responsible for identifying if sender and receiver are the same type
                                            boolean flag1 = true;          // This flag is responsible for showing if user exists
                                            boolean flag2 = true;          // This flag is responsible for checking if user to which you are texting blocked you, or you blocked that user before
                                            for (User value : users) {
                                                if (value.getUsername().equals(newUser)) {
                                                    flag1 = false;          // flag1 = false; means that user to which you are trying to text indeed exists
                                                    if (value instanceof Seller) {
                                                        System.out.println("You can't write to Seller, because you are Seller yourself");
                                                        flag = false;       // means that user to which you are trying to text is also an instance of Seller, which should be possible
                                                                            // you should only be able to text Buyers as a Seller
                                                    } else if (currUser.getBlockedUsers().contains(value) || value.getBlockedUsers().contains(currUser)) {
                                                        System.out.println("You can't write to this user because they are blocked");
                                                        flag2 = false;      // flag2 = false; means that neither of you blocked each other
                                                    }
                                                }
                                            }
                                            if (flag1) {     // if flag1 is true, user does NOT exist
                                                System.out.println("USER DOES NOT EXIST");
                                            } else if (flag && flag2 && !alreadyMessaged) {     // this code runs if
                                                                                                // user exists, user is Buyer, you didn't block each other
                                                System.out.println("Write your hello message first!");
                                                String mes = scanner.nextLine();               // user enters the message he would want to send to new user
                                                ArrayList<Message> temp = user.getMessages();  // creates new ArrayList with user messages
                                                temp.add(new Message(user.getUsername(), newUser, mes));    // adds new message to that ArrayList
                                                user.setMessages(temp);                        // updates the messages field on the user
                                                messageHistory = parseMessageHistory(user, newUser);     // after the messages field was updated, we update the messageHistory and print that out
                                                for (Message message : messageHistory) {
                                                    System.out.print(message.toString());                     //we print their message history
                                                }
                                            }
                                        } else if (receiveUser >= 1) {           // if user doesn't choose to start new dialog or exit the program
                                                                                 // receiveUser is to view conversations you had before with other users
                                            while (true) {
                                                messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);        // update messageHistory to print that out
                                                for (Message value : messageHistory) {
                                                    if (value.getMessage().contains("\\n")) {        // this part of the code is here in case if message has multiple lines in it
                                                        String ansMes = value.getMessage().replaceAll("\\\\n", "\n");  // it replaces signs of new lines, to actual new lines
                                                        String ans = String.format("%s   (%s -> %s)%n%s%n", value.getTime(), value.getSender(), value.getReceiver(), ansMes);  // same implementation as in Message class, but with specific message string
                                                        System.out.print(ans);
                                                    } else
                                                        System.out.print(value);     // if it's regular one line message, then it uses basic toString() method of Message class
                                                }
                                                System.out.println();
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
                                                System.out.println("[1] Write message                         [2] Edit message");
                                                System.out.println("[3] Delete message                        [0] Exit");
                                                System.out.println("[-1] Export this message history to csv file");
                                                int optionChoice = Integer.parseInt(scanner.nextLine());            // enters which option you want to do
                                                if (optionChoice == -1) {            // exporting messages
                                                    System.out.println("Enter name of the file to which you want to export your message history");
                                                    String fileName = scanner.nextLine();            // enters the file name
                                                    PrintWriter pw = new PrintWriter(new FileOutputStream(fileName,false));
                                                    for (Message msg : messageHistory) {
                                                        // this line writes Message object in the same manner as it does in main "messages.csv" file
                                                        String ans = String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", msg.getId(), msg.getTime(), msg.getSender(), msg.getReceiver(), msg.getMessage(), msg.isDelBySender(), msg.isDelByReceiver());
                                                        pw.write(ans);
                                                        pw.println();
                                                        pw.flush();
                                                    }
                                                    System.out.println("Your message history was successfully saved to "+fileName);          // confirmation that history was saved
                                                    System.out.println();
                                                }
                                                if (optionChoice == 1) {            // writing new messages
                                                    System.out.println("You want to send a message or upload a txt file?\n[1] Send message\n[2] Upload file");    // you are presented with two options as described before
                                                                                                                                                                  // 1 - regular message          2 - upload a txt file
                                                    int fileOrText = Integer.parseInt(scanner.nextLine());
                                                    if (fileOrText == 1) {       // regular message
                                                        System.out.println("Enter message: ");
                                                        String mes = scanner.nextLine();
                                                        ArrayList<Message> temp = user.getMessages();
                                                        temp.add(new Message(user.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                        user.setMessages(temp);        // updates the messages field of the user to the renewed messageHistory
                                                    } else if (fileOrText == 2) {      //uploading files
                                                        System.out.println("Enter name of txt file: ");
                                                        String fileName = scanner.nextLine();         // enters name of the file
                                                        String mes = "";
                                                        try {
                                                            ArrayList<String> tempArr = new ArrayList<>();
                                                            BufferedReader bfr = new BufferedReader(new FileReader(fileName));
                                                            String st;
                                                            while ((st = bfr.readLine()) != null) {
                                                                tempArr.add(st);                     // reads whole file and saves lines to ArrayList
                                                            }
                                                            mes = String.join("\\n",tempArr);              // combine all lines in the file by \\n which shows up as \n in the messages.csv file
                                                                                                                   // we read it as new line when writing all messages
                                                            ArrayList<Message> temp = user.getMessages();
                                                            temp.add(new Message(user.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                            user.setMessages(temp);                  // updates the messages field of the user
                                                        }
                                                        catch (FileNotFoundException e) {         // if user enters file that does not exist
                                                            System.out.println("I'm sorry but that file does not exist");
                                                        }
                                                    }
                                                }
                                                if (optionChoice == 2) {          // editing messages
                                                    messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);
                                                    ArrayList<Message> userIsSender = new ArrayList<>();         // here only messages that are sends by the current user will be saved
                                                    int i = 0;
                                                    int z = 0;
                                                    while (z < messageHistory.size()) {
                                                        if (messageHistory.get(z).getSender().equals(user.getUsername())) {      // checks if message is sent by the main user
                                                            userIsSender.add(messageHistory.get(z));
                                                            System.out.printf("[%d] " + messageHistory.get(z).toString(), i + 1);      // if message is sent by the main user, the number
                                                                                                                                        // will appear next to it
                                                            i++;
                                                        } else
                                                            System.out.print(messageHistory.get(z).toString());     // if main user is receiver, then message is printed as usual with any number next to it
                                                        z++;
                                                    }
                                                    System.out.println("Choose message to edit");
                                                    choice = Integer.parseInt(scanner.nextLine());           // user chooses which message available for him to edit he wants to edit
                                                    System.out.println("To which message you want to change it?");
                                                    String msg = scanner.nextLine();                   // user enters the message to which user wants to change his message
                                                    Message temp = userIsSender.get(choice - 1);       // we grab value form the userIsSender which stores only messages where main user is sender
                                                    for (Message message : messageHistory) {
                                                        if (message.getId() == temp.getId()) {
                                                            message.setMessage(msg);                   // when we find that message in the main message history, we change its text
                                                        }
                                                    }
                                                }
                                                if (optionChoice == 3) {             // deleting messages
                                                    messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);       // we save message history
                                                    ArrayList<Message> userIsSender = new ArrayList<>();         // I guess here I was kind of lazy, so userIsSender now stores every message in it
                                                                                                                // because in deleting messages it doesn't really matter if you aren't creator of the message
                                                                                                                // since you can delete whether message you wish for
                                                    int i = 0;
                                                    while (i < messageHistory.size()) {
                                                        userIsSender.add(messageHistory.get(i));              // adding every message into the userIsSender arraylist
                                                        System.out.printf("[%d] " + messageHistory.get(i).toString(), i + 1);             // printing every message with a number next to it
                                                        i++;
                                                    }
                                                    System.out.println("Choose message to delete");
                                                    choice = Integer.parseInt(scanner.nextLine());      // user chooses which message to delete
                                                    Message temp = userIsSender.get(choice - 1);        // we assign the message user chose to Message temp variable
                                                    ArrayList<Message> allUserMessages = user.getMessages();
                                                    for (int j = 0; j < allUserMessages.size(); j++) {
                                                        if (allUserMessages.get(j).getId() == temp.getId()) {         // finding temp message in the main allUserMessages ArrayList
                                                            if (temp.getSender().equals(user.getUsername()))          // if main user was sender
                                                                allUserMessages.get(j).setDelBySender(true);          // then message becomes invisible to the sender
                                                            else                                                      // if main user was receiver
                                                                allUserMessages.get(j).setDelByReceiver(true);        // then message becomes invisible to the receiver
                                                            user.setMessages(allUserMessages);                        // updates the messages field of the user after deleting the message
                                                            break;
                                                        }
                                                    }
                                                    user.refreshMessages();            // refreshMessages is used to remove some messages in the messages field of the user, because we need to be
                                                                                       // manually remove some messages in the messages field. setMessages isn't enough, because it doesn't actually remove messages
                                                                                       // it only updates its values
                                                }
                                                if (optionChoice == 0) {          // if user chooses to exit, we break from infinite loop
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    saveMessages(user);                          // after everything is finished, we save the current messages field of the user to the messages.csv using this method
                                } else if (currUser instanceof Buyer) {
                                    /*
                                    if you are buyer, when trying to enter the Messaging part of the program, you will be presented with 3 options
                                    1) Either you write to the store that interests you
                                    When you write message to the message, your message will be sent to the owner of the store, and you will be redirected to the conversation with Seller
                                    2) Or you write to a specific seller, just like you do message with a normal person
                                    0) Exits to the menu
                                    */
                                    System.out.println("[1] Write to store\n[2] Write to seller\n[0] Exit");
                                    int makeChoice = Integer.parseInt(scanner.nextLine());            // user chooses the option
                                    if (makeChoice == 0) {
                                        break;          // breaks out of infinite loop if user chooses to exit
                                    } else if (makeChoice == 1) {           // user chooses to text the store
                                        System.out.println("List of Stores:");
                                        for (User value : users) {
                                            if (value instanceof Seller) {
                                                for (String storeName: ((Seller) value).getStores()) {
                                                    System.out.println(storeName);              // name of the existing stores are printed, and all you need is to
                                                                                                // write the name of the store you want to text to
                                                }
                                            }
                                        }
                                        System.out.println("Enter name of the store");
                                        String store = scanner.nextLine();                  // user enters the name of the store
                                        boolean flag = false;                 // responsible for showing if store exists
                                        for (User value : users) {
                                            if (value instanceof Seller) {                  // goes through every User in users ArrayList, and chooses only Seller to parse through
                                                                                            // their stores, and understand to which Seller is the store that user entered belongs to
                                                for (int j = 0; j < ((Seller) value).getStores().size(); j++) {
                                                    if (((Seller) value).getStores().get(j).equals(store)) {          // if store belongs to the Seller, then Seller's object is saved as "value" variable
                                                        flag = true;
                                                        System.out.println("Enter message you want to send to that store");
                                                        String msg = scanner.nextLine();           // main user writes the message to the store
                                                        ArrayList<Message> temp = currUser.getMessages();
                                                        temp.add(new Message(currUser.getUsername(), value.getUsername(), msg));                  // writes new message
                                                        user.setMessages(temp);               // update the messages field of the user
                                                        System.out.println("Store manager's username is " + value.getUsername());          // tells user who is owner of the store
                                                        System.out.println("Please wait for his message");
                                                        for (Store s : ((Seller) value).getNewStores()) {
                                                            if (s.getStoreName().equalsIgnoreCase(store)) {
                                                                s.addMessagesReceived();
                                                                if (!s.getUserMessaged().contains(currUser)) {
                                                                    s.addUserMessaged((Buyer) currUser);
                                                                }
                                                                for (Store st : stores) {
                                                                    if (s.getStoreName().equalsIgnoreCase(st.getStoreName())) {
                                                                        st.addMessagesReceived();
                                                                        if (!st.getUserMessaged().contains(currUser)) {
                                                                            st.addUserMessaged((Buyer) currUser);
                                                                        }
                                                                    }
                                                                }

                                                                writeStores("stores.csv", stores);
                                                            }
                                                        }
                                                        ArrayList<Message> messageHistory = parseMessageHistory(user, value.getUsername());          // we update our message history and print that out
                                                        for (Message message : messageHistory) {
                                                            if (message.getMessage().contains("\\n")) {        // if message and not a single line messages
                                                                String ansMes = message.getMessage().replaceAll("\\\\n", "\n");
                                                                String ans = String.format("%s   (%s -> %s)%n%s%n", message.getTime(), message.getSender(), message.getReceiver(), ansMes);
                                                                System.out.print(ans);
                                                            } else {
                                                                System.out.print(message.toString());       // if message is a single line message
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (!flag) {              // flag is false when store doesn't exist
                                            System.out.println("That store doesn't exist!");
                                        }
                                        saveMessages(currUser);                            // updates the messages.csv file with the changes that have been made to the messages field of the user
                                    } else if (makeChoice == 2) {                          // if user chooses to write to the specific Seller directly
                                        while (true) {
                                            ArrayList<Message> messageHistory;
                                            String[] listOfUsers = parseUsers(user);                        // List of users with whom he had conversations before
                                            for (int i = 0; i < listOfUsers.length; i++) {
                                                System.out.printf("[%d] %s%n", i + 1, listOfUsers[i]);      // prints the name of every user
                                            }
                                            System.out.printf("[%d] %s%n", 0, "Start new dialog");           // We provide an option to start new dialog
                                            System.out.printf("[%d] %s%n", -1, "Exit");
                                            int receiveUser = Integer.parseInt(scanner.nextLine());          // He makes the choice
                                            if (receiveUser == -1) {
                                                break;
                                            }
                                            if (receiveUser == 0) {                                          // dialog with new user
                                                System.out.println("List of Available Sellers to Message: ");
                                                for (User u : users) {
                                                    if (u instanceof Seller) {
                                                        System.out.println(u.getUsername());
                                                    }
                                                }
                                                System.out.println();
                                                System.out.println("Enter name of user:");
                                                String newUser = scanner.nextLine();
                                                boolean alreadyMessaged = false;
                                                for (String u : listOfUsers) {
                                                    if (u.equals(newUser)) {
                                                        alreadyMessaged = true;
                                                        System.out.println("You can't start a new dialog with a user you already messaged!");
                                                    }
                                                }
                                                boolean flag = true;
                                                boolean flag1 = true;               // same logic as it was in the line 123, read comments there
                                                boolean flag2 = true;
                                                for (User value : users) {
                                                    if (value.getUsername().equals(newUser)) {
                                                        flag1 = false;
                                                        if (value instanceof Buyer) {
                                                            System.out.println("You can't write to Buyer, because you are Buyer yourself");
                                                            flag = false;
                                                        } else if (currUser.getBlockedUsers().contains(value) || value.getBlockedUsers().contains(currUser)) {
                                                            System.out.println("You can't write to this user because they are blocked");
                                                            flag2 = false;
                                                        }
                                                    }
                                                }
                                                if (flag1) {
                                                    System.out.println("USER DOES NOT EXIST");
                                                } else if (flag && flag2 && !alreadyMessaged) {
                                                    System.out.println("Write your hello message first!");
                                                    String mes = scanner.nextLine();                                        // writing message to the new user
                                                    ArrayList<Message> temp = user.getMessages();
                                                    temp.add(new Message(user.getUsername(), newUser, mes));
                                                    user.setMessages(temp);
                                                    messageHistory = parseMessageHistory(user, newUser);
                                                    for (Message message : messageHistory) {
                                                        if (message.getMessage().contains("\\n")) {
                                                            String ansMes = message.getMessage().replaceAll("\\\\n", "\n");
                                                            String ans = String.format("%s   (%s -> %s)%n%s%n", message.getTime(), message.getSender(), message.getReceiver(), ansMes);
                                                            System.out.print(ans);
                                                        } else
                                                            System.out.print(message.toString());
                                                    }
                                                }
                                            } else if (receiveUser >= 1) {           // if user chooses to continue conversation with the user he had conversation before
                                                while (true) {
                                                    messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);
                                                    for (Message message : messageHistory) {                            // prints out every message
                                                        if (message.getMessage().contains("\\n")) {
                                                            String ansMes = message.getMessage().replaceAll("\\\\n", "\n");
                                                            String ans = String.format("%s   (%s -> %s)%n%s%n", message.getTime(), message.getSender(), message.getReceiver(), ansMes);
                                                            System.out.print(ans);
                                                        } else
                                                            System.out.print(message);
                                                    }
                                                    System.out.println();
                                                    /*
                                                     read comments on the line 166, identical features
                                                    */
                                                    System.out.println("[1] Write message                         [2] Edit message");
                                                    System.out.println("[3] Delete message                        [0] Exit");
                                                    System.out.println("[-1] Export this message history to csv file");
                                                    int optionChoice = Integer.parseInt(scanner.nextLine());
                                                    if (optionChoice == -1) {                   // if he chooses to export messages to the csv file
                                                        System.out.println("Enter name of the file to which you want to export your message history");
                                                        String fileName = scanner.nextLine();
                                                        PrintWriter pw = new PrintWriter(new FileOutputStream(fileName,false));
                                                        for (Message msg : messageHistory) {
                                                            String ans = String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", msg.getId(), msg.getTime(), msg.getSender(), msg.getReceiver(), msg.getMessage(), msg.isDelBySender(), msg.isDelByReceiver());
                                                            pw.write(ans);
                                                            pw.println();
                                                            pw.flush();
                                                        }
                                                        System.out.println("Your message history was successfully saved to "+fileName);
                                                        System.out.println();
                                                    }
                                                    if (optionChoice == 1) {                             //if user chooses to write a new message
                                                        System.out.println("You want to send a message or upload a txt file?\n[1] Send message\n[2] Upload file");
                                                        int fileOrText = Integer.parseInt(scanner.nextLine());
                                                        if (fileOrText == 1) {              // if user sends regular message
                                                            System.out.println("Enter message: ");
                                                            String mes = scanner.nextLine();
                                                            ArrayList<Message> temp = user.getMessages();
                                                            temp.add(new Message(user.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                            user.setMessages(temp);
                                                        }
                                                        else if (fileOrText == 2) {            // if user sends txt file as a message
                                                            System.out.println("Enter name of txt file: ");
                                                            String fileName = scanner.nextLine();
                                                            String mes = "";
                                                            ArrayList<String> tempArr = new ArrayList<>();
                                                            try {
                                                                BufferedReader bfr = new BufferedReader(new FileReader(fileName));
                                                                String st;
                                                                while ((st = bfr.readLine()) != null) {
                                                                    tempArr.add(st);
                                                                }
                                                                mes = String.join("\\n",tempArr);
                                                                ArrayList<Message> temp = user.getMessages();
                                                                temp.add(new Message(user.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                                user.setMessages(temp);
                                                            }
                                                            catch (FileNotFoundException e) {
                                                                System.out.println("I'm sorry but that file does not exist");
                                                            }
                                                        }
                                                    }
                                                    if (optionChoice == 2) {              // if user chooses to edit messages (for more detailed comments refer to line 210)
                                                        messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);
                                                        ArrayList<Message> userIsSender = new ArrayList<>();
                                                        int i = 0;
                                                        while (i < messageHistory.size()) {
                                                            if (messageHistory.get(i).getSender().equals(user.getUsername())) {
                                                                userIsSender.add(messageHistory.get(i));
                                                                System.out.printf("[%d] " + messageHistory.get(i).toString(), i + 1);
                                                                i++;
                                                            } else
                                                                System.out.print(messageHistory.get(i).toString());
                                                        }
                                                        System.out.println("Choose message to edit");
                                                        choice = Integer.parseInt(scanner.nextLine());
                                                        System.out.println("To which message you want to change it?");
                                                        String msg = scanner.nextLine();
                                                        Message temp = userIsSender.get(choice - 1);
                                                        for (Message message : messageHistory) {
                                                            if (message.getId() == temp.getId()) {
                                                                message.setMessage(msg);                        // updates the messages field of the user
                                                            }
                                                        }
                                                    }
                                                    if (optionChoice == 3) {                 // if user chooses to delete the message (more detailed comments on the line 258)
                                                        messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);
                                                        ArrayList<Message> userIsSender = new ArrayList<>();
                                                        int i = 0;
                                                        while (i < messageHistory.size()) {
                                                            userIsSender.add(messageHistory.get(i));
                                                            System.out.printf("[%d] " + messageHistory.get(i).toString(), i + 1);
                                                            i++;
                                                        }
                                                        System.out.println("Choose message to delete");
                                                        choice = Integer.parseInt(scanner.nextLine());
                                                        Message temp = userIsSender.get(choice - 1);
                                                        ArrayList<Message> allUserMessages = user.getMessages();
                                                        for (int j = 0; j < allUserMessages.size(); j++) {
                                                            if (allUserMessages.get(j).getId() == temp.getId()) {
                                                                if (temp.getSender().equals(user.getUsername()))
                                                                    allUserMessages.get(j).setDelBySender(true);
                                                                else
                                                                    allUserMessages.get(j).setDelByReceiver(true);
                                                                user.setMessages(allUserMessages);
                                                                break;
                                                            }
                                                        }
                                                        user.refreshMessages();
                                                    }
                                                    if (optionChoice == 0) {
                                                        break;
                                                    }
                                                }
                                            } else {
                                                System.out.println("Please enter a valid number");
                                            }
                                        }
                                        saveMessages(user);                        // saves changed to the messages.csv after finishing the messages part of the program
                                    }
                                }
                                break;
                            case 2:                              // this is Statistics part of the code
                                while (true) {
                                    System.out.printf("%s - Statistics%n", currUser.getUsername());
                                    System.out.println("--------------");
                                    /*
                                    User is presented with 4 options
                                    */
                                    System.out.println("Select in which order you want to sort\n[1] Alphabetical\n[2] Reverse alphabetical\n[3] Most common words\n[0] Exit");
                                    int alphabetical = Integer.parseInt(scanner.nextLine()); // Assigns alphabetical to user choice as an int.
                                    if (currUser instanceof Buyer) {
                                        if (alphabetical == 1)
                                            currUser.viewStatistics(true); // shows user statistics in alphabetical order
                                        else if (alphabetical == 2)
                                            currUser.viewStatistics(false); // shows user statistics in reverse alphabetical order
                                        else if (alphabetical == 3) {
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
                                            System.out.println("The most common word in Messages is " + word + " said " + maxCount + " times");
                                            System.out.println("The second common word in Messages is " + secondWord + " said " + secondCount + " times");
                                            System.out.println("The third most common word in Messages is " + thirdWord + " said " + thirdCount + " times");
                                            System.out.println();
                                        } else if (alphabetical == 0)
                                            break;
                                    } else if (currUser instanceof Seller) {
                                        Map<String, Integer> sentMessages = new HashMap<>();
                                        for (User u : users) { // Iterates through every user
                                            int count;
                                            ArrayList<Message> messages;
                                            if (!u.equals(currUser) && u instanceof Buyer) {
                                                 messages = parseStoreMessages(currUser, u.getUsername()); // gets all messages sent to the current user's store from a user
                                                count = messages.size(); // gets number of messages from the sender
                                                sentMessages.put(u.getUsername(), count); // assigns the users and number of messages they sent to a hashmap
                                            }
                                        }
                                        ArrayList<String> sortedSentMessages = new ArrayList<>(sentMessages.keySet());
                                        Collections.sort(sortedSentMessages); // sorts users
                                        if (alphabetical == 1) {
                                            for (String s : sortedSentMessages) {
                                                System.out.printf("%s sent %d messages%n", s, sentMessages.get(s)); // writes the user and number of messages they sent alphabetically
                                            }
                                        } else if (alphabetical == 2) {
                                            for (int j = sortedSentMessages.size() - 1; j >= 0; j--) {
                                                System.out.printf("%s sent %d messages%n", sortedSentMessages.get(j), sentMessages.get(sortedSentMessages.get(j))); // // writes the user and number of messages they sent reverse alphabetically
                                            }
                                        } else if (alphabetical == 0) {
                                            break;
                                        } else if (alphabetical == 3) {
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
                                                    allMessages.addAll(parseMessageHistory(currUser, u1.getUsername()));
                                                }
                                            }
                                            String message = "";
                                            for (Message m : allMessages) {
                                                message += m.getMessage() + " ";
                                            }
                                            // Gets the most commonly used word and the number of times it is used
                                            String[] wordArr = message.split(" ");
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
                                            System.out.println("The most common word in Messages is " + word + " said " + maxCount + " times");
                                            System.out.println("The second common word in Messages is " + secondWord + " said " + secondCount + " times");
                                            System.out.println("The third most common word in Messages is " + thirdWord + " said " + thirdCount + " times");
                                            System.out.println();
                                        }
                                    }

                                }
                                break;
                            case 3:
                                // options for the user's account
                                do {
                                    System.out.printf("%s - Account Details%n", currUser.getUsername());
                                    System.out.println("--------------");
                                    System.out.printf("Email: %s%nPassword: %s%n", currUser.getEmail(), currUser.getPassword());
                                    if (currUser instanceof Seller) {
                                        // shows 4 options if user is a seller
                                        System.out.println("[1] Edit Account\n[2] Delete Account\n[3] Block/Unblock User\n[4] Create New Store\n[0] Exit");
                                    } else {
                                        // shows 3 options if user is a buyer
                                        System.out.println("[1] Edit Account\n[2] Delete Account\n[3] Block/Unblock User\n[0] Exit");
                                    }
                                    choice = scanner.nextInt();
                                    if (choice > 4) {
                                        throw new InputMismatchException();
                                    }
                                    switch (choice) {
                                        case 1:                     // user selects edit account
                                            scanner.nextLine();
                                            System.out.println("[1] Change Email\n[2] Change Password\n[0] Exit");
                                            choice = scanner.nextInt();
                                            String newAccountInfo;
                                            if (choice > 2) {
                                                throw new InputMismatchException(); // throws an exception if user types invalid option
                                            }
                                            switch (choice) {
                                                case 1 -> {         // user selects change email
                                                    scanner.nextLine();
                                                    do {
                                                        System.out.println("Enter new email:");
                                                        newAccountInfo = scanner.nextLine();       // user types new email
                                                        if (newAccountInfo.contains("@") && newAccountInfo.contains(".")) {
                                                            currUser.setEmail(newAccountInfo); // if new email is valid changes current user's email to new email
                                                        } else {
                                                            System.out.println("Error: Enter a valid email!");  // user inputs an invalid email (does not contain @ and .)
                                                            newAccountInfo = "";
                                                        }
                                                    } while (newAccountInfo.isEmpty());
                                                    System.out.printf("Email changed to: %s%n", newAccountInfo); // shows that the user's email was changed
                                                }
                                                case 2 -> {         // user selects change password
                                                    scanner.nextLine();
                                                    System.out.println("Enter new password:");
                                                    newAccountInfo = scanner.nextLine(); // user types new password
                                                    currUser.setPassword(newAccountInfo); // changes user's password to new password
                                                    System.out.printf("Password changed to: %s%n", newAccountInfo); // shows that the user's password was changed
                                                }
                                            }
                                            break;
                                        case 2:         // user selects delete account
                                            scanner.nextLine();
                                            System.out.println("Are you sure you want to delete this user? [Y/N]");     // makes sure user wants to delete their account
                                            String yesNo = scanner.nextLine();      // user selects Y or N
                                            if (yesNo.equalsIgnoreCase("Y")) {
                                                // user selects Y, their account is deleted (Their messages to other users will remain)
                                                System.out.printf("User [%s] successfully deleted%n", currUser.getUsername());
                                                currUser.removeUser();
                                                users.remove(currUser);
                                                choice = 3;
                                                currUser = null;
                                            }
                                            break;
                                        case 3: // user selects block/unblock users
                                            System.out.println("Blocked Users: ");
                                            for (User b : currUser.getBlockedUsers()) { // shows list of currently blocked users
                                                System.out.println(b.getUsername());
                                            }
                                            System.out.println("--------------");
                                            System.out.println("[1] Block new User\n[2] Unblock Users\n[3] Exit"); // Asks if user wants to block or unblock a user
                                            choice = scanner.nextInt();
                                            scanner.nextLine();
                                            switch (choice) {
                                                case 1:         // user selects block user
                                                    System.out.println("Enter name of user to block:");
                                                    String blockUsername = scanner.nextLine(); // user enters username of user to block
                                                    if (currUser.blockUser(blockUsername, users)) {
                                                        System.out.println(blockUsername + " blocked"); // if that user exist they are blocked
                                                    } else {
                                                        System.out.println("That user doesn't exist"); // if they don't exist tell user
                                                    }
                                                    break;
                                                case 2:        // user select unblock user
                                                    System.out.println("Enter name of user to unblock:");
                                                    String unblockUsername = scanner.nextLine(); // user enters username of user to unblock
                                                    if (currUser.unblockUser(unblockUsername, users)) {
                                                        System.out.println(unblockUsername + " unblocked"); // if that user is currently blocked they are unblocked
                                                    } else {
                                                        System.out.println("That user doesn't exist in your blocked list"); // if they aren't blocked tell user
                                                    }
                                                    break;
                                                case 3:
                                                    break;
                                            }
                                            writeUsers("login.csv", users); // writes changes to login.csv file
                                            break;
                                        case 4:
                                            if (currUser instanceof Buyer) {
                                                break; // if user is a buyer do nothing
                                            } else if (currUser instanceof Seller) {    // if user is seller allow user to create store
                                                System.out.println("Your Stores:");
                                                for (String storeName : ((Seller) currUser).getStores()) {
                                                    System.out.println(storeName); // shows list of current stores by current user
                                                }
                                                System.out.println("--------------");
                                                scanner.nextLine();
                                                System.out.println("Enter name for new store");
                                                String storeName = scanner.nextLine();
                                                ((Seller) currUser).createStore(storeName); // adds new store
                                                stores.add(new Store(storeName,0));
                                                writeStores("stores.csv", stores); // updates stores.csv
                                                writeUsers("login.csv", users); // updates login.csv
                                                break;
                                            }
                                        default:
                                            break;
                                    }
                                } while (choice != 0 && currUser != null);
                                break;
                            default:
                                loggedIn = false;
                                break;
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input");
                        scanner.nextLine();
                    }
                }
            }
            if (currUser != null) {
                System.out.println("Successfully Logged out\n"); // user logs out
            } else {
                System.out.println("Thank you for using the messaging service"); // user leaves program
            }
            writeUsers("login.csv", users); // updates login.csv
            writeStores("stores.csv", stores); // updates stores.csv
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
            e.printStackTrace();
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
            if(s.charAt(i)==',' && notInsideComma)
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
public static User login(Scanner scanner) {
    // Initialize variables
        ArrayList<String[]> users = new ArrayList<>();
        ArrayList<String> tempArrayList = new ArrayList<>();
        String[] tempArray;
        ArrayList<String> transferList;
        boolean invEmail;
        String email, pass;
        //Add users from file to arraylist
        try {
            BufferedReader bfr = new BufferedReader(new FileReader("login.csv"));
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
            e.printStackTrace();
        }
    //Loops forever until a valid email and password are entered, or the escape sequence is ran
        while (true) {
            System.out.println("Please enter your email:");
            email = scanner.nextLine();
            System.out.println("Please enter you password:");
            pass = scanner.nextLine();
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
                System.out.println("Your email was incorrect");
            } else {
                System.out.println("Your password was incorrect");
            }
            String option;
            do {
                System.out.println("Would you like to try again?\n1.Yes\n2.No");
                option = scanner.nextLine();
                if (option.equals("2")) {
                    return null;
                } else if (!(option.equals("1"))) {
                    System.out.println("Please enter a valid option");
                }
            } while (!option.equals("1"));
        }
    }

    public static User createAccount(Scanner scanner, String file) {
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
            e.printStackTrace();
        }
        //Loops until a valid email is inputted, a valid email being an email that is
        //not previously used and does not have and @ sign or comma
        System.out.println("A valid email contains an @ sign and has no commas");
        while(invEmail){
            System.out.print("Please enter a valid email: ");
            email = scanner.nextLine();
            for (String[] strings : userFile) {
                if (email.equals(strings[1])) {
                    repeatEmail = true;
                    break;
                }
            }
            if (email.contains(",") || !email.contains("@")) {
                System.out.println("That user name was not valid");
            } else if (repeatEmail) {
                System.out.println("Someone else has that email please enter a different one.");
                repeatEmail = false;
            } else {
                invEmail = false;
            }
        }
        //Loops until a valid username is inputted, a valid username being a username that is
        //not previously used and does not have a comma
        System.out.println("A valid username contains no commas");
        while(invUsername){
            System.out.print("Please enter a valid username: ");
            userName = scanner.nextLine();
            for (String[] strings : userFile) {
                if (userName.equals(strings[0])) {
                    repeatUser = true;
                    break;
                }
            }
            if (userName.contains(",") || userName.equals("")) {
                System.out.println("That user name was not valid");
            } else if (repeatUser) {
                System.out.println("Someone else has that user name please enter a different one.");
                repeatUser = false;
            } else {
                invUsername = false;
            }
        }
        //Loops until a password is inputted
        while(invPass){
            System.out.print("Please enter a password: ");
            pass = scanner.nextLine();
            if (pass == null || pass.equals("")) {
                System.out.println("That password was not valid");
            } else {
                invPass = false;
            }
        }
        //Loops until buyer/seller is inputted
        System.out.println("A valid user type is either Buyer or Seller");
        while(invBuyer){
            System.out.print("Please enter a valid user type: ");
            userType = scanner.nextLine();
            if (userType.equalsIgnoreCase("Buyer")) {
                userType = "b";
                user = new Buyer(userName, email, pass);
                invBuyer = false;
            }else if (userType.equalsIgnoreCase("Seller")) {
                userType = "s";
                user = new Seller(userName, email, pass);
                invBuyer = false;
            } else {
                System.out.println("That user type was not valid");
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
            e.printStackTrace();
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
                throw new RuntimeException(e);
            } finally {
                try {
                    if (bfr != null) {
                        bfr.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
                throw new RuntimeException(e);
            } finally {
                try {
                    if (bfr != null) {
                        bfr.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
            e.printStackTrace();
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

