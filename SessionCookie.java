/**
 * 
 */
public class SessionCookie {
    public static int timeoutLength = 300;
    private long id;
    private static long time;

    public SessionCookie(long id) {
        this.id = id;
        this.time = System.currentTimeMillis();
    }

    public long getID() {
        return this.id;
    }

    public SessionCookie() {
    }
    //method returns the ID of the cookie

    public boolean hasTimedOut() {
        if (System.currentTimeMillis() - SessionCookie.time > timeoutLength * 1000) {
            //changed this.time to SessionCookie.time
            return true;
        } else return false;

    }

    public void updateTimeOfActivity() {
        this.time = System.currentTimeMillis();

    } //method will update the cookie's time of last activity by setting it to the current time


}
