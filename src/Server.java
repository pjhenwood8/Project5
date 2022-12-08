import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4242);
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

            try {
                Scanner scanner = new Scanner(System.in);
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                boolean online = true;
                while (online) {
                    User user = null;
                    boolean loggingIn = true;
                    while (true) {
                        while (loggingIn) {
                            int choice = reader.read();
                            if (choice == 0) {                 // LOGIN / CREATE ACCOUNT / EXIT
                                String email = reader.readLine();
                                String password = reader.readLine();
                                user = login(email, password, allUsers);
                            } else if (choice == 1) {         // LOGIN / CREATE ACCOUNT / EXIT
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
                                online = false;
                                break;
                            }
                            oos.writeObject(user);
                            if (user != null) {
                                loggingIn = false;
                                oos.writeObject(buyers);
                                oos.writeObject(sellers);
                                oos.writeObject(userArr);
                            }
                        }
                        int choice = reader.read();
                        if (choice == 0) {       // MESSAGES / STATISTICS / ACCOUNT / EXIT
                            if (user instanceof Buyer) {
                                ArrayList<Message> messageHistory = null;
                                choice = reader.read();

                                if (choice == 0) {      //WRITE TO STORE / WRITE TO SELLER / EXIT
                                    oos.writeObject(allStores);   // we send the store list in order for them to choose one to text
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

                                    oos.writeObject(sellerToMessage);
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
                                    saveMessages(user);
                                } else if (choice == 1) {              //WRITE TO STORE/ WRITE TO SELLER / EXIT
                                    // if user chooses to write to the specific Seller directly
                                    while (true) {
                                        String[] listOfUsers = parseUsers(user);                        // List of users with whom he had conversations before

                                        oos.writeObject(listOfUsers);       //send it to client
                                        oos.flush();

                                        String[] options = (String[]) ois.readObject();

                                        choice = reader.read();

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
//
                                            oos.writeObject(listOfSellers);          // we send to Buyer all sellers
//                                                                                     // he can text to
                                            oos.flush();

                                            String sellerToWrite = reader.readLine();      // we ask him to write one
                                            // seller name
                                            User seller = null;
                                            for (User value : allUsers) {
                                                if (value.getUsername().equals(sellerToWrite)) {
                                                    seller = value;
                                                    break;
                                                }
                                            }

                                            boolean alreadyMessaged = false;

                                            for (String u : listOfUsers) {
                                                if (u.equals(sellerToWrite)) {
                                                    alreadyMessaged = true;
                                                    writer.write(0);
                                                    writer.flush();
                                                }
                                            }
                                            // same logic as it was in the line 123, read comments there
                                            boolean flag2 = true;
                                            if (user.getBlockedUsers().contains(seller) || seller.getBlockedUsers().contains(user)) {
                                                flag2 = false;
                                                writer.write(1);
                                                writer.flush();
                                            }


                                            if (flag2 && !alreadyMessaged) {
                                                writer.write(2);
                                                writer.flush();
                                                String mes = reader.readLine();
                                                ArrayList<Message> userMessagesTemp = user.getMessages();
                                                userMessagesTemp.add(new Message(user.getUsername(), sellerToWrite, mes));
                                                user.setMessages(userMessagesTemp);
                                            }
                                        } else if (receiveUser >= 1 && receiveUser != options.length - 1) {           // if user chooses to continue
                                            // conversation with the user he had conversation before
                                            while (true) {
                                                messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);

                                                oos.writeObject(messageHistory);
                                                oos.flush();
//
                                                int optionChoice = reader.read();     //
                                                // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                                // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE

                                                // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                                // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                                if (optionChoice == 0) {                             //if user
                                                    // chooses to write a new message
                                                    //System.out.println("You want to send a message or upload a txt
                                                    // file?\n[1] Send message\n[2] Upload file");
                                                    choice = reader.read();
                                                    int fileOrText = choice;
                                                    // 1-SEND MESSAGE   /   2-UPLOAD FILE
                                                    if (fileOrText == 0) {              // if user sends regular message
                                                        // System.out.println("Enter message: ");
                                                        String mes = reader.readLine();
                                                        ArrayList<Message> temp = user.getMessages();
                                                        temp.add(new Message(user.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                        user.setMessages(temp);
                                                        // 1-SEND MESSAGE   /   2-UPLOAD FILE
                                                    } else if (fileOrText == 1) {            // if user sends txt
                                                        // file as a message
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
                                                            writer.write("Success");
                                                            writer.println();
                                                            writer.flush();
                                                        } catch (FileNotFoundException e) {
                                                            writer.write("I'm sorry but that file does not exist");
                                                            writer.println();
                                                            writer.flush();
                                                        }
                                                    }
                                                }
                                                // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                                // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                                else if (optionChoice == 1) {              // if user chooses to edit
                                                    // messages (for more detailed comments refer to line 210)
                                                    messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);
                                                    oos.writeObject(messageHistory);
                                                    oos.flush();
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
                                                        } else {
                                                        }
                                                        //System.out.print(messageHistory.get(z).toString());
                                                        // if main user is receiver, then message is printed as usual with any number next to it
                                                        z++;
                                                    }
                                                    // System.out.println("Choose message to edit");
                                                    if (i > 0) {
                                                        choice = reader.read();     // user chooses
                                                        // which message to edit by typing in the number
                                                        //System.out.println("To which message you want to change it?");
                                                        String msg = reader.readLine();
                                                        Message temp = userIsSender.get(choice - 1);
                                                        for (Message message : messageHistory) {
                                                            if (message.getId() == temp.getId()) {
                                                                message.setMessage(msg);                        // updates the messages field of the user
                                                            }
                                                        }
                                                        saveMessages(user);
                                                    }
                                                }
                                                // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                                // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                                else if (optionChoice == 2) {                 // if user chooses to
                                                    // delete
                                                    // the message (more detailed comments on the line 258)
                                                    messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);

                                                    oos.writeObject(messageHistory);
                                                    oos.flush();

                                                    ArrayList<Message> userIsSender = new ArrayList<>();
                                                    int i = 0;
                                                    while (i < messageHistory.size()) {
                                                        userIsSender.add(messageHistory.get(i));       //
                                                        // userIsSender is basically all messages
//                                                        System.out.printf("[%d] " + messageHistory.get(i).toString(), i + 1);
                                                        i++;
                                                    }
                                                    //System.out.println("Choose message to delete");
                                                    if (i > 0) {
                                                        choice = reader.read();
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
                                                } else if (optionChoice == 3) {                   // if he chooses to export
                                                    // messages to the csv file

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
                                                else {
                                                    break;
                                                }
                                            }
                                        }
                                        // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                        // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                        else {
                                            break;
                                        }
                                    }
                                    saveMessages(user);                        // saves changed to the messages.csv after finishing the messages part of the program
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
                                    oos.writeObject(listOfUsers);
                                    oos.flush();

                                    choice = reader.read();

                                    int receiveUser = choice;          // He makes the choice
                                    if (receiveUser == -1) {
                                        break;                                          // If user chooses to exit, we break the infinite loop, and user is able to choose statistics or account settings
                                    }
                                    if (receiveUser == 0) {                                          // dialog with new user
                                        String newUser = reader.readLine();         // Enter name of the new user
                                        boolean alreadyMessaged = false;
                                        for (String u : listOfUsers) {
                                            if (u.equals(newUser)) {
                                                alreadyMessaged = true;                 // if you already messaged the user before, it will show this message that you already messaged him before
                                                writer.write(0);
                                                writer.flush();
                                            }
                                        }
                                        boolean flag1 = true;          // This flag is responsible for showing if user exists
                                        boolean flag2 = true;          // This flag is responsible for checking if user to which you are texting blocked you, or you blocked that user before
                                        for (User value : allUsers) {
                                            if (value.getUsername().equals(newUser)) {
                                                flag1 = false;          // flag1 = false; means that user to which you are trying to text indeed exists
                                                if (user.getBlockedUsers().contains(value) || value.getBlockedUsers().contains(user)) {
                                                    writer.write(1);
                                                    writer.flush();
                                                    flag2 = false;      // flag2 = false; means that neither of you blocked each other
                                                }
                                            }
                                        }
                                        if (flag2 && !alreadyMessaged) {     // this code runs if
                                            // user exists, user is Buyer, you didn't block each other
                                            writer.write(2);
                                            writer.flush();
                                            String mes = reader.readLine();               // user enters the message he would
                                            // want to send to new user
                                            ArrayList<Message> temp = user.getMessages();  // creates new ArrayList with user messages
                                            temp.add(new Message(user.getUsername(), newUser, mes));    // adds new message to that ArrayList
                                            user.setMessages(temp);                        // updates the messages field on the user
                                            messageHistory = parseMessageHistory(user, newUser);     // after the messages field was updated, we update the messageHistory and print that out
                                            oos.writeObject(messageHistory);
                                            oos.flush();
                                        }

                                    } else if (receiveUser >= 1 && receiveUser != listOfUsers.length + 1) {
                                        // if user doesn't choose to start new dialog or exit the program receiveUser is
                                        // to view conversations you had before with other users
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

                                            int optionChoice = reader.read();            // enters which
                                            // option you want to do
                                            if (optionChoice == 0) {            // writing new messages
                                                //System.out.println("You want to send a message or upload a txt " +
                                                //      "file?\n[1] Send message\n[2] Upload file");    // you are
                                                // presented with two options as described before
                                                // 1 - regular message          2 - upload a txt file
                                                int fileOrText = reader.read();
                                                if (fileOrText == 0) {       // regular message
                                                    //System.out.println("Enter message: ");
                                                    String mes = reader.readLine();
                                                    ArrayList<Message> temp = user.getMessages();
                                                    temp.add(new Message(user.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                    user.setMessages(temp);        // updates the messages field of the user to the renewed messageHistory
                                                } else if (fileOrText == 1) {      //uploading files
                                                    //System.out.println("Enter name of txt file: ");
                                                    String fileName = reader.readLine();         // enters name of the file
                                                    String mes;
                                                    try {
                                                        ArrayList<String> tempArr = new ArrayList<>();
                                                        BufferedReader bfr = new BufferedReader(new FileReader(fileName));
                                                        String st;
                                                        while ((st = bfr.readLine()) != null) {
                                                            tempArr.add(st);                     // reads whole file and saves lines to ArrayList
                                                        }
                                                        mes = String.join("\\n", tempArr);              // combine all lines in the file by \\n which shows up as \n in the messages.csv file
                                                        // we read it as new line when writing all messages
                                                        ArrayList<Message> temp = user.getMessages();
                                                        temp.add(new Message(user.getUsername(), listOfUsers[receiveUser - 1], mes));
                                                        user.setMessages(temp);                  // updates the messages field of the user
                                                        writer.write(0);
                                                        writer.flush();
                                                        writer.write(mes);
                                                        writer.println();
                                                        writer.flush();

                                                    } catch (
                                                            FileNotFoundException e) {         // if user enters file that does not exist
                                                        writer.write(1);
                                                        writer.flush();
                                                    }
                                                }
                                            } else if (optionChoice == 1) {          // editing messages
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
                                                if (i > 0) {
                                                    choice = reader.read();           // user chooses which
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
                                                    saveMessages(user);
                                                }
                                            } else if (optionChoice == 2) {             // deleting messages
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
                                                if (i > 0) {
                                                    //System.out.println("Choose message to delete");
                                                    choice = reader.read();      // user chooses which
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
                                            } else if (optionChoice == 3) {            // exporting messages
//                                                System.out.println("Enter name of the file to which you want to export your message history");
                                                String fileName = reader.readLine();            // enters the file name
                                                PrintWriter pw = new PrintWriter(new FileOutputStream(fileName, false));
                                                for (Message msg : messageHistory) {
                                                    // this line writes Message object in the same manner as it does in main "messages.csv" file
                                                    String ans = String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", msg.getId(), msg.getTime(), msg.getSender(), msg.getReceiver(), msg.getMessage(), msg.isDelBySender(), msg.isDelByReceiver());
                                                    pw.write(ans);
                                                    pw.println();
                                                    pw.flush();
                                                }
                                            } else {          // if user chooses to exit, we break from infinite loop
                                                break;
                                            }
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                saveMessages(user);
                            }
                        } else if (choice == 1) {        // MESSAGES / STATISTICS / ACCOUNT / EXIT
                            while (true) {
                                int alphabetical = reader.read();
                                if (alphabetical == 0) {
                                    if (user instanceof Buyer) {
                                        String stats = ((Buyer) user).viewStatistics(true);
                                        System.out.println(stats);
                                        writer.write(stats);
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        Map<String, Integer> sentMessages = new HashMap<>();
                                        for (User u : allUsers) { // Iterates through every user
                                            int count;
                                            ArrayList<Message> messages;
                                            if (!u.equals(user) && u instanceof Buyer) {
                                                messages = parseStoreMessages(user, u.getUsername()); // gets all
                                                // messages sent to the current user's store from a user
                                                count = messages.size(); // gets number of messages from the sender
                                                sentMessages.put(u.getUsername(), count); // assigns the users and number of messages they sent to a hashmap
                                            }
                                        }
                                        ArrayList<String> sortedSentMessages = new ArrayList<>(sentMessages.keySet());
                                        Collections.sort(sortedSentMessages); // sorts users
                                        StringBuilder sortMessages = new StringBuilder();
                                        for (String s : sortedSentMessages) {
                                            // writes the user and number of messages they sent alphabetically
                                            sortMessages.append(String.format("%s sent %d messages\\n", s,
                                                    sentMessages.get(s)));
                                        }
                                        writer.write(sortMessages.toString());
                                        writer.println();
                                        writer.flush();
                                    }
                                } else if (alphabetical == 1) {
                                    if (user instanceof Buyer) {
                                        String stats = ((Buyer) user).viewStatistics(false);
                                        System.out.println(stats);
                                        writer.write(stats);
                                        writer.println();
                                        writer.flush();
                                    } else {
                                        Map<String, Integer> sentMessages = new HashMap<>();
                                        for (User u : allUsers) { // Iterates through every user
                                            int count;
                                            ArrayList<Message> messages;
                                            if (!u.equals(user) && u instanceof Buyer) {
                                                messages = parseStoreMessages(user, u.getUsername()); // gets all
                                                // messages sent to the current user's store from a user
                                                count = messages.size(); // gets number of messages from the sender
                                                sentMessages.put(u.getUsername(), count); // assigns the users and number of messages they sent to a hashmap
                                            }
                                        }
                                        ArrayList<String> sortedSentMessages = new ArrayList<>(sentMessages.keySet());
                                        Collections.sort(sortedSentMessages); // sorts users
                                        StringBuilder sortMessages = new StringBuilder();
                                        for (int j = sortedSentMessages.size() - 1; j >= 0; j--) {
                                            // writes the user and number of messages they sent reverse alphabetically
                                            sortMessages.append(String.format("%s sent %d messages\\n",
                                                    sortedSentMessages.get(j), sentMessages.get(sortedSentMessages.get(j))));
                                        }
                                        writer.write(sortMessages.toString());
                                        writer.println();
                                        writer.flush();
                                    }
                                } else if (alphabetical == 2) {
                                    ArrayList<Message> allMessages = new ArrayList<>();
                                    String word = "";
                                    String secondWord = "";
                                    String thirdWord = "";
                                    int count;
                                    int maxCount = 0;
                                    int secondCount = 0;
                                    int thirdCount = 0;
                                    for (User u1 : allUsers) {
                                        if (u1 != user) {
                                            allMessages.addAll(parseMessageHistory(user, u1.getUsername()));
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
                                    // Prints the first, second, and third most commonly used words
                                    String commonWords = "The most common word in Messages is " + word + " " +
                                            "said " + maxCount + " times\\n" + "The second most common word in " +
                                            "Messages is " + secondWord + " said " + secondCount + " times\\n" +
                                            "The third most common word in Messages is " + thirdWord + " said "
                                            + thirdCount + " times";
                                    writer.write(commonWords);
                                    writer.println();
                                    writer.flush();
                                } else {
                                    break;
                                }
                            }
                        } else if (choice == 2) {        // MESSAGES / STATISTICS / ACCOUNT / EXIT
                            while (true) {
                                assert user != null;
                                choice = reader.read();
                                if (choice == 0) {
                                    // user selects edit account
                                    choice = reader.read();
                                    String newAccountInfo;
                                    switch (choice) {
                                        case 0:
                                            // user selects change email
                                            boolean repeat = false;
                                            do {
                                                newAccountInfo = reader.readLine();
                                                // user types new email
                                                for (User u : allUsers) {
                                                    if (u.getEmail().equals(newAccountInfo)) {
                                                        repeat = true;
                                                    }
                                                }
                                                if (newAccountInfo == null) {
                                                    break;
                                                } else if (newAccountInfo.contains("@") && newAccountInfo.contains(".") && !repeat) {
                                                    user.setEmail(newAccountInfo); // if new email is valid changes
                                                    // current user's email to new email
                                                    writeUsers("login.csv", allUsers);
                                                } else if (repeat) {
                                                    newAccountInfo = "";
                                                } else {
                                                    // user inputs an invalid email (does not contain @ and .)
                                                    newAccountInfo = "";
                                                }
                                                writer.write(newAccountInfo);
                                                writer.println();
                                                writer.flush();
                                            } while (newAccountInfo.isEmpty());
                                            break;
                                        case 1:
                                            // user selects change password
                                            newAccountInfo = reader.readLine();
                                            // user types new password
                                            if (newAccountInfo != null) {
                                                // changes user's password to new password
                                                user.setPassword(newAccountInfo);
                                                writeUsers("login.csv", allUsers);
                                            } else {
                                                break;
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                } else if (choice == 1) {
                                    // user selects delete account
                                    // makes sure user wants to delete their account
                                    choice = reader.read();
                                    if (choice == 0) {
                                        // user selects Y, their account is deleted (Their messages to other users will remain)
                                        user.removeUser();
                                        allUsers.remove(user);
                                        choice = 3;
                                        user = null;
                                        writeUsers("login.csv", allUsers);
                                    }
                                } else if (choice == 2) {
                                    // user selects block/unblock users
                                    StringBuilder blockedUsers = new StringBuilder("Blocked Users: \\n");
                                    for (User b : user.getBlockedUsers()) { // shows list of currently blocked users
                                        blockedUsers.append(b.getUsername()).append("\\n");
                                    }
                                    writer.write(blockedUsers.toString());
                                    writer.println();
                                    writer.flush();
                                    // Asks if user wants to block or unblock a user
                                    choice = reader.read();
                                    String[] blockedUsersArr = new String[user.getBlockedUsers().size()];
                                    for (int i = 0; i < user.getBlockedUsers().size(); i++) {
                                        blockedUsersArr[i] = user.getBlockedUsers().get(i).getUsername();
                                    }
                                    switch (choice) {
                                        case 0:
                                            // user selects block user
                                            String blockUsername = reader.readLine();
                                            // user enters username of user to block
                                            if (blockUsername == null) {
                                                break;
                                            } else if (user.blockUser(blockUsername, allUsers)) {
                                                // if that user exist they are blocked
                                                writer.write("yes");
                                                writer.println();
                                                writer.flush();
                                            } else {
                                                // if they don't exist tell user
                                                writer.write("no");
                                                writer.println();
                                                writer.flush();
                                            }
                                            break;
                                        case 1:
                                            // user select unblock user
                                            oos.writeObject(blockedUsersArr);
                                            oos.flush();
                                            if (blockedUsersArr.length > 0) {
                                                String unblockUsername = reader.readLine();
                                                // user enters username of user to unblock
                                                if (unblockUsername == null) {
                                                    break;
                                                } else if (user.unblockUser(unblockUsername, allUsers)) {
                                                    // if that user is currently blocked they are unblocked
                                                    writer.write("yes");
                                                    writer.println();
                                                    writer.flush();
                                                } else {
                                                    // if they aren't blocked tell user
                                                    writer.write("no");
                                                    writer.println();
                                                    writer.flush();
                                                }
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                    writeUsers("login.csv", allUsers); // writes changes to login.csv file
                                } else if (choice == 3 && user instanceof Seller) {
                                    // if user is seller allow user to create store
                                    StringBuilder userStores = new StringBuilder("Your Stores: \\n");
                                    for (String storeName : ((Seller) user).getStores()) {
                                        // shows list of current stores by current user
                                        userStores.append(storeName).append("\\n");
                                    }
                                    writer.write(userStores.toString());
                                    writer.println();
                                    writer.flush();
                                    String storeName = reader.readLine();
                                    ((Seller) user).createStore(storeName); // adds new store
                                    allStores.add(new Store(storeName, 0));
                                    writeStores("stores.csv", allStores); // updates stores.csv
                                    writeUsers("login.csv", allUsers); // updates login.csv
                                    break;
                                } else {
                                    break;
                                }
                            }
                        } else {        // MESSAGES / STATISTICS / ACCOUNT / EXIT
                            user = null;
                            break;
                        }
                    }
                }
            }
            catch (Exception e) {
                int imHereToAvoidCheckStyle;
                e.printStackTrace();
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
                            if (u instanceof Seller) {
                                if (((Seller) u).getStores().size() > 0) {
                                    pw.print(blockedUsernames.get(i) + "\"");
                                } else {
                                    pw.print(blockedUsernames.get(i) + "\",");
                                }
                            } else {
                                pw.print(blockedUsernames.get(i) + "\"");
                            }
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
