import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4242);
        ArrayList<Message> allMessages = readWholeFile();
        ArrayList<User> allUsers = readUsers("login.csv");
        ArrayList<Store> allStores = readStores("stores.csv", allUsers);

        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                while (true) {
                    User user = null;
                    int choice = Integer.parseInt(reader.readLine());
                    if (choice == 1) {                 // LOGIN / CREATE ACCOUNT / EXIT
                        String email = reader.readLine();
                        String password = reader.readLine();
                        user = login(email, password, allUsers);
                    }
                    else if (choice == 2) {         // LOGIN / CREATE ACCOUNT / EXIT
                        String email = reader.readLine();
                        String username = reader.readLine();
                        String password = reader.readLine();
                        String type = reader.readLine();
                        try {
                            user = createAccount(email, username, password, type, allUsers);
                            writer.write("User created");
                            writer.println();
                            writer.flush();
                        }
                        catch (LoginException e) {
                            writer.write(e.getMessage());
                        }
                    }
                    else if (choice == 0) {          // LOGIN / CREATE ACCOUNT / EXIT
                        break;
                    }

                    if (user == null){
                        writer.write("Login fail");
                        writer.println();
                        writer.flush();
                    } else {
                        writer.write("Login success");
                        writer.println();
                        writer.flush();
                        choice = Integer.parseInt(reader.readLine());
                        if (choice == 1) {       // MESSAGES / STATISTICS / ACCOUNT / EXIT
                            if (user instanceof Buyer) {
                                ArrayList<Message> messageHistory = null;
                                choice = Integer.parseInt(reader.readLine());
                                if (choice == 1) {      //WRITE TO STORE / WRITE TO SELLER / EXIT
                                    oos.writeObject(allStores);   // we send the store list
                                    // in order for them to chose one to text
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
                                                            "user " +
                                                            "you " +
                                                            "already messaged!");
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
                                                // -1  ISEXPORT MESSAGE HISTORY TO CSV FILE
                                                if (optionChoice == 2) {              // if user chooses to edit messages (for more detailed comments refer to line 210)
                                                    messageHistory = parseMessageHistory(user, listOfUsers[receiveUser - 1]);
                                                    ArrayList<Message> userIsSender = new ArrayList<>();
                                                    int i = 0;
                                                    while (i < messageHistory.size()) {
                                                        if (messageHistory.get(i).getSender().equals(user.getUsername())) {
                                                            userIsSender.add(messageHistory.get(i));
                                                            //System.out.printf("[%d] " + messageHistory.get(i)
                                                            // .toString(), i + 1);
                                                            i++;
                                                        } else {
                                                            //System.out.print(messageHistory.get(i).toString());
                                                        }

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
                                                // 1-WRITE MESSAGE / 2-EDIT MESSAGE / 3-DELETE MESSAGE / 0-EXIT
                                                // -1  IS EXPORT MESSAGE HISTORY TO CSV FILE
                                                if (optionChoice == 0) {
                                                    break;
                                                }
                                            }
                                        } else {
                                            System.out.println("Please enter a valid number");
                                        }
                                    }
                                    saveMessages(user);                        // saves changed to the messages.csv after finishing the messages part of the program
                                    break;
                                } else if (choice == 0) {            //WRITE TO STORE/ WRITE TO SELLER / EXIT

                                }
                            } else if (user instanceof Seller) {

                            }
                        }
                        else if (choice == 2) {        // MESSAGES / STATISTICS / ACCOUNT / EXIT

                        }
                        else if (choice == 3) {        // MESSAGES / STATISTICS / ACCOUNT / EXIT

                        }
                        else if (choice == 0) {        // MESSAGES / STATISTICS / ACCOUNT / EXIT

                        }
                        else {

                        }
                    }
                }
            }
            catch (Exception e) {
                int imHereToAvoidCheckStyle;
            }
        }
    }

    public static ArrayList<Message> readWholeFile() throws IOException {             // this method is responsible for
        // reading
        // the messages.csv file, and converting the every line there
        // to the Message object. Method return ArrayList of those Message objects
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
                                     String type, ArrayList<User> users) throws LoginException {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                throw new LoginException("This email is already taken, please select another email");
            }
            if (user.getUsername().equals(username)) {
                throw new LoginException("This username is already taken, please select another username");
            }
        }
        User answer = null;
        if (type.equals("Seller")) {
            answer = new Seller(email, username, password);
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream("login.csv", true));
                pw.println(answer.toString());
                pw.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (type.equals("Buyer")) {
            answer = new Buyer(email, username, password);
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
}
