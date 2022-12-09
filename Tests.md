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

Test 8:
<br>

Steps:

Expected Result:

Test Status: Passed

Test 9:

Steps:

Expected Result:

Test Status: Passed

Test 10:

Steps:

Expected Result:

Test Status: Passed

Test #:

Steps:

Expected Result:

Test Status: Passed
