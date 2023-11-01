import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * The main class of IrcServerMain program.
 * This class creates a server with a port and awaits for a client connection. When a client connection is made,
 * a thread object is created and is started which does everything else.
 * The class accepts the arguments and throws exceptions for errors.
 * @author 220026989
 * @version - 1.0
 */
public class IrcServerMain {
    private static ServerSocket ss;
    private static final int MIN_PORT_NUMBER = 1024;  //port number 1 to 1023 are reserved for special purposes.
    /**
     * The main method takes in the server name and port number, which throws exceptions if not present.
     * The main method creates a socket to which a client can connect with the given port number. When the
     * connection is made a thread object is created that talks with the client directly, leaving the main
     * method free to connect with other clients.
     * @param args The input from the user.
     * @throws IOException when an I/O error happens waiting for the connection.
     */
    public static void main(String[] args) {
        try {
            String serverName = args[0];
            int port = Integer.parseInt(args[1]);
            if (port < MIN_PORT_NUMBER) {
                throw new IllegalArgumentException("Invalid port number: " + port);
            }
            ss = new ServerSocket(port);
            while (true) {
                Socket conn = ss.accept();
                ClientHandler t = new ClientHandler(conn, serverName);
                t.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) { //catching the error thrown when the required input is not recieved from the user.
            System.out.println("Usage: java IrcServerMain <server_name> <port>");
        } catch (NumberFormatException e) {  //catching the error thrown when the port is not an integer.
            System.out.println("Usage: java IrcServerMain <server_name> <port>");
        } catch (IllegalArgumentException e) { //catching the error thrown when the port value is out of range.
            System.out.println(e.getLocalizedMessage());
        }
    }
}
