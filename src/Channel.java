import java.util.ArrayList;
/**
 * A class that keeps track of the list of users of a specific channel.
 * The methods in this class has been defined according to the requirements of the ClientHandler
 * to take care of the channel requirements in the program.
 * @author 220026989
 * @version - 1.0
 */
public class Channel {
    private ArrayList<String> usersOfChannel = new ArrayList<String>();
    private String name;
    /**
     * Constructor of Channel class to initialize the parameters of its instance.
     * A specific Channel that does not exist, is only created when a user joins it. Therefore, the
     * constructor only takes on user to add to the list.
     * @param userToAdd The user to be added.
     * @param name The name of the channel that is created.
     */
    public Channel(String userToAdd, String name) {
        usersOfChannel.add(userToAdd);
        this.name = name;
    }

    /**
     * Method to add users to the list of users.
     * @param nickName The nick name of the user
     */
    public void addUser(String nickName) {
        usersOfChannel.add(nickName);
        //usersInChannel.replace(this, usersOfChannel);
    }

    /**
     * Method to remove a specific user from the list.
     * @param nickName The nick name of the user that has to be removed.
     */
    public void removeUser(String nickName) {
        usersOfChannel.remove(nickName);
    }

    /**
     * Method that checks if the number of users is zero.
     * @return Boolean according to the number of users in the channel.
     */
    public boolean ifZeroUsers() {
        return (usersOfChannel.size() == 0);
    }

    /**
     * Getter method for the channel name.
     * @return String name of the channel.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter method for the list of users.
     * @return ArrayList of the names of users of the given channel.
     */
    public ArrayList<String> getListOfUsers() {
        return this.usersOfChannel;
    }

    /**
     * Method to check if the given user exists.
     * @param nickName The nick name of the user to be checked.
     * @return Boolean value according to whether the user exists in the list of the given channel or not.
     */
    public boolean ifUserExists(String nickName) {
        return (usersOfChannel.contains(nickName));
    }
}
