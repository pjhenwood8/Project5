# Project 5
Project 5 for CS 180  
To run the project code, first run Server.java. Once the server is running users can 
connect by running Menu.java. Users will be prompted with an input dialog for the port number.
The Users should input port number 4242 to connect to the server and use the messaging system.
After connecting to server, the follow-ups and tips will be shown to user, 
which will guide him on how to use the program.

PJ Henwood was responsible for submitting the report on the project on BrightSpace

Alimzhan Sultanov was responsible for submitting the code

Menu.java is the client class of the project. This class contains the GUI that the user will interact with 
in order to progress through the menus. At the beginning, the user will be able to log in with existing account, create
a new account, or exit. Once logged in, the user will be able to write messages, edit them, and delete them.
Furthermore, users will also be able to view statistics of those messages, change the password and email of 
the account, delete their account, and block users from messaging them. In short, Menu.java is used as an
interactive shell for the project that will collect information from the client and send it to the server.

We have 2 classes in Server.java file which are Server class and MultiClient class.

MultiClient class is the server for the project. This class contains the methods that read and write to the csv files 
and all the data for this program that will be sent to the client when needed. All calculations are basically 
done in the MultiClient class. Important thing about MultiClient is that it implements Runnable and have run() method
in it, and it's created everytime new user connects to the socket.

Server class contains the main method and is responsible for checking if user connected to the serverSocket and 
starting multiple Threads in case users are connecting to the serverSocket. Starting multiple Threads allows us to 
handle multiple clients at the same time and also updating stuff in real-time. It makes our project concurrent.

Message.java is file for managing messages from the messages.csv file. Message.java allows us to convert
each line in the messages.csv into the Message object with specific field, using methods in Message.java.

RunLocalTest.java is the file for testing functionality of the code. It runs test on every method besides main
and checks if their output matches the expected output.

User.java is the main class for creating users in our program. The most important field in that class
is ArrayList<Message> messages field, which saves all messages that are available to the user. After
each time user logs off from the program, these field is read and being saves to messages.csv.

Buyer.java is a subclass of User class, which has new method called viewStatistics, which allows user 
to view most used words, and most messaged stores.

Seller.java is a subclass of User class, which has additional field that saves all stores that are 
owned by the seller. Similar to Buyer.java it has a viewStatistics method, which allows them to see which 
store is getting the highest amount of messages.

Store.java is a class that helps to keep track of statistics, specifically amount of messages that 
store receives. With that class it's much easier to construct statistics for the Seller class.

LoginException.java is thrown whenever there is an issue when creating new account. It's thrown if email or
username is already in use. 
