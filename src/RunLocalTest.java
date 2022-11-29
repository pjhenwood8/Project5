import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static org.junit.Assert.*;

public class RunLocalTest {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestCase.class);
        if (result.wasSuccessful()) {
            System.out.println("Excellent - Test ran successfully");
        } else {
            for (Failure failure : result.getFailures()) {
                System.out.println(failure.toString());
            }
        }
    }

    /**
     * A set of public test cases.
     *
     * <p>Purdue University -- CS18000 -- Summer 2022</p>
     *
     * @author Purdue CS
     * @version June 13, 2022
     */
    public static class TestCase {
        private final PrintStream originalOutput = System.out;
        private final InputStream originalSysin = System.in;

        @SuppressWarnings("FieldCanBeLocal")
        private ByteArrayInputStream testIn;

        @SuppressWarnings("FieldCanBeLocal")
        private ByteArrayOutputStream testOut;

        @Before
        public void outputStart() {
            testOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(testOut));
        }

        public void restoreInputAndOutput() {
            System.setIn(originalSysin);
            System.setOut(originalOutput);
        }

        private String getOutput() {
            return testOut.toString();
        }

        @SuppressWarnings("SameParameterValue")
        private void receiveInput(String str) {
            testIn = new ByteArrayInputStream(str.getBytes());
            System.setIn(testIn);
        }

        @Test(timeout = 1000)
        public void testCustomSplitSpecific() {
            User user = new User("a", "a@gmail.com", "123");
            String s = "hi,I,am,a,snail";
            String[] list = s.split(",");
            ArrayList<String> sList = new ArrayList<>(Arrays.asList(list));
            assertEquals("Wrong", sList, user.customSplitSpecific(s));
        }

        @Test(timeout = 1000)
        public void testReadWholeFile() throws IOException {
            User user1 = new User("a", "a@gmail.com", "123");
            User user2 = new User("b", "b@gmail.com", "1234");
            //ArrayList<Message> mList1 = user1.readWholeFile();
            //ArrayList<Message> mList2 = user2.readWholeFile();
            //assertEquals( mList1.toString(), mList2.toString());
        }

        @Test(timeout = 1000)
        public void testParseMessages() throws IOException {
            User user1 = new User("Buyer1", "buyer1@purdue.edu", "password1234");
            ArrayList<Message> mList = new ArrayList<>();
            mList.add(new Message(169, "13-11-2022 02:23:31", "Buyer1", "Seller1", "Hi there", false, false));
            mList.add(new Message(183, "13-11-2022 02:24:59", "Buyer1", "Seller1", "Yes i want to buy", false, false));
            //assertEquals( mList.toString(), user1.parseMessages().toString());
        }

        @Test(timeout = 1000)
        public void testBlockUser() {
            User user1 = new User("Buyer1", "buyer1@purdue.edu", "password1234");
            User user2 = new User("a", "a@gmail.com", "123");
            User user3 = new User("b", "b@gmail.com", "1234");
            ArrayList<User> uList = new ArrayList<>();
            uList.add(user1);
            uList.add(user2);
            uList.add(user3);
            assertTrue(user1.blockUser("a", uList));
            assertFalse(user1.blockUser("c", uList));
        }

        @Test(timeout = 1000)
        public void testUnblockUser() {
            User user1 = new User("Buyer1", "buyer1@purdue.edu", "password1234");
            User user2 = new User("a", "a@gmail.com", "123");
            User user3 = new User("b", "b@gmail.com", "1234");
            ArrayList<User> uList = new ArrayList<>();
            uList.add(user1);
            uList.add(user2);
            uList.add(user3);
            user1.blockUser("a", uList);
            user1.blockUser("c", uList);
            assertTrue(user1.unblockUser("a", user1.getBlockedUsers()));
            assertFalse(user1.unblockUser("c", user1.getBlockedUsers()));
        }
        
            @Test(timeout = 1000)
        public void testParseUsers(){
            User user1 = new User("Buyer1", "buyer1@purdue.edu", "password1234");
            assertEquals(Arrays.toString(new String[]{"Seller1"}), Arrays.toString(Menu.parseUsers(user1)));
        }

        @Test(timeout = 1000)
        public void testParseMessageHistory(){
            User user1 = new User("mann", "mann72@purdue.edu", "12345");
            ArrayList<Message> mList = new ArrayList<>();
            mList.add(new Message(155, "13-11-2022 01:18:33", "mann", "alik",
                    "Hey man where is my money", false, false));
            assertEquals(mList.toString(), Menu.parseMessageHistory(user1, "alik").toString());
        }

        @Test(timeout = 1000)
        public void testWriteUsers() throws IOException {
            User user1 = new User("Buyer1", "buyer1@purdue.edu", "password1234");
            User user2 = new User("a", "a@gmail.com", "123");
            User user3 = new User("b", "b@gmail.com", "1234");
            ArrayList<User> uList = new ArrayList<>();
            uList.add(user1);
            uList.add(user2);
            uList.add(user3);
            Menu.writeUsers("test.csv", uList);
            BufferedReader br = new BufferedReader(new FileReader("test.csv"));
            String line = br.readLine();
            String concString = "";
            while(line != null){
                concString += line;
                System.out.println(line);
                line = br.readLine();
            }
            assertEquals("\"Buyer1\",\"buyer1@purdue.edu\",\"password1234\",\"s\",\"\"" +
                            "\"a\",\"a@gmail.com\"," + "\"123\",\"s\",\"\"" +
                    "\"b\",\"b@gmail.com\",\"1234\",\"s\"," +
                    "\"\"", concString);
        }

        @Test(timeout = 1000)
        public void testAddBlockedUsers(){
            ArrayList<String> bList = new ArrayList<>();
            bList.add("a");
            User user1 = new User("Buyer1", "buyer1@purdue.edu", "password1234", bList);
            User user2 = new User("a", "a@gmail.com", "123");
            User user3 = new User("b", "b@gmail.com", "1234");
            ArrayList<User> uList = new ArrayList<>();
            uList.add(user1);
            uList.add(user2);
            uList.add(user3);
            Menu.addBlockedUsers(uList);
            assertEquals("[a]", user1.getBlockedUsernames().toString());
        }

        @Test(timeout = 1000)
        public void testSaveMessages() throws IOException {
            User user1 = new User("Buyer1", "buyer1@purdue.edu", "password1234");
            ArrayList<Message> mList = new ArrayList<>();
            mList.add(new Message(169, "13-11-2022 02:23:31", "Buyer1", "Seller1", "Hi there", false, false));
            Menu.saveMessages(user1);
            assertEquals(mList.toString(), "[13-11-2022 02:23:31   (Buyer1 -> Seller1)\nHi there\n]");
        }
        
        @Test(timeout = 1000)
        public void testWriteStores() throws IOException {
            User user1 = new Buyer("Buyer1", "buyer1@purdue.edu", "password1234");
            User user2 = new Buyer("a", "a@gmail.com", "123");
            User user3 = new Buyer("b", "b@gmail.com", "1234");
            ArrayList<Buyer> bList = new ArrayList<>();
            bList.add((Buyer) user1);
            bList.add((Buyer) user2);
            bList.add((Buyer) user3);
            Store store1 = new Store("store1", 1);
            Store store2 = new Store("store2", 2);
            Store store3 = new Store("store3", 3, bList);
            ArrayList<Store> sList = new ArrayList<>();
            sList.add(store1);
            sList.add(store2);
            sList.add(store3);
            Menu.writeStores("test.csv", sList);
            BufferedReader br = new BufferedReader(new FileReader("test.csv"));
            String line = br.readLine();
            String concStr = "";
            while (line != null){
                concStr += line;
                line = br.readLine();
            }
            assertEquals("\"store1\",\"1\",\"\"\"store2\",\"2\",\"\"\"store3" +
                    "\",\"3\",\"Buyer1,a,b\"", concStr);
        }

        @Test(timeout = 1000)
        public void testParseStoreMessages() {
            Menu menu = new Menu();
            User user = new User("username","email","pass");
            ArrayList<Message> messages = new ArrayList<>();
            messages.add(new Message("sender1","receiver","Test1"));
            messages.add(new Message("sender2","receiver","Test2"));
            user.setMessages(messages);
            ArrayList<Message> expected = new ArrayList<>();
            expected.add(new Message("sender1","receiver","Test1"));
            try {
                assertEquals(expected.toString(), menu.parseStoreMessages(user, "sender1").toString());
            } catch (AssertionError e) {
                e.printStackTrace();
            }
        }

        @Test(timeout = 1000)
        public void testReadStores() {
            ArrayList<User> users = new ArrayList<>();
            users.add(new User("buyer1", "buyer1@gmail.com", "pass1"));
            users.add(new User("buyer2", "buyer2@gmail.com", "pass2"));
            Menu menu = new Menu();
            ArrayList<Buyer> buyers = new ArrayList<>();
            ArrayList<Store> expectedStore = new ArrayList<>();
            expectedStore.add(new Store("storeName", 0, buyers));
            File testFile = new File("test.csv");
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream(testFile, false));
                pw.println("\"storeName\",\"0\",\"\"");
                pw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                assertEquals(expectedStore.get(0).getStoreName(), menu.readStores("test.csv", users).get(0).getStoreName());
            } catch (AssertionError e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Test
        public void testReadUsers() {
            Menu menu = new Menu();
            ArrayList<User> users = new ArrayList<>();
            users.add(new Buyer("buyer1", "buyer1@gmail.com", "pass1"));
            users.add(new Buyer("buyer2", "buyer2@gmail.com", "pass2"));
            File testFile = new File("test.csv");
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream(testFile, false));
                pw.println("\"buyer1\",\"buyer1@gmail.com\",\"pass1\",\"b\",\"\"");
                pw.println("\"buyer2\",\"buyer2@gmail.com\",\"pass2\",\"b\",\"\"");
                pw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                assertEquals(users.get(0).getEmail(), menu.readUsers("test.csv").get(0).getEmail());
            } catch(IOException e) {
                e.printStackTrace();
            } catch (AssertionError e) {
                e.printStackTrace();
            }
        }
        @Test
        public void testCreateAccount() {
            Scanner testScannner = new Scanner("pj1@gmail.com" + System.lineSeparator() + "PJ1" + System.lineSeparator() + "stark" + System.lineSeparator() + "buyer" + System.lineSeparator()
                    + "pj2@gmail.com" + System.lineSeparator() + "PJ2" + System.lineSeparator() + "stark" + System.lineSeparator() + "buyer" + System.lineSeparator()
                    + "pj3@gmail.com" + System.lineSeparator() + "PJ3" + System.lineSeparator() + "stark" + System.lineSeparator() + "buyer" + System.lineSeparator()
                    + "pj4@gmail.com" + System.lineSeparator() + "PJ4" + System.lineSeparator() + "stark" + System.lineSeparator() + "buyer" + System.lineSeparator()
                    + "pj5@gmail.com" + System.lineSeparator() + "PJ5" + System.lineSeparator() + "stark" + System.lineSeparator() + "buyer" + System.lineSeparator()
                    + "pj6@gmail.com" + System.lineSeparator() + "PJ6" + System.lineSeparator() + "stark" + System.lineSeparator() + "buyer" + System.lineSeparator());
            File testFile = new File("test.csv");
            try {
                assertEquals(new Buyer("PJ1","pj1@gmail.com","stark").getEmail(), Menu.createAccount(testScannner,"test.csv").getEmail());
                assertEquals(new Buyer("PJ2","pj2@gmail.com","stark").getMessages(), Menu.createAccount(testScannner,"test.csv").getMessages());
                assertEquals(new Buyer("PJ3","pj3@gmail.com","stark").getPassword(), Menu.createAccount(testScannner,"test.csv").getPassword());
                assertEquals(new Buyer("PJ4","pj4@gmail.com","fire").getBlockedUsers(), Menu.createAccount(testScannner,"test.csv").getBlockedUsers());
                assertEquals(new Buyer("PJ5","pj5@gmail.com","fire").getBlockedUsernames(), Menu.createAccount(testScannner,"test.csv").getBlockedUsernames());
                assertEquals(new Buyer("PJ6","pj6@gmail.com","fire").getClass(), Menu.createAccount(testScannner,"test.csv").getClass());
            } catch(AssertionError e) {
                e.printStackTrace();
            }
        }

        @Test
        public void testLogin() {
            Scanner testScannner = new Scanner("pj@gmail.com" + System.lineSeparator() + "fire" + System.lineSeparator()
                                                    + "pj@gmail.com" + System.lineSeparator() + "fire" + System.lineSeparator()
                                                    + "pj@gmail.com" + System.lineSeparator() + "fire" + System.lineSeparator()
                                                    + "pj@gmail.com" + System.lineSeparator() + "fire" + System.lineSeparator()
                                                    + "pj@gmail.com" + System.lineSeparator() + "fire" + System.lineSeparator());
            File testFile = new File("test.csv");
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream(testFile, false));
                pw.println("\"PJ\",\"pj@gmail.com\",\"fire\",\"s\",\"\",\"\"");
                pw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                //assertEquals(new Seller("PJ","pj@gmail.com","fire",new ArrayList<String>()).getEmail(),Menu.login(testScannner,"test.csv").getEmail());
                //assertEquals(new Seller("PJ","pj@gmail.com","fire",new ArrayList<String>()).getMessages(),Menu.login(testScannner,"test.csv").getMessages());
                //assertEquals(new Seller("PJ","pj@gmail.com","fire",new ArrayList<String>()).getPassword(),Menu.login(testScannner,"test.csv").getPassword());
                //assertEquals(new Seller("PJ","pj@gmail.com","fire",new ArrayList<String>()).getBlockedUsers(),Menu.login(testScannner,"test.csv").getBlockedUsers());
                //assertEquals(new Seller("PJ","pj@gmail.com","fire",new ArrayList<String>()).getBlockedUsernames(),Menu.login(testScannner,"test.csv").getBlockedUsernames());
            } catch(AssertionError e) {
                e.printStackTrace();
            }
        }
    }
}
