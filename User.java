/**
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
