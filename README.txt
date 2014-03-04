Lauren Zou (ljz2112)
Computer Networks
Programming 1

a. A brief description of your code
-----------------------------------
The Simple Chat Server consists of two parts: the Server and the Client.

The Server (Server.java) depends on several other classes:
- ServerThread.java
- User.java
- Message.java
- Utilities.java

The Client (Client.java) depends on Utilities.java. Note that both Server.java and Client.java use Utilities.java. 

Utilities.java
--------------
Utilities.java is a sort of protocol between the Server and the Client. It reduces the need for redudant code such as error() and parsePortNumber() as well as defines some strings (EXIT, FORCE_EXIT, and NEWLINE) that are crucial in the communication between the Server and the Client. Utilities.java also faciliates the encoding and decoding of messages between the Server and the Client.

Client.java
-----------
Client.java is the Client. It connects to the port and ip address associated with the Server and communicates with the Server. The Client uses Javax Swing GUI to optimize user experience. One can close the client either by typing in "logout" as a command (in which case it is a regular EXIT, and the connection will close after a second) or by clicking on the close button of the JFrame (in which case it is a FORCE_EXIT and the connection will close immediately).

Server.java
-----------
Server.java is the Server. It binds a port number at the localhost and listens for Clients to accept. The Server is the main provider of information to the Client. All the important information such as the list of ServerThreads and the database of Users are located inside Server.java.

ServerThread.java
-----------------
ServerThread.java is a thread that handles the communication with a single Client. ServerThread refers back to the Server for information on how to respond to the client, but ultimately handles the input/output communication between the Server and the Client.

User.java
---------
User.java is a data structure for the users in the program. User.java keeps track of information on each user such as username, password, blocked users, last logged in, etc.

Message.java
------------
Message.java is a data structure for messages that are sent between users. Message contains information on who the message was from, what the message is, and who should receive the message.


b. Details on development environment
-------------------------------------
I chose to develop this project in Java. The Client has a GUI that uses the Javax Swing library.


c. Instructions on how to run your code
---------------------------------------
There is a Makefile that will compile the code.

make - compiles both Server.java and Client.java
make server - compiles Server.java
make client - compiles Client.java
make clean - removes all the *.class files

To run the Server: java Server <port number>
To run the Client: java Client <ip address> <port number>


d. Sample commands to invoke your code
--------------------------------------
To invoke the Server:
java Server 4119

To invoke a Client:
java Client localhost 4119


e. Description of an additional functionalities and how they should be executed/tested.
---------------------------------------------------------------------------------------

Javax Swing GUI
---------------
The Client is presented as a Javax Swing GUI.

Broadcast Block
---------------
Users who have blocked the user broadcasting will not receive the blocked user's broadcast messages.

Emoticons
---------
Emoticon images will appear instead of text when one of the following strings are printed:
:D     XD     :)     ;)     -.-     >.<     o.o     :(

To see an emoticon coming from the server, use the command "whoelse" when you are the only use online.
Emoticons can also be sent in messages to other users: message Google Hello there! :)
Emoticons can be broadcasted: broadcast XD

Emoticon pack from: http://smgbas.deviantart.com/art/Emix-1-emoticons-pack-162285666