import java.net.Socket;
import java.time.LocalDateTime;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class that extends the Thread class.
 * This class handles the input and output from and to each client separately and works
 * parellelly with all the clients connected to the server.
 * @author 220026989
 * @version - 1.0
 */
public class ClientHandler extends Thread {
    private static HashMap<String, Users> users = new HashMap<String, Users>(); //connects the nickName with each User.
    private static HashMap<String, ClientHandler> registeredUsers = new HashMap<String, ClientHandler>(); //connects the nickName with the ClientHandler class.
    private static ArrayList<Channel> channelsAvailable = new ArrayList<Channel>(); //Arraylist to keep track of the channels available.
    private ArrayList<Channel> channelsOfTheUser = new ArrayList<Channel>(); //Arraylist that keeps track of the channel list this client has.
    private Socket conn;
    private String serverName;
    private String nickName;
    private Users userDetails = new Users();
    private Boolean quitClient = false;
    /**
     * Constructor for the ClientHandler.
     * @param conn The connection from the client.
     * @param serverName The name of the server the client has connected to.
     */
    public ClientHandler(Socket conn, String serverName) {
        this.conn = conn;
        this.serverName = serverName;
    }

    /**
     * This method is an overridden method of the Thread class which is called when the ClientHandler object.starts() is invoked.
     */
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(conn.getInputStream());
            BufferedReader in = new BufferedReader(isr);

            OutputStream output = conn.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            while (!quitClient) {
                String line = in.readLine();
                forEachLine(line, writer);
            }
        } catch (IOException e) {
                System.out.println(e.getMessage());
        }
    }

    /**
     * This method checks the command of the line that is passed into it and calls the required function accordingly.
     * @param line The Command Line entered by the client.
     * @param writer PrintWriter object that prints text to the output stream.
     */
    public void forEachLine(String line, PrintWriter writer) {
        String[] lineSplitOut = line.split(" ");
        switch (lineSplitOut[0]) {
            case "NICK" :
                setNickName(writer, line);
                break;
            case "USER" :
                toregister(writer, line);
                break;
            case "QUIT" :
                quit(writer);
                break;
            case "JOIN" :
                join(writer, line);
                break;
            case "PART" :
                part(writer, line);
                break;
            case "PRIVMSG" :
                privMsg(writer, line);
                break;
            case "NAMES" :
                names(writer, line);
                break;
            case "LIST" :
                list(writer);
                break;
            case "TIME" :
                writeTime(writer);
                break;
            case "INFO" :
                info(writer);
                break;
            case "PING" :
                ping(writer, line);
                break;
            default :
                toReplyInvalidMsg(writer);
        }
    }

//Method required for NICK command begins here.
    /**
     * Method called to set the nick name of the client connected.
     * @param writer PrintWriter object that prints text to the output stream.
     * @param lineGiven Each line input from the client.
     */
    public void setNickName(PrintWriter writer, String lineGiven) {
        try {
            String line = lineGiven.replaceFirst("NICK ", "");
                if (Pattern.matches("[[^0-9]&&[a-zA-Z_]]{1}[\\w]{0,8}", line)) {
                    //if the client has already given a nickName and wannts to change it.
                    if (nickName != null) {
                        if (!(users.putIfAbsent(line, users.get(nickName)) == null)) { //Checks if the nickName exists and adds the nickName if does not exist.
                            writer.println(":" + serverName + " 400 * :Nickname exists");
                        } else {
                            registeredUsers.put(line, registeredUsers.get(nickName)); //Switches the nickname key of the registeredUsers keeping the value.
                            users.remove(nickName);
                            nickName = line;
                        }
                    } else {
                        if (!(users.putIfAbsent(line, userDetails) == null)) {
                            writer.println(":" + serverName + " 400 * :Nickname exists");
                        } else {
                            nickName = line;
                        }
                    }
                } else {
                    writer.println(":" + serverName + " 400 * :Invalid nickname");
                }
        } catch (Exception e) { //error thrown if nickName is unable to write
            System.out.println("Unable to write the nickName");
        }
    }
//Methods required for the NICK command ends here.

//Methods required for the USER command begins here.
    /**
     * Method for registering.
     * Validates the username and realname to register the client.
     * @param writer PrintWriter object for printing text to the output stream.
     * @param lineGiven Single input line from the client.
     */
    public void toregister(PrintWriter writer, String lineGiven) {
                String inputLine = lineGiven;
                String realNameInput;
                String userNameInput;
                final int minNumberOfArgumentsProvided = 4;
                final int minNumberOfWordsRequiredWithRespectToColon = 2; //minimum number of strings required with respect to : character.
                String[] inputLineSplitOut = inputLine.split(" ");
                if (nickName != null) {
                    if (users.get(nickName).isRegistered()) {
                        writer.println(":" + serverName + " 400 * :You are already registered");
                    } else {
                        String[] commandSplitOut = inputLine.split(":");
                        if (inputLineSplitOut.length < minNumberOfArgumentsProvided || commandSplitOut.length < minNumberOfWordsRequiredWithRespectToColon) {
                            writer.println(":" + serverName + " 400 * :Not enough arguments");
                        } else if (!(Pattern.matches(("\\bUSER\\b [\\w]+ \\b0\\b \\* :.+"), lineGiven))) { //white spaces are taken as real name too.
                            writer.println(":" + serverName + " 400 * :Invalid arguments to USER command");
                        } else if (!(Pattern.matches("(\\S)*", inputLineSplitOut[1]))) {   //username should not have white spaces.
                            writer.println(":" + serverName + " 400 * :Invalid arguments to USER command");
                        } else {
                            userNameInput = inputLineSplitOut[1];
                            if (commandSplitOut.length > minNumberOfWordsRequiredWithRespectToColon) { //if the realname has : character, it has to be combined back again.
                                realNameInput = combineString(commandSplitOut); //The method is defined on the privMsg section.
                            } else {
                                realNameInput = commandSplitOut[1];
                            }
                            userDetails.setUsers(userNameInput, realNameInput, true);
                            registeredUsers.put(nickName, this);
                            users.replace(nickName, userDetails); //replacing the value of already existing key - nickName, in hashmap with the input values.
                            writer.println(":" + serverName + " 001 " + nickName + " :Welcome to the IRC network, " + nickName);
                            //registered(serverName, in, writer);
                        }
                    }
                } else {
                    writer.println(":" + serverName + " 400 * :Enter nickname first");
                }
    }
//Method required for the USER command ends here.

//Methods required for the quit command begins here.
    /**
     * Method invoked when a client quits from the server.
     * @param writer PrintWriter object that writes output to the client.
     */
    public void quit(PrintWriter writer) {
        try {
            if (nickName == null) {
                quitClient = true;
                conn.close();
            } else if (users.get(nickName).isRegistered()) {
                quitClient = true;
                registeredUsers.forEach((key, value) -> {
                try {
                    value.toPrintQuittingClient(nickName); //method that sends message to all the clients connected that a specific user is quitting.
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }});
                //removing the quitting from all channels
                for (Channel channelWithThisUser : channelsOfTheUser) {
                    channelWithThisUser.removeUser(nickName);
                    if (channelWithThisUser.ifZeroUsers()) {
                        channelsAvailable.remove(channelWithThisUser);
                    }
                }
                registeredUsers.remove(nickName);
                users.remove(nickName);
                conn.close();
            } else {
                quitClient = true;
                users.remove(nickName);
                conn.close();
            }
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    /**
     * Method that is invoked when a client quits from the server.
     * Used to send a message to all the registered users that the client is quitting.
     * @param nickName The nickname of the client that is quitting.
     * @throws IOException
     */
    public void toPrintQuittingClient(String nickName) throws IOException {
        try {
            OutputStream output = conn.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(":" + nickName + " QUIT");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
//Methods required for the QUIT command ends here.

//Method required for the JOIN command begins here.
    /**
     * Method for letting client join a specific channel.
     * @param writer PrintWriter method that writes output to the client.
     * @param givenLine Input line from the client connected.
     */
    public void join(PrintWriter writer, String givenLine) {
        String[] inputLineSplitOut = givenLine.split(" ");
        String channelToJoin;
        final int minNumberOfArgumentsProvidedIncludingCommand = 2;
        if (nickName == null) {
            writer.println(":" + serverName + " 400 * :You need to register first");
        } else if (!(users.get(nickName).isRegistered())) {
            writer.println(":" + serverName + " 400 * :You need to register first");
        } else if (inputLineSplitOut.length == minNumberOfArgumentsProvidedIncludingCommand) {
            channelToJoin = inputLineSplitOut[1];
            if (Pattern.matches("^#([A-Za-z0-9_])*", channelToJoin)) {
                Channel existingChannel = null;
                existingChannel = getChannelFromChannelName(channelToJoin, existingChannel); //invoking a method to get the name of the channel from its name.
                if (existingChannel != null) {
                    if (!(existingChannel.ifUserExists(nickName))) {
                        existingChannel.addUser(nickName);
                        channelsOfTheUser.add(existingChannel);
                        gettingEachUserForNotifyingJoin(nickName, existingChannel); //invoking a method to notify all the users of the joinee
                    } else {
                    writer.println(":" + serverName + " 400 * :You already exist in this channel");  //taking out user duplication in a channel.
                    }
                } else {
                    Channel newChannel = new Channel(nickName, channelToJoin);
                    channelsAvailable.add(newChannel);
                    channelsOfTheUser.add(newChannel);
                    gettingEachUserForNotifyingJoin(nickName, newChannel);
                }
            } else {
                writer.println(":" + serverName + " 400 * :Invalid channel name");
            }
        } else {
            writer.println(":" + serverName + " 400 * :Invalid arguments to JOIN command");
        }
    }

    /**
     * Method that returns Channel object given its channel name parameter.
     * @param channelToFind String - name of the channel.
     * @param existingChannel Channel object that is to be returned which is initially null.
     * @return Channel object if there exists a channel with the given channel name, and null if there does not exist one.
     * Here instead of creating a channel and then return it, we send the channel object that we return.
     */
    public Channel getChannelFromChannelName(String channelToFind, Channel existingChannel) {
         for (Channel eachChannel : channelsAvailable) {
            if (eachChannel.getName().equals(channelToFind)) { //Since it is inside a for loop the else condition could be confusing if used here.
                existingChannel = eachChannel; //Instead of creating a flag to check if the channel exists, it is assigned to existingChannel and returned for simplicity.
                break;
            }
         }
    return existingChannel;
    }

    /**
     * Method that fetches each user of the given channel for notifying that a new member has joined in.
     * @param joineeNickName String - Nick name of the client joining in.
     * @param joiningChannel Channel object to which the user joins.
     */
    public void gettingEachUserForNotifyingJoin(String joineeNickName, Channel joiningChannel) {
        ArrayList<String> listOfUsers = joiningChannel.getListOfUsers();
        for (String eachNickName : listOfUsers) {
            try {
                (registeredUsers.get(eachNickName)).toPrintJoiningChannel(joineeNickName, joiningChannel);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Method that prints the message of joining to each user of the given channel.
     * @param joineeNickName String - nick name of the joining client.
     * @param joiningChannel Channel object to which the client is joining.
     * @throws IOException is thrown when an error occurs while creating the output stream or when socket connection fails.
     */
    public void toPrintJoiningChannel(String joineeNickName, Channel joiningChannel) throws IOException {
        OutputStream output = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        writer.println(":" + joineeNickName + " JOIN " + joiningChannel.getName());
    }
// Methods required for JOIN command ends here

//Method required for PART command starts here
    /**
     * Method for letting a given client quit from a specific channel only.
     * @param writer PrintWriter object that prints a line to the client.
     * @param givenLine String - input line from the client.
     */
    public void part(PrintWriter writer, String givenLine) {
        String[] inputLineSplitOut = givenLine.split(" ");
        String channelName;
        final int minNumberOfArgumentsProvidedIncludingCommand = 2;
        if (nickName == null) {
            writer.println(":" + serverName + " 400 * :You need to register first");
        } else if (!(users.get(nickName).isRegistered())) {
            writer.println(":" + serverName + " 400 * :You need to register first");
        } else if (inputLineSplitOut.length != minNumberOfArgumentsProvidedIncludingCommand) {  //The command should exactly contain only 2 parts
            writer.println(":" + serverName + " 400 * :Invalid arguments to PART command");
        } else {
            channelName = inputLineSplitOut[1];
            Channel existingChannel = null;
            existingChannel = getChannelFromChannelName(channelName, existingChannel);
            if (existingChannel == null) {
                writer.println(":" + serverName + " 400 " + nickName + " :No channel exists with that name");
            } else if (!(existingChannel.ifUserExists(nickName))) {
                writer.println(":" + serverName + " 400 * :You do not exist in this channel");
            } else {
                gettingEachUserForNotifyingParting(existingChannel); //Method for getting all the users of the channel for notification.
                channelsOfTheUser.remove(existingChannel);
                existingChannel.removeUser(nickName);
            }
        }
   }

   /**
    * Method for getting the users of a specific channel to notify that the given client is leaving.
    * @param existingChannel The channel object from which the client is quitting.
    */
   public void gettingEachUserForNotifyingParting(Channel existingChannel) {
      ArrayList<String> listOfUsers = existingChannel.getListOfUsers();
      for (String eachNickName : listOfUsers) {
          try {
              (registeredUsers.get(eachNickName)).toPrintPartingChannel(nickName, existingChannel); //method for printing the message to all the users.
            } catch (IOException e) {
            System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Method for printing parting message.
     * @param partingNickName String - nick name of the parting client.
     * @param partingChannel Channel object from which the user is quiting.
     * @throws IOException is thrown when an error occurs while creating the output stream or when socket connection fails.
     */
    public void toPrintPartingChannel(String partingNickName, Channel partingChannel) throws IOException {
        OutputStream output = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        writer.println(":" + partingNickName + " PART " + partingChannel.getName());
    }
//Methods for implementing PART command ends here

//Methods for implementing PRIVMSG commands begin here
    /**
     * Method that lets the given client send a private message to another client or to a channel.
     * @param writer PrintWriter object that helps write message to the clients.
     * @param givenLine String - input line received by the server.
     */
    public void privMsg(PrintWriter writer, String givenLine) {
        final int minNumberOfArgumentsProvided = 3;
        final int minNumberOfWordsRequiredWithRespectToColon = 2; //minimum number of strings required with respect to : character.
        if (nickName == null) {
            writer.println(":" + serverName + " 400 * :You need to register first");
        } else if (!(users.get(nickName).isRegistered())) {
            writer.println(":" + serverName + " 400 * :You need to register first");
        } else {
            String[] inputLineSplitOut = givenLine.split(" ");
            String[] commandLineSplitOut = givenLine.split(":");
            if ((inputLineSplitOut.length >= minNumberOfArgumentsProvided)) { //the input command should have atleast 3 parts(splitting them by space)
                String[] toCheckCommandArguments = commandLineSplitOut[0].split(" "); //for checking if the <target> has space or not
                if (toCheckCommandArguments.length != minNumberOfWordsRequiredWithRespectToColon) {
                    writer.println(":" + serverName + " 400 *  :Invalid arguments to PRIVMSG command"); //the command syntax should only have 2 strings apart from the message.
                } else if (commandLineSplitOut.length < minNumberOfWordsRequiredWithRespectToColon) {
                    writer.println(":" + serverName + " 400 *  :Invalid arguments to PRIVMSG command"); //there should be a message.
                } else {
                    if (commandLineSplitOut.length > minNumberOfWordsRequiredWithRespectToColon) { //this is for the case where there are : characters in the message
                        String messageLine = combineString(commandLineSplitOut);
                        sendMessage(messageLine, inputLineSplitOut[1], writer);
                    } else {
                        sendMessage(commandLineSplitOut[1], inputLineSplitOut[1], writer); //incase there are no : characters in the mnessage
                    }
                }
            } else if (inputLineSplitOut.length < minNumberOfArgumentsProvided) {
                writer.println(":" + serverName + " 400 *  :Invalid arguments to PRIVMSG command");
            }
        }
    }

    /**
     * Method that combines strings.
     * Used specifically to join  sentences that could be separated by : character that was split apart for strict verification of the command syntax.
     * @param commandLineSplitOut Array of String objects - Contains each word of the recieved line split apart with respect to : character
     * @return String - combined with : character
     */
    public String combineString(String[] commandLineSplitOut) {
        String combinedString = commandLineSplitOut[1];
        for (int i = 2; i < commandLineSplitOut.length; i++) {
            combinedString = combinedString + ":" + commandLineSplitOut[i];
        }
        return combinedString;
    }

    /**
     * Method to send the message from the given client to the required target.
     * @param messageLine String - The message line required to be send to the target.
     * @param receiver String - the target that should receive the message.
     * @param writer PrintWriter object that helps write the message to the client.
     */
    public void sendMessage(String messageLine, String receiver, PrintWriter writer) {
        if (Pattern.matches("^#([A-Za-z0-9_])*", receiver)) { //checking if the target name matches the name of the channels.
            int flag = 0; //flag is created as to know whether the target channel is in the available channel objects.
            for (Channel eachChannel : channelsAvailable) {
                if (eachChannel.getName().equals(receiver)) {
                    flag = 1;
                    gettingEachUserForMessagingChannel(messageLine, eachChannel);
                }
            }
            if (flag == 0) {
                writer.println(":" + serverName + " 400 *   :No channel exists with that name");
            }
        } else if (Pattern.matches("[[^0-9]&&[a-zA-Z_]]{1}[\\w]{0,8}", receiver)) {  //checking if the target name matches the name of any clients.
            if (registeredUsers.containsKey(receiver)) {
                registeredUsers.get(receiver).sendMessageToUser(nickName, messageLine, receiver);
            } else {
                writer.println(":" + serverName + " 400 *   :No user exists with that name");
            }
        } else {
            writer.println(":" + serverName + " 400 *  :Invalid arguments to PRIVMSG command");
        }
    }

    /**
     * Method for getting all the users.
     * @param messageLine String - the message that has to be sent to all the users.
     * @param existingChannel Channel object - The existing channel to which the message has to be sent.
     */
    public void gettingEachUserForMessagingChannel(String messageLine, Channel existingChannel) {
        ArrayList<String> listOfUsers = existingChannel.getListOfUsers();
        for (String eachNickName : listOfUsers) {
                try {
                    (registeredUsers.get(eachNickName)).toPrintMessage(nickName, messageLine, existingChannel);
                } catch (IOException e) {
                System.out.println(e.getMessage());
                }
          }
      }

    /**
     * Method to print message to all the users of the given channel.
     * @param senderName String - nick name of the client who is sending the message.
     * @param messageLine String - the message that the sender client has given.
     * @param targetChannel Channel Object - the target channel to which the message is directed.
     * @throws IOException is thrown when an error occurs while creating the output stream or when socket connection fails.
     */
    public void toPrintMessage(String senderName, String messageLine, Channel targetChannel) throws IOException {
        OutputStream output = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        writer.println(":" + senderName + " PRIVMSG " + targetChannel.getName() + " :" + messageLine);
    }

    /**
     * Method to send the message to a target user.
     * @param senderName String - nick name of the client who is sending the message.
     * @param messageLine String - the message that the sender client has given.
     * @param receiver String - the target client to which the message is directed.
     */
    public void sendMessageToUser(String senderName, String messageLine, String receiver) {
        try {
            OutputStream output = conn.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(":" + senderName + " PRIVMSG " + nickName + " :" + messageLine);
        }  catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
//Methods required to implement PRIVMSG command ends here.

//Methods required to implement NAMES start here.
    /**
     * Method to get the name of all the users in a specific channel.
     * @param writer PrintWriter object - that helps write the message to the client.
     * @param givenLine String - single line input recieved from the client.
     */
    public void names(PrintWriter writer, String givenLine) {
        if (nickName == null) {
            writer.println(":" + serverName + " 400 * :You need to register first");
        } else if (!(users.get(nickName).isRegistered())) {
            writer.println(":" + serverName + " 400 * :You need to register first");
        } else {
            String[] inputLineSplitOut = givenLine.split(" ");
            String channelName = inputLineSplitOut[1];
            if (Pattern.matches("^#([A-Za-z0-9_])*", channelName)) {
                int flag = 0;
                for (Channel eachChannel : channelsAvailable) {
                    if (eachChannel.getName().equals(channelName)) {
                        flag = 1;
                        toPrintUsersOfChannel(writer, eachChannel); //Method for printing all the users of the channel.
                    }
                }
                if (flag == 0) {
                    writer.println(":" + serverName + " 400 *   :No channel exists with that name");
                }
            } else {
                writer.println(":" + serverName + " 400 *   :No channel exists with that name");
            }

        }
    }

    /**
     * Method for printing all the users of the channel.
     * @param writer PrintWriter object - that helps write the message to the client.
     * @param givenChannel Channel object - the channel whose users are to be printed.
     */
    public void toPrintUsersOfChannel(PrintWriter writer, Channel givenChannel) {
        ArrayList<String> usersOfThisChannel = givenChannel.getListOfUsers();
        String allUsersForDisplay = " ";
        for (String eachUser : usersOfThisChannel) {
            allUsersForDisplay = allUsersForDisplay + " " + eachUser;
        }
        allUsersForDisplay = allUsersForDisplay.trim();
        writer.println(":" + serverName + " 353 " +  nickName + " = " + givenChannel.getName() + " :" + allUsersForDisplay);
    }
//Methods to implement NAMES command ends here

//Method to implement LIST command begins here
    /**
     * Method to get the list of channels existing in the server.
     * @param writer PrintWriter object - that helps write the message to the client.
     */
    public void list(PrintWriter writer) {
        if (nickName == null) {
            writer.println(":" + serverName + " 400 * :You need to register first");
        } else if (!(users.get(nickName).isRegistered())) {
            writer.println(":" + serverName + " 400 * :You need to register first");
        } else {
            for (Channel eachChannelExisting : channelsAvailable) {
                writer.println(":" + serverName + " 322 " + nickName + " " + eachChannelExisting.getName());
            }
            writer.println(":" + serverName + " 323 " + nickName + " " + ":End of LIST");
        }
    }
//Method to implement LIST command ends here

    /**
     * Method to write time to the client.
     * @param writer PrintWriter object - that helps write the message to the client.
     */
    public void writeTime(PrintWriter writer) {
        LocalDateTime timeNow = LocalDateTime.now();
        writer.println(":" + serverName + " 391 " + "* " + ":" + timeNow);
    }

    /**
     * Method to print INFO.
     * @param writer PrintWriter object - that helps write the message to the client.
     */
    public void info(PrintWriter writer) {
        String message = "A basic IRC server written by a desparate and sleep deprived student.";
        writer.println(":" + serverName + " 371 " + "* " + ":" + message);
    }

    /**
     * Method for replying for a PING from the client side.
     * @param writer PrintWriter object - that helps write the message to the client.
     * @param givenLine String - Line received by the server.
     */
    public void ping(PrintWriter writer, String givenLine) {
        String line = givenLine.replaceFirst("PING ", "");
        writer.println("PONG " + line);
    }

    /**
     * Method that take cares of COMMANDS that are not yet defined for the IRC server.
     * @param writer PrintWriter object - that helps write the message to the client.
     */
    public void toReplyInvalidMsg(PrintWriter writer) {
        writer.println(":" + serverName + " 400 * :Invalid command");
    }
}
