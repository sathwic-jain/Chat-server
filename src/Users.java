/**
 * A class that has the features required for a user.
 * This class has been defined to be used by objects ClientHandler
 * for storing the details of a user.
 * @author 220026989
 * @version - 1.0
 */
public class Users {
    private String userName;
    private String realName;
    private Boolean registered;
    /**
     * Constructor for the User class.
     * Initializes the parameters with default values.
     */
    public Users() {
        this.userName = null;
        this.realName = null;
        this.registered = false;
    }
    /**
     * Getter method that returns the username.
     * @return String which contains the username of the specific user
     */
    public String getUserName() {
        return this.userName;
    }
    /**
     * Getter method returns the realname.
     * @return realname of the user
     */
    public String getRealName() {
        return this.realName;
    }
    /**
     * Setter method that resets/sets the values of each parameters of the User object.
     * @param userName The username to be set
     * @param realName The realname of the user that is to be set
     * @param registered Boolean value that is set according to whether the user is registered or not.
     */
    public void setUsers(String userName, String realName, Boolean registered) {
        this.userName = userName;
        this.realName = realName;
        this.registered = registered;
    }
    /**
     * Method that checks if the user is registered or not.
     * @return Boolean value that specifies whether the user is registered or not.
     */
    public boolean isRegistered() {
        return this.registered;
    }
}
