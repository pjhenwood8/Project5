Test 0: Connect to Server

Steps:
<br> Run Program
<br> User enters the correct server number

Expected Result: User is connected to server and prompter to log in

Test Status: Passed


Test 1: Create Account and Log In

Steps:
<br> User enters correct server number
<br> User selects Create Account
<br> User enters an invalid email
<br> User is prompted to try again
<br> User enters a valid email
<br> User enters an invalid username
<br> User is prompted to try again
<br>User enters a valid username
<br>User enters an invalid password
<br>User is prompted to try again
<br>User enters a valid password
<br>User selects Buyer
<br>User selects OK
<br>User selects Exit
<br>User selects OK
<br>User selects Exit
<br>User selects OK

Expected Result: User has access to main program before exiting and login.csv is updated

Test Status: Passed

Test 2: Log In

Steps:
<br>User enters correct server number
<br>User selects Log In
<br>User enters an existing email
<br>User enters a matching password
<br>User selects OK
<br>User selects Exit
<br>User selects OK
<br>User selects Exit
<br>User selects OK


Expected Result: User has access to main program before exiting and no csv file changes

Test Status: Passed

*All tests below will assume the user successfully connected to the server,
successfully exits the program, and successfully logged in as the following user*
<br> Username: User
<br> Email: user@purdue.edu
<br> Password: pass
<br> Type: Buyer


Test 3: Edit Account Change Password

Steps:
<br> Select Account
<br> Select Change Password
<br> Enter fireball
<br> Select OK
<br> Select Exit


Expected Result: The new password should be fireball in login.csv
and displayed on the edit account home screen.

Test Status: Passed

Test 4: Edit Account Change Email

Steps:
<br> Select Account
<br> Select Change Email
<br> Enter fakeEmail
<br> Select OK
<br> Enter user@gmail.com
<br> Select OK
<br> Select Exit


Expected Result: The first email entered "fakeEmail" should be 
rejected as it is invalid and does not contain an @ sign. Then
after the new valid email "user@gmail.com" is entered it should be
updated in login.csv and displayed on the edit account home screen.

Test Status: Passed

Test 5: Delete Account

Steps:
<br> Select Account
<br> Select Delete Account
<br> Select No
<br> Select Delete Account
<br> Select Yes

Expected Result: Should be immediately logged out and the account
should no longer appear in login.csv

Test Status: Passed

*The next two tests should be done in sequence and require a second
account in login.csv with the following details
<br> Username: Friend
<br> Email: friend@purdue.edu
<br> Password: pass
<br> Type: Buyer*


Test 6: Block User

Steps:
<br> Select Account
<br> Select Block/Unblock User
<br> Select Block New User
<br> Select Friend from the dropdown menu
<br> Select OK
<br> Select Exit

Expected Result: The blocked user should appear on the screen if 
you select block/unblock users, you should no longer be able write
messages to that user, and login.csv should now have the blocked user's 
username in the fifth set of quotations of the logged in account.

Test Status: Passed

Test 7:Unblock User

Steps:
<br> Select Account
<br> Select Block/Unblock User
<br> Select Unblock User
<br> Select Friend from the dropdown menu
<br> Select OK
<br> Select Unblock User
<br> Select OK
<br> Select Exit

Expected Result: The formerly blocked user should no longer appear
on the block/unblock users screen, you should be able to message that
user and they should no longer appear in the fifth set of quotations
in login.csv. Furthermore, The second time you select unblock user it
should error as there are no other users to block and redirect to the 
menu.

Test Status: Passed

*The next seven tests should be run in sequence and 
require the following two accounts*
<br> Account 1
<br> Email: user@purdue.edu
<br> Username:  User
<br> Password:  pass
<br> User Type: Buyer
<br>
<br> Account 2
<br> Email: friend@gmail.com
<br> Username: Friend
<br> Password: pass
<br> User Type: Seller

Test 8: Write message

Steps:
<br> Select Messages
<br> Select Write to Seller
<br> Select Start New Dialog
<br> Select Friend from the dropdown menu
<br> Write "Hello Friend" and select OK
<br> Select Exit

Expected Result: messages.csv was updated to include the Hello Friend
message = and Friend should now be able to view that message as we
will check in the next test

Test Status: Passed

*The next test requires you to sign in as Friend*

Test 9: Create Store and Message Received 

Steps:
<br> Log in as Friend
<br> Select Account
<br> Select Create New Store
<br> Write "Shoe Store" and select OK
<br> Select OK
<br> Select Account
<br> Select Create New Store
<br> Write "Nike" and select OK
<br> Select OK
<br> Select Messages
<br> Select User
<br> Select Exit

Expected Result: Should be able to see the message user sent and
when they send it, and stores.csv should now contain shoe store

Test Status: Passed

Test 10: Write Store

Steps:
<br> Select Messages
<br> Select Write to Store
<br> Select Shoe Store
<br> Write "Friend I would like a shoe" and select OK
<br> Select OK
<br> Select Exit

Expected Result: Messages.csv should be updated with the new message
to Friend through the write Store Function

Test Status: Passed

Test 11: Most Common Words

Steps:
<br> Select Messages
<br> Select Write to Seller
<br> Select Friend
<br> Select Write Message
<br> Select Send Message
<br> Write "Goodbye shoe Friend"
<br> Select Exit
<br> Select Exit
<br> Select Statistics
<br> Select Most Common Words
<br> Select Exit

Expected Result: The Most common word should be Friend said three 
times, the next most common word should be "shoe" said twice, and
then the third most common word should "Hello" said once.

Test Status: Passed

Test 12: Buyer Alphabetical

Steps:
<br> Select Statistics
<br> Select Alphabetical
<br> Select Exit

Expected result: When alphabetical is clicked on the following 
list should appear
Store: Nike - Number of messages received: 0
Store: shoe store - Number of messages received: 1

Test Status: Passed

Test 13: Buyer Reverse Alphabetical

Steps:
<br> Select Statistics
<br> Select Reverse Alphabetical
<br> Select Exit

Expected result: When reverse alphabetical is clicked on the 
following list should appear
Store: shoe store - Number of messages received: 1
Store: Nike - Number of messages received: 0

Test Status: Passed

*The next test requires you to create a new account and then log in
as Friend*

Test 14: Seller Alphabetical

Steps:
<br> Create a new Buyer called User2 with email user2@purdue.edu
<br> Select Messages
<br> Select Write to Seller
<br> Write "Hi I am User 2" and Select OK
<br> Exit and Log out
<br> Log in as Friend
<br> Select Statistics
<br> Select Alphabetical
<br> Select Exit

Expected result: When alphabetical is clicked on the following 
list should appear
User sent 3 messages
User2 sent 1 messages

Test Status: Passed

*The next test requires you to be logged in as Friend*

Test 15: Seller Reverse Alphabetical

Steps:
<br> Select Statistics
<br> Select Reverse Alphabetical
<br> Select Exit

Expected result: When reverse alphabetical is clicked on the 
following list should appear
User2 sent 1 messages
User sent 3 messages

Test Status: Passed

Test 16: Delete Messages

Steps:
<br> Select Messages
<br> Select Write to Seller
<br> Select Friend
<br> Select Delete Message
<br> Select 1
<br> Select Exit

Expected Result: After selecting one you will return to the page 
which logs your conversation with Friend and Message one "Hello Friend"
should be deleted, and message.csv should also reflect this change

Test Status: Passed

Test 17: Edit Messages

Steps:
<br> Select Messages
<br> Select Write to Seller
<br> Select Friend
<br> Select Edit Message
<br> Select 2, which should correspond too "Goodbye shoe Friend"
<br> Write "Goodbye Friend" and select OK
<br> Select Exit

Expected Result: After selecting OK you will return to the page
which logs your conversation with Friend and Message two should
now be "Goodbye Friend", and message.csv should also reflect this
change

Test Status: Passed

Test 18: Export History to CSV

Steps:
<br> Select Messages
<br> Select Write to Seller
<br> Select Friend
<br> Select Export History to CSV
<br> Write "test.csv" and Select OK
<br> Select Exit

Expected Result: All messages should now be saved in the file 
test.csv

Test Status: Passed
