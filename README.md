# Chat-server

...need to add the coursework specifications.


This README file explains the basic functionalities and usages for
the "IrcServerMain.java" file, version 1.0, which has been submitted as
required for Practical - 3, of CS5001 course.

The program does all the basic functionalities as specified by the 
coursework. The program works in accordance with the principle of least surprise.

Additional features implemented :

---- COMPILE AND RUN ON THE COMMAND LINE ----

The usage for this program:

        java IrcServerMain <serverName> <port>

1) If the port number goes out of bounds required, or is a reserved number:
    
        Port value out of range: <port>
 
            or
 
        Invalid port number: <port>
        
   is shown accordingly

2) Invalid command addition:
    
    toReplyInvalidMsg() method is implemented that writes to the client if
    the command the client entered was not available in the command cases.
    
    message:
             :localhost 400 * :Invalid command
    
3) The program stores nickName linking it with the username and the realname
   (eventhough its not specified), as chat apps usually keep the details of the
   users until the user quits.
   
4) Each nick name(of the client) is unique. If a client tries to enter a nick name that
   already exists an error is thrown. (The nickname entered is only compared with the 
   other clients that are registered).
   
   message:
            :localhost 400 * :Nickname exists
   
5) Eventhough once a user is registered they cannot change the username and the realname,
   the program allows the user to change the nickname, keeping the username and the realname.
   
6) User duplication for a given channel is taken care of. Whenever a user tries to join a 
   channel on which he already exists, he is given a message, and not allowed to join again.
   
   message:
           :localhost 400 * :You already exist in this channel
   
7) An invalid argument message for JOIN command is implemented.

   message:
           :localhost 400 * :Invalid arguments to JOIN command

8) An invalid argument message for PART command is implemented.

   message:
           :localhost 400 * :Invalid arguments to PART command
