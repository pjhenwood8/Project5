# Project 5
Project 5 for CS 180 
To run the project code, first run Server.java. Once the sever is running users can 
connect by running Menu.java. Users will be prompted with an input dialog for the port number.
The Users will input 4242 to connect to the server and use the messaging system.
After connecting to server, the follow-ups and tips will be shown to user, 
which will guide him on how to use the program.

PJ Henwood was responsible for submitting the report on the project on BrightSpace

Alimzhan Sultanov was responsible for submitting the code

Menu.java is the client class of the project. This class contains the GUI that the user will interact with 
in order to progress through the menus. At the beginning, the user will be able to log in with existing account, create
a new account, or exit. Once logged in, the user will be able to write messages, edit them, and delete them.
Furthermore, users will also be able to view statistics of those messages, change the password and email of 
the account, delete their account, and block users from messaging them.

Server.java is the server for the project. This class contains the methods that read and write to the csv files 
and all the data for this program that will be sent to the client when needed.

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
