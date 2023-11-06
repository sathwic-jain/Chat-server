# Chat Server

This repository contains the implementation of a Java-based chat server designed to follow a simplified version of the IRC protocol. This project was developed as part of Practical 3 for the CS5001 course - Object-Oriented Modelling, Design, and Programming at the University of St Andrews.

## Overview

The chat server implements core functionalities akin to the Internet Relay Chat (IRC) protocol, allowing multiple users to connect, join different channels, and exchange messages in real-time. The main functionalities implemented in accordance with the coursework specifications include:

- **NICK and USER Commands:** Allows users to set their nickname, username, and real name for registration.
- **JOIN and PART Commands:** Enables users to join or leave a channel, ensuring smooth communication among channel members.
- **PRIVMSG Command:** Facilitates messaging between users within a channel or privately to a specific user.
- **NAMES and LIST Commands:** Provides users with a list of channel members or all available channels.
- **TIME, INFO, and PING Commands:** Supports additional functionalities to retrieve server time, basic server information, and check the connection status.

## Usage

### Compiling and Running

To compile and run the server, use the following command:

```bash
javac *.java
java IrcServerMain <serverName> <port>
```

Replace `<serverName>` with the desired name for your server and `<port>` with a valid port number.

### Error Handling and Additional Features

- **Invalid Port Number:** Displays an error message when the port number is out of bounds or is a reserved number.
- **Command Validation:** Provides responses for invalid or unavailable commands entered by the client.
- **Unique Nickname Registration:** Ensures each nickname registered by clients is unique. A message is sent if a duplicate nickname is attempted.
- **Dynamic Nickname Modification:** Allows users to change their nickname while retaining their username and real name.
- **Prevents Duplicate Entry into a Channel:** Ensures a user cannot join a channel in which they are already present, preventing channel duplication.

## Future Improvements

Future enhancements may include:
- Implementing private message history.
- Adding support for multiple server instances.
- Developing a client-side application for a user-friendly chat experience.

## Author
Sathwic Krishna Jain

This README provides information on how to compile and run the server along with an overview of its features and potential improvements.
