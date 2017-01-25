/**
 * <b> CS 180 - Project 4 - Chat Server Skeleton </b>
 * <p>
 * <p>
 * This is the skeleton code for the ChatServer Class. This is a private chat
 * server for you and your friends to communicate.
 *
 * @author Neil Nachnani nnachnan@purdue.edu
 *         Tejaswi Kotekar tkotekar@purdue.edu
 * @version November 20th, 2015
 * @lab Neil - BR8
 * Tejaswi - LC2
 */
public class User {
    String userName;
    String password;
    SessionCookie cookie;

    public User(String userName, String password, SessionCookie cookie) {
        this.cookie = cookie;
        this.password = password;
        this.userName = userName;
    }

    public String getName() {
        return userName;
    }

    public boolean checkPassword(String password) {
        if (password.equals(this.password)) {
            return true;
        }
        return false;
    }

    public SessionCookie getCookie() {
        return cookie;
    }

    public void setCookie(SessionCookie cookie) {
        this.cookie = cookie;

    }
}
