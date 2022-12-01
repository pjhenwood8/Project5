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

        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                boolean repeat = true;
                while (repeat) {
                    int choice = Integer.parseInt(reader.readLine());
                    if (choice == 1) {
                        String email = reader.readLine();
                        String password = reader.readLine();
                        login(email,password);
                    }
                    else if (choice == 2) {

                    }
                    else if (choice == 3) {
                        repeat = false;
                        break;
                    }
                }
            }
            catch (Exception e) {
                int imHereToAvoidCheckStyle;
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

    public static boolean login(String email, String password) {
        ArrayList<ArrayList<String>> userList = new ArrayList<>();
        try {
            BufferedReader bfr = new BufferedReader(new FileReader("login.csv"));
            String line = bfr.readLine();
            while (line != null) {
                userList.add(customSplitSpecific(line));
                line = bfr.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ArrayList<String> strings : userList) {
            if (strings.get(1).equals(email)) {
                if (strings.get(2).equals(password))
                    return true;
            }
        }
        return false;
    }
}
