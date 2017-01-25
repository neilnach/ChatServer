/**
 * <b> CS 180 - Project 4 - Chat Server Skeleton </b>
 * <p>
 * <p>
 * This is the skeleton code for the ChatServer Class. This is a private chat
 * server for you and your friends to communicate.
 *
 * @author Neil Nachnani nnachnan@purdue.edu
 * Tejaswi Kotekar tkotekar@purdue.edu
 * @version November 20th, 2015
 * @lab Neil - BR8
 * Tejaswi - LC2
 */
public class LaunchServer {
    /**
     * This main method is for testing purposes only.
     *
     * @param args - the command line arguments
     */


    public static void main(String[] args) {
// Create a ChatServer and start it
        (new ChatServer(new User[0], 200)).run();
    }

}
