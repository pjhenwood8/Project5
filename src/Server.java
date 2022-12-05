import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4242);
        //ArrayList<Message> allMessages = readWholeFile();
        ArrayList<User> allUsers = readUsers("login.csv");
        ArrayList<Store> allStores = readStores("stores.csv", allUsers);
        addBlockedUsers(allUsers);
        int numOfBuyers = 0;
        int numOfSellers = 0;
        for (User u : allUsers) {
            if (u instanceof Buyer) {
                numOfBuyers++;
            } else if (u instanceof Seller) {
                numOfSellers++;
            }
        }
        String[] buyers = new String[numOfBuyers];
        int n = 0;
        for (User u : allUsers) {
            if (u instanceof Buyer) {
                buyers[n] = u.getUsername();
                n++;
            }
        }
        String[] sellers = new String[numOfSellers];
        n = 0;
        for (User u : allUsers) {
            if (u instanceof Seller) {
                sellers[n] = u.getUsername();
                n++;
            }
        }
        String[] userArr = new String[allUsers.size()];
        n = 0;
        for (User u : allUsers) {
            userArr[n] = u.getUsername();
            n++;
        }

        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                while (true) {
                    User user = null;
                    int choice = reader.read();
                    if (choice == 0) {                 // LOGIN / CREATE ACCOUNT / EXIT
                        String email = reader.readLine();
                        String password = reader.readLine();
                        user = login(email, password, allUsers);
                    }
                    else if (choice == 1) {         // LOGIN / CREATE ACCOUNT / EXIT
                        String email = reader.readLine();
                        String username = reader.readLine();
                        String password = reader.readLine();
                        int type = reader.read();
                        try {
                            user = createAccount(email, username, password, type, allUsers);
                        } catch (LoginException e) {
                            user = null;
                        }
                        if (user != null) {
                            allUsers.add(user);
                            writeUsers("login.csv", allUsers);
                        }
                    } else {
                        break;
                    }
                    oos.writeObject(user);
                    if (user != null) {
                        choice = Integer.parseInt(reader.readLine());
                        if (choice == 0) {       // MESSAGES / STATISTICS / ACCOUNT / EXIT
                            if (user instanceof Buyer) {
                                ArrayList<Message> messageHistory = null;
                                choice = Integer.parseInt(reader.readLine());
                                if (choice == 1) {      //WRITE TO STORE / WRITE TO SELLER / EXIT
                                    oos.writeObject(allStores);   // we send the store list
                                    // in order for them to choose one to text
                                    oos.flush();

                                    String storeNameToMessage = reader.readLine();       // user enters the name of the store
                                    String message = reader.readLine();                  // and the message itself also
                                    User sellerToMessage = null;

                                    for (User tempUser : allUsers) {          // we find the owner of the store
                                        if (tempUser instanceof Seller) {
                                            for (Store tempStore : ((Seller) tempUser).getNewStores()) {
                                                if (tempStore.getStoreName().equals(storeNameToMessage)) {
                                                    sellerToMessage = tempUser;
                                                }
                                            }
                                        }
                                    }

                                    writer.write(sellerToMessage.getUsername());
                                    writer.println();
                                    writer.flush();

                                    ArrayList<Message> temp = user.getMessages();
                                    temp.add(new Message(user.getUsername(),
                                            sellerToMessage.getUsername(), message));                  //
                                    // writes new message
                                    user.setMessages(temp);               // update the messages field of
                                    // the user
                                    for (Store s : ((Seller) sellerToMessage).getNewStores()) {    //didn't quite
                                        // understand this part of the code Jalen wrote
                                        if (s.getStoreName().equalsIgnoreCase(storeNameToMessage)) {
                                            s.addMessagesReceived();
                                            if (!s.getUserMessaged().contains(user)) {
                                                s.addUserMessaged((Buyer) user);
                                            }
                                            for (Store st : allStores) {
                                                if (s.getStoreName().equalsIgnoreCase(st.getStoreName())) {
                                                    st.addMessagesReceived();
                                                    if (!st.getUserMessaged().contains(user)) {
                                                        st.addUserMessaged((Buyer) user);
                                                    }
                                                }
                                            }
                                            writeStores("stores.csv", allStores);
                                        }
                                    }

                                    messageHistory = parseMessageHistory(user,
                                            sellerToMessage.getUsername());          // we update our message
                                    // history and print that out
                                    oos.writeObject(messageHistory);
                                    oos.flush();

                                    saveMessages(user);
                                } else if (choice == 2) {              //WRITE TO STORE/ WRITE TO SELLER / EXIT
                                    // if user chooses to write to the specific Seller directly
                                    while (true) {
                                        String[] listOfUsers = parseUsers(user);                        // List of users with whom he had conversations before

                                        oos.writeObject(listOfUsers);       //send it to client
                                        oos.flush();

                                        choice = Integer.parseInt(reader.readLine());

                                        int receiveUser = choice;          // He makes the choice

                                        //-1 is EXIT   /    0 is START NEW DIALOG    /
                                        // All other non-negative numbers are list of users he had conversations before
                                        if (receiveUser == -1) {               //   EXIT
                                            break;
                                        }

                                        if (receiveUser == 0) {                 // START NEW DIALOG
                                            ArrayList<String> listOfSellers = new ArrayList<String>();
                                            for (User u : allUsers) {
                                                if (u instanceof Seller) {
                                                    listOfSellers.add(u.getUsername());
                                                }
                                            }

                                            oos.writeObject(listOfSellers);          // we send to Buyer all sellers
                                                                                     // he can text to
                                            oos.flush();

                                            String sellerToWrite = reader.readLine();      // we ask him to write one
                                                                                           // seller name

                                            boolean alreadyMessaged = false;
                                            for (String u : listOfUsers) {
                                                if (u.equals(sellerToWrite)) {
                                                    alreadyMessaged = true;
                                                    writer.write("You can't start a new dialog with a " +
                                                            "user you already messaged!");
                                                    writer.println();
                                                    writer.flush();
                                                }
                                            }
                                            boolean flag = true;
                                            boolean flag1 = true;               // same logic as it was in the line 123, read comments there
                                            boolean flag2 = true;
                                            for (User value : allUsers) {
                                                if (value.getUsername().equals(sellerToWrite)) {
                                                    flag1 = false;
                                                    if (value instanceof Buyer) {
                                                        writer.write("You can't write to Buyer, because you " +
                                                                "are Buyer yourself");
                                                        writer.println();
                                                        writer.flush();

                                                        flag = false;
                                                    } else if (user.getBlockedUsers().contains(value) || value.getBlockedUsers().contains(user)) {
                                                        writer.write("You can't write to this user because they are blocked");
                                                        writer.println();
                                                        writer.flush();
                                                        flag2 = false;
                                                    }
                                                }
                                            }
                                            if (flag1) {
                                                writer.write("USER DOES NOT EXIST");
                                                writer.println();
                                                writer.flush();
                                            } else if (flag && flag2 && !alreadyMessaged) {
                                                writer.write("Write your hello message first!");
                                                writer.println();
                                                writer.flush();

                                                String mes = reader.readLine();
                                                // writing message to the new user
                                                ArrayList<Message> userMessagesTemp = user.getMessages();
                                                userMessagesTemp.add(new Message(user.getUsername(), sellerToWrite, mes));
                                                user.setMessages(userMessagesTemp);

                                                messageHistory = parseMessageHistory(user, sellerToWrite);

                                                oos.writeObject(messageHistory);

//                                                            for (Message tempMessage : messageHistory) {
//                                                                if (tempMessage.getMessage().contains("\\n")) {
//                                                                    String ansMes = tempMessage.getMessage().replaceAll(
//                                                                            "\\\\n", "\n");
//                                                                    String ans = String.format("%s   (%s -> %s)" +
//                                                                            "%n%s%n", tempMessage.getTime(),
//                                                                            tempMessage.getSender(),
//                                                                            tempMessage.getReceiver(), ansMes);
//                                                                    System.out.print(ans);
//                                                                } else
//                                                                    System.out.print(message.toString());
//                                                            }
                                            }
                                        } else if (receiveUser >= 1) {           // if user chooses to continue conversation with the user he had conversation before
                                            while (true) {
                                                messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);

                                                oos.writeObject(messageHistory);
                                                oos.flush();
//                                                for (Message message : messageHistory) {                            // prints out every message
//                                                    if (message.getMessage().contains("\\n")) {
//                                                        String ansMes = message.getMessage().replaceAll("\\\\n", "\n");
//                                                        String ans = String.format("%s   (%s -> %s)%n%s%n", message.getTime(), message.getSender(), message.getReceiver(), ansMes);
//                                                        System.out.print(ans);
//                                                    } else
//                                                        System.out.print(message);
//                                                }
//                                                System.out.println();
                                                    /*
                                                     read comments on the line 166, identical features
                                                    */
//                                                System.out.println("[1] Write message                         [2] Edit message");
//                                                System.out.println("[3] Delete message                        [0] Exit");
//                                                System.out.println("[-1] Export this message history to csv file");
                                                int optionChoice = Integer.parseInt(scanner.nextLine());     //
                                                // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                                // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                                if (optionChoice == -1) {                   // if he chooses to export messages to the csv file

                                                    //System.out.println("Enter name of the file to which you want to
                                                    // export your message history");

                                                    String fileName = reader.readLine();
                                                    PrintWriter pw = new PrintWriter(new FileOutputStream(fileName, false));
                                                    for (Message msg : messageHistory) {
                                                        String ans = String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", msg.getId(), msg.getTime(), msg.getSender(), msg.getReceiver(), msg.getMessage(), msg.isDelBySender(), msg.isDelByReceiver());
                                                        pw.write(ans);
                                                        pw.println();
                                                        pw.flush();
                                                    }

//                                                    System.out.println("Your message history was successfully saved to " + fileName);
//                                                    System.out.println();
                                                }
                                                // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                                // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                                if (optionChoice == 1) {                             //if user chooses to write a new message
                                                    //System.out.println("You want to send a message or upload a txt
                                                    // file?\n[1] Send message\n[2] Upload file");
                                                    choice = Integer.parseInt(reader.readLine());
                                                    int fileOrText = choice;
                                                    // 1-SEND MESSAGE   /   2-UPLOAD FILE
                                                    if (fileOrText == 1) {              // if user sends regular message
                                                        // System.out.println("Enter message: ");
                                                        String mes = reader.readLine();
                                                        ArrayList<Message> temp = user.getMessages();
                                                        temp.add(new Message(user.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                        user.setMessages(temp);
                                                    // 1-SEND MESSAGE   /   2-UPLOAD FILE
                                                    } else if (fileOrText == 2) {            // if user sends txt file as a message
                                                        // System.out.println("Enter name of txt file: ");
                                                        String fileName = reader.readLine();
                                                        String mes = "";
                                                        ArrayList<String> tempArr = new ArrayList<>();
                                                        try {
                                                            BufferedReader bfr = new BufferedReader(new FileReader(fileName));
                                                            String st;
                                                            while ((st = bfr.readLine()) != null) {
                                                                tempArr.add(st);
                                                            }
                                                            mes = String.join("\\n", tempArr);
                                                            ArrayList<Message> temp = user.getMessages();
                                                            temp.add(new Message(user.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                            user.setMessages(temp);
                                                        } catch (FileNotFoundException e) {
                                                            writer.write("I'm sorry but that file does not exist");
                                                            writer.println();
                                                            writer.flush();
                                                        }
                                                    }
                                                }
                                                // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                                // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                                if (optionChoice == 2) {              // if user chooses to edit messages (for more detailed comments refer to line 210)
                                                    messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);
                                                    ArrayList<Message> userIsSender = new ArrayList<>();
                                                    int i = 0;
                                                    int z = 0;
                                                    while (z < messageHistory.size()) {
                                                        if (messageHistory.get(z).getSender().equals(user.getUsername())) {      // checks if message is sent by the main user
                                                            userIsSender.add(messageHistory.get(z));
                                                            //System.out.printf("[%d] " + messageHistory.get(z)
                                                            // .toString(), i + 1);      // if message is sent by the main user, the number
                                                            // will appear next to it
                                                            i++;
                                                        } else {}
                                                            //System.out.print(messageHistory.get(z).toString());
                                                        // if main user is receiver, then message is printed as usual with any number next to it
                                                        z++;
                                                    }
                                                    // System.out.println("Choose message to edit");
                                                    choice = Integer.parseInt(reader.readLine());     // user chooses
                                                    // which message to edit by typing in the number

                                                    //System.out.println("To which message you want to change it?");
                                                    String msg = reader.readLine();

                                                    Message temp = userIsSender.get(choice - 1);
                                                    for (Message message : messageHistory) {
                                                        if (message.getId() == temp.getId()) {
                                                            message.setMessage(msg);                        // updates the messages field of the user
                                                        }
                                                    }
                                                }
                                                // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                                // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                                if (optionChoice == 3) {                 // if user chooses to delete the message (more detailed comments on the line 258)
                                                    messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);
                                                    ArrayList<Message> userIsSender = new ArrayList<>();
                                                    int i = 0;
                                                    while (i < messageHistory.size()) {
                                                        userIsSender.add(messageHistory.get(i));       //
                                                        // userIsSender is basically all messages
//                                                        System.out.printf("[%d] " + messageHistory.get(i).toString(), i + 1);
                                                        i++;
                                                    }
                                                    //System.out.println("Choose message to delete");
                                                    choice = Integer.parseInt(reader.readLine());
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
                                                // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                                // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                                if (optionChoice == 0) {
                                                    break;
                                                }
                                            }
                                        }
                                        // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                        // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                        else {
                                            writer.write("Please enter a valid number");
                                            writer.println();
                                            writer.flush();
                                        }
                                    }
                                    saveMessages(user);                        // saves changed to the messages.csv after finishing the messages part of the program
                                } else if (choice == 0) {            //WRITE TO STORE/ WRITE TO SELLER / EXIT
                                    break;
                                }
                            } else if (user instanceof Seller) {
                                while (true) {
                                    ArrayList<Message> messageHistory;
                                        /*
                                        parseUsers(user) parse through messages.csv and collects a list of users, with which
                                        user had conversations before. That way we are able to avoid situation where messages not
                                        related to the user are being used.
                                        */
                                    String[] listOfUsers = parseUsers(user);
//                                    for (int i = 0; i < listOfUsers.length; i++) {
//                                        System.out.printf("[%d] %s%n", i + 1, listOfUsers[i]);          // Displays every user in the listOfUsers list
//                                    }
//                                    System.out.printf("[%d] %s%n", 0, "Start new dialog");           // We provide an option to start new dialog
//                                    System.out.printf("[%d] %s%n", -1, "Exit");                      // We provide an option to exit the messages part of the program, to view statistics or change account settings

                                    choice = Integer.parseInt(reader.readLine());

                                    int receiveUser = choice;          // He makes the choice
                                    if (receiveUser == -1) {
                                        break;                                          // If user chooses to exit, we break the infinite loop, and user is able to choose statistics or account settings
                                    }
                                    if (receiveUser == 0) {                                          // dialog with new user

//                                        System.out.println("List of Available Buyers to Message: ");
                                        ArrayList<String> buyerUserNames = new ArrayList<>();
                                        for (User u : allUsers) {
                                            if (u instanceof Buyer) {
                                                buyerUserNames.add(u.getUsername());
                                            }
                                        }
                                        oos.writeObject(buyerUserNames);
                                        oos.flush();

//                                        System.out.println();
//                                        System.out.println("Enter name of user:");
                                        String newUser = reader.readLine();         // Enter name of the new user
                                        boolean alreadyMessaged = false;
                                        for (String u : listOfUsers) {
                                            if (u.equals(newUser)) {
                                                alreadyMessaged = true;                 // if you already messaged the user before, it will show this message that you already messaged him before
                                                writer.write("You already messaged this user");
                                                writer.println();
                                                writer.flush();
                                            }
                                        }
                                        boolean flag = true;           // This flag is responsible for identifying if sender and receiver are the same type
                                        boolean flag1 = true;          // This flag is responsible for showing if user exists
                                        boolean flag2 = true;          // This flag is responsible for checking if user to which you are texting blocked you, or you blocked that user before
                                        for (User value : allUsers) {
                                            if (value.getUsername().equals(newUser)) {
                                                flag1 = false;          // flag1 = false; means that user to which you are trying to text indeed exists
                                                if (value instanceof Seller) {
                                                    writer.write("You can't write to Seller, because you are Seller " +
                                                        "yourself");
                                                    writer.println();
                                                    writer.flush();
                                                    flag = false;       // means that user to which you are trying to text is also an instance of Seller, which should be possible
                                                    // you should only be able to text Buyers as a Seller
                                                } else if (user.getBlockedUsers().contains(value) || value.getBlockedUsers().contains(user)) {
                                                    writer.write("You can't write to this user because they are blocked");
                                                    writer.println();
                                                    writer.flush();
                                                    flag2 = false;      // flag2 = false; means that neither of you blocked each other
                                                }
                                            }
                                        }
                                        if (flag1) {     // if flag1 is true, user does NOT exist
                                            writer.write("USER DOES NOT EXIST");
                                            writer.println();
                                            writer.flush();
                                        } else if (flag && flag2 && !alreadyMessaged) {     // this code runs if
                                            // user exists, user is Buyer, you didn't block each other

                                            String mes = reader.readLine();               // user enters the message he would
                                            // want to send to new user
                                            ArrayList<Message> temp = user.getMessages();  // creates new ArrayList with user messages
                                            temp.add(new Message(user.getUsername(), newUser, mes));    // adds new message to that ArrayList
                                            user.setMessages(temp);                        // updates the messages field on the user
                                            messageHistory = parseMessageHistory(user, newUser);     // after the messages field was updated, we update the messageHistory and print that out

//                                            for (Message message : messageHistory) {
//                                                System.out.print(message.toString());                     //we print their message history
//                                            }
                                            oos.writeObject(messageHistory);
                                            oos.flush();
                                        }
                                    } else if (receiveUser >= 1) {           // if user doesn't choose to start new dialog or exit the program
                                        // receiveUser is to view conversations you had before with other users
                                        while (true) {
                                            messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);        // update messageHistory to print that out
                                            oos.writeObject(messageHistory);
                                            oos.flush();
//                                            for (Message value : messageHistory) {
//                                                if (value.getMessage().contains("\\n")) {        // this part of the code is here in case if message has multiple lines in it
//                                                    String ansMes = value.getMessage().replaceAll("\\\\n", "\n");  // it replaces signs of new lines, to actual new lines
//                                                    String ans = String.format("%s   (%s -> %s)%n%s%n", value.getTime(), value.getSender(), value.getReceiver(), ansMes);  // same implementation as in Message class, but with specific message string
//                                                    System.out.print(ans);
//                                                } else
//                                                    System.out.print(value);     // if it's regular one line message, then it uses basic toString() method of Message class
//                                            }
//                                            System.out.println();
                                                /* User is presented with 5 options of what they can in the Message part with specific user
                                                   1) Write new message
                                                   when writing new message you are presented with 2 options
                                                        1) Either write a regular message
                                                            you type a message and it send it to the receiver
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
//                                            System.out.println("[1] Write message                         [2] Edit message");
//                                            System.out.println("[3] Delete message                        [0] Exit");
//                                            System.out.println("[-1] Export this message history to csv file");

                                            int optionChoice = Integer.parseInt(reader.readLine());            // enters which
                                            // option you want to do
                                            if (optionChoice == -1) {            // exporting messages
//                                                System.out.println("Enter name of the file to which you want to export your message history");
                                                String fileName = reader.readLine();            // enters the file name
                                                PrintWriter pw = new PrintWriter(new FileOutputStream(fileName,false));
                                                for (Message msg : messageHistory) {
                                                    // this line writes Message object in the same manner as it does in main "messages.csv" file
                                                    String ans = String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", msg.getId(), msg.getTime(), msg.getSender(), msg.getReceiver(), msg.getMessage(), msg.isDelBySender(), msg.isDelByReceiver());
                                                    pw.write(ans);
                                                    pw.println();
                                                    pw.flush();
                                                }
//                                                System.out.println("Your message history was successfully saved to "+fileName);          // confirmation that history was saved
//                                                System.out.println();
                                            }
                                            if (optionChoice == 1) {            // writing new messages
                                                //System.out.println("You want to send a message or upload a txt " +
                                                //      "file?\n[1] Send message\n[2] Upload file");    // you are
                                                // presented with two options as described before
                                                // 1 - regular message          2 - upload a txt file
                                                int fileOrText = Integer.parseInt(scanner.nextLine());
                                                if (fileOrText == 1) {       // regular message
                                                    //System.out.println("Enter message: ");
                                                    String mes = reader.readLine();
                                                    ArrayList<Message> temp = user.getMessages();
                                                    temp.add(new Message(user.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                    user.setMessages(temp);        // updates the messages field of the user to the renewed messageHistory
                                                } else if (fileOrText == 2) {      //uploading files
                                                    //System.out.println("Enter name of txt file: ");
                                                    String fileName = reader.readLine();         // enters name of the file
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
                                                        writer.write("I'm sorry but that file does not exist");
                                                        writer.println();
                                                        writer.flush();
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
                                                //System.out.println("Choose message to edit");
                                                choice = Integer.parseInt(reader.readLine());           // user chooses which
                                                // message
                                                // available for him to edit he wants to edit
                                                //System.out.println("To which message you want to change it?");
                                                String msg = reader.readLine();                   // user enters the message
                                                // to
                                                // which user wants to change his message
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
                                                    //System.out.printf("[%d] " + messageHistory.get(i).toString(),i + 1);             // printing every message with a number next to it
                                                    i++;
                                                }
                                                //System.out.println("Choose message to delete");
                                                choice = Integer.parseInt(reader.readLine());      // user chooses which
                                                // message to delete
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
                                saveMessages(user);
                            }
                        }
                        else if (choice == 1) {        // MESSAGES / STATISTICS / ACCOUNT / EXIT

                        }
                        else if (choice == 2) {        // MESSAGES / STATISTICS / ACCOUNT / EXIT

                        }
                        else {        // MESSAGES / STATISTICS / ACCOUNT / EXIT

                        }
                    }
                }
            }
            catch (Exception e) {
                int imHereToAvoidCheckStyle;
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<Message> readWholeFile() throws IOException {
        // this method is responsible for reading the messages.csv file, and converting the every line there to the
        // Message object. Method return ArrayList of those Message objects
        ArrayList<Message> fileContent = new ArrayList<>();
        BufferedReader bfr = new BufferedReader(new FileReader("messages.csv"));
        String st;
        while ((st = bfr.readLine()) != null) {
            ArrayList<String> temp = customSplitSpecific(st);
            for (int i = 0; i < temp.size(); i++) {
                temp.set(i, temp.get(i).substring(1, temp.get(i).length()-1));
            }
            // we use specific constructor in the Message.java to assign values to the Message object when reading from file
            fileContent.add(new Message(Integer.parseInt(temp.get(0)),temp.get(1),temp.get(2),temp.get(3),temp.get(4),Boolean.parseBoolean(temp.get(5)),Boolean.parseBoolean(temp.get(6))));
        }
        return fileContent;
    }

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

    public static User login(String email, String password, ArrayList<User> users) {
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public static User createAccount(String email, String username, String password,
                                     int type, ArrayList<User> users) throws LoginException {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                throw new LoginException("This email is already taken, please select another email");
            }
            if (user.getUsername().equals(username)) {
                throw new LoginException("This username is already taken, please select another username");
            }
        }
        User answer = null;
        if (type == 1) {
            answer = new Seller(username, email, password);
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream("login.csv", true));
                pw.println(answer.toString());
                pw.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (type == 0) {
            answer = new Buyer(username, email, password);
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream("login.csv", true));
                pw.println(answer.toString());
                pw.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return answer;
    }

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

    public static User createAccount(String username, String email, String password, String userType) {
        ArrayList<ArrayList<String>> existingUsers = new ArrayList<>();
        ArrayList<String> file = new ArrayList<>();
        try {
            BufferedReader bfr = new BufferedReader(new FileReader("login.csv"));
            String line = bfr.readLine();
            while (line != null) {
                file.add(line);
                existingUsers.add(customSplitSpecific(line));
                line = bfr.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ArrayList<String> users : existingUsers) {
            if (users.get(0).equals(username)){
                return null;
            }
            if (users.get(1).equals(email)){
                return null;
            }
        }
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream("login.csv", false));
            for (String strings : file) {
                pw.println(strings);
            }
            pw.println("\"" + username + "\"" + "," + "\"" + email + "\"" + "," + "\"" + password + "\"" + "," + "\"" + userType + "\"" + ",\"\"");
            pw.close();
        } catch (IOException e ) {
            e.printStackTrace();
        }
        return new User(username,email,password);
    }

    public static void addBlockedUsers(ArrayList<User> users) {
        for (User u : users) {
            ArrayList<String> blockedUsernames = u.getBlockedUsernames();
            for (String bUser : blockedUsernames) {
                u.blockUser(bUser, users);
            }
        }
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
}
