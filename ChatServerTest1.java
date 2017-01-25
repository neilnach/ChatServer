import org.junit.Assert;
import org.junit.Test;

/**
 * Chat Server Tests for Project 04
 * Elliot Berman, 2015
 */

public class ChatServerTest1 {
    // <editor-fold desc="General server test">
    @Test // Error Code 11: Unknown Command Error - described action on Error Code Table
    public void testInvalidCommand() {
        ChatServer server = new ChatServer(new User[0], 100);
        String response = server.parseRequest("BLAHBLAHBLAH\t \r\n");
        Assert.assertTrue(response.matches("FAILURE\t11.*\r\n"));
    }

    @Test // Additionally, you should note how the request command is in all-caps
    public void testCapitalization() {
        ChatServer server = new ChatServer(new User[0], 100);
        String response = server.parseRequest("User-Login\troot\tcs180\r\n");
        Assert.assertTrue(response.matches("FAILURE\t11.*\r\n"));
    }
    // </editor-fold>

    // <editor-fold desc="Login tests">
    @Test
    // A default user is a user whose existence is hardcoded into the server and will exist in every instance of the server.
    public void testLoginRoot() {
        ChatServer server = new ChatServer(new User[0], 100);
        String response = server.parseRequest("USER-LOGIN\troot\tcs180\r\n");
        Assert.assertTrue(response.matches("SUCCESS\t\\d{4}\r\n"));
    }

    @Test
    public void testLoginRootDirectly() {
        ChatServer server = new ChatServer(new User[0], 100);
        String response = server.userLogin(new String[]{"USER-LOGIN", "root", "cs180"});
        Assert.assertTrue(response.matches("SUCCESS\t\\d{4}\r\n"));
    }

    @Test // Invoke Error Code 20
    public void testLoginNonExistentUser() {
        ChatServer server = new ChatServer(new User[0], 100);
        String response = server.parseRequest("USER-LOGIN\tblah\tblah\r\n");
        Assert.assertTrue(response.matches("FAILURE\t20\tUsername Lookup Error: .*\r\n"));
    }

    @Test // Invoke Error Code 21
    public void testLoginInvalidPassword() {
        ChatServer server = new ChatServer(new User[0], 100);
        String response = server.parseRequest("USER-LOGIN\troot\tblah\r\n");
        Assert.assertTrue(response.matches("FAILURE\t21\tAuthentication Error: .*\r\n"));
    }

    @Test // Invoke Error Code 10 with invalidly formatted USER-LOGIN
    public void testLoginInvalidFormat() {
        ChatServer server = new ChatServer(new User[0], 100);
        String response = server.parseRequest("USER-LOGIN\troot\tcs180\t\r\n");
        Assert.assertTrue(response.matches("FAILURE\t10\tFormat Command Error: .*\r\n"));
    }

    @Test // Invoke Error Code 25 by already being logged in
    public void testLoginAlreadyLoggedIn() {
        ChatServer server = new ChatServer(new User[0], 100);
        server.parseRequest("USER-LOGIN\troot\tcs180\r\n");
        String response = server.parseRequest("USER-LOGIN\troot\tcs180\r\n");
        Assert.assertTrue(response.matches("FAILURE\t25.*\r\n"));
    }

    @Test // receive the session cookie ID
    public void testLoginGeneratesUsableCookie() {
        ChatServer server = new ChatServer(new User[0], 100);
        String response = server.parseRequest("USER-LOGIN\troot\tcs180\r\n");
        Assert.assertTrue(response.matches("SUCCESS\t\\d{4}\r\n"));
        String cookie = response.substring(8, 12);
        response = server.parseRequest("ADD-USER\t" + cookie + "\tblah\tcs180\r\n");
        Assert.assertTrue(response.matches("SUCCESS\r\n"));
    }
    // </editor-fold>

    // <editor-fold desc="Add User Tests">
    @Test // This command will add a new user to the chat.
    public void testAddUser() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        String response = server.parseRequest("ADD-USER\t47\tusername\tpassword\r\n");
      Assert.assertTrue(response.matches("SUCCESS\r\n"));

        // if we were able to successfully log in, the user was added and can be logged into
        response = server.parseRequest("USER-LOGIN\tusername\tpassword\r\n");
        Assert.assertTrue(response.matches("SUCCESS\t\\d{4}\r\n"));
    }

    @Test // Direct of above test case
    public void testAddUserDirectly() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        String response = server.addUser(new String[]{"ADD-USER", "47", "username", "password"});
        Assert.assertTrue(response.matches("SUCCESS\r\n"));
    }

    @Test // You should note that only users who are logged in to the server can add users to the server.
    public void testAddUserInvalidAuth() {
        ChatServer server = new ChatServer(new User[0], 100);
        String response = server.parseRequest("ADD-USER\t42\tusername\tpassword\r\n");
        Assert.assertTrue(response.matches("FAILURE\t23.*\r\n"));
    }

    @Test // Usernames must be between 1 and 20 characters in length
    public void testAddUserShortUsername() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);

        String response = server.parseRequest("ADD-USER\t47\tp\tpassword\r\n");
        Assert.assertTrue(response.matches("SUCCESS\r\n"));

        // lower bound is caught by invalid format error
    }


    @Test // Usernames must be between 1 and 20 characters in length
    public void testAddUserFullLengthUsername() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);

        // Success on maximum length username
        String response = server.parseRequest("ADD-USER\t47\tABCDEFGHIJKLMNOPQRST\tpassword\r\n");
        Assert.assertTrue(response.matches("SUCCESS\r\n"));

        // Failure on maximum+1 length username
        response = server.parseRequest("ADD-USER\t47\tABCDEFGHIJKLMNOPQRSTU\tpassword\r\n");
        Assert.assertTrue(response.matches("FAILURE\t24.*\r\n"));
    }

    @Test // Usernames and passwords can only contain alphanumerical values [A-Za-z0-9].
    public void testAddUserSpecialCharactersInUsername() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);

        // Fail on special characters
        String response = server.parseRequest("ADD-USER\t47\tp-c@rd\tpassword\r\n");
        Assert.assertTrue(response.matches("FAILURE\t24.*\r\n"));
    }

    @Test // Usernames and passwords can only contain alphanumerical values [A-Za-z0-9].
    public void testAddUserFullCharacterSetUsername() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);

        // Success on full A-z0-9
        String response = server.parseRequest("ADD-USER\t47\tAz09username\tpassword\r\n");
        Assert.assertTrue(response.matches("SUCCESS\r\n"));
    }

    @Test // Password must be between 4 and 40 characters in length.
    public void testAddUserMinLengthPassword() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        String response = server.parseRequest("ADD-USER\t47\tusername\tpass\r\n");
        Assert.assertTrue(response.matches("SUCCESS\r\n"));

        response = server.parseRequest("ADD-USER\t47\tusername2\tpas\r\n");
        Assert.assertTrue(response.matches("FAILURE\t24.*\r\n"));
    }

    @Test // Password must be between 4 and 40 characters in length.
    public void testAddUserMaxLengthPassword() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        String response = server.parseRequest("ADD-USER\t47\tusername\tpasswordIsSuperDuperSecureAndHardToGuess\r\n");
        Assert.assertTrue(response.matches("SUCCESS\r\n"));

        response = server.parseRequest("ADD-USER\t47\tusername2\tpasswordIsSuperDuperSecureAndHardToGuess3\r\n");
        Assert.assertTrue(response.matches("FAILURE\t24.*\r\n"));
    }

    @Test // Usernames and passwords can only contain alphanumerical values [A-Za-z0-9].
    public void testAddUserAlphanumericPassword() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        String response = server.parseRequest("ADD-USER\t47\tusername\tAz09password\r\n");
        Assert.assertTrue(response.matches("SUCCESS\r\n"));
    }

    @Test // Usernames and passwords can only contain alphanumerical values [A-Za-z0-9].
    public void testAddUserSpecialCharacterPassword() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        String response = server.parseRequest("ADD-USER\t47\tusername\tp@ssw0rd\r\n");
        Assert.assertTrue(response.matches("FAILURE\t24.*\r\n"));
    }

    @Test // Invoke error code 22
    public void testAddUserAlreadyExists() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);

        String response = server.parseRequest("ADD-USER\t47\telliot\tpassword\r\n");
        Assert.assertTrue(response.matches("FAILURE\t22.*\r\n"));
    }

    @Test // It is required that the Server can handle between 10 and 100 users. (including the root user).
    public void testAddUser100Users() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);

        String response = server.parseRequest("USER-LOGIN\troot\tcs180\r\n");
        Assert.assertTrue(response.matches("SUCCESS\t\\d{4}\r\n"));

        for (int i = 0; i <= 98; i++) {
            response = server.parseRequest("ADD-USER\t47\tuser" + i + "\tpassword\r\n");
            Assert.assertEquals("SUCCESS\r\n", response);

            response = server.parseRequest("USER-LOGIN\tuser" + i + "\tpassword\r\n");
            Assert.assertTrue(response.matches("SUCCESS\t\\d{4}\r\n"));
        }
    }
    // </editor-fold>

    // <editor-fold desc="Post Message Tests">
    @Test
    public void testPostMessage() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        String response = server.parseRequest("POST-MESSAGE\t47\thello world\r\n");
        Assert.assertTrue(response.equals("SUCCESS\r\n"));

        response = server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Assert.assertTrue(response.equals("SUCCESS\t0000) elliot: hello world\r\n"));
    }

    @Test // The name variable is the username of the User sending the message.
    public void testPostMessageDirectly() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        String response = server.postMessage(new String[]{"POST-MESSAGE", "47", "hello world"}, "picard");
        Assert.assertTrue(response.equals("SUCCESS\r\n"));

        response = server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Assert.assertTrue(response.equals("SUCCESS\t0000) picard: hello world\r\n"));
    }
    // </editor-fold>

    // <editor-fold desc="Get Messages Tests">
    @Test
    // The request status and the list of messages are delimited by tab characters, just like how a client request is formatted.
    public void testGetMessages() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        server.parseRequest("POST-MESSAGE\t47\thello world\r\n");
        String response = server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Assert.assertTrue(response.equals("SUCCESS\t0000) elliot: hello world\r\n"));
    }

    @Test
    // The number of messages required can be higher than the number of available messages, the function returns as many as possible.
    // Messages should be listed in chronological order with the oldest messages at the beginning.
    public void testGetMessagesMaxOut() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        server.parseRequest("POST-MESSAGE\t47\thello world\r\n");
        server.parseRequest("POST-MESSAGE\t47\twelcome world\r\n");
        server.parseRequest("POST-MESSAGE\t47\thola world\r\n");
        server.parseRequest("POST-MESSAGE\t47\thi world\r\n");
        String response = server.parseRequest("GET-MESSAGES\t47\t5\r\n");
        Assert.assertTrue(response.equals("SUCCESS" + "\t0000) elliot: hello world" +
                "\t0001) elliot: welcome world" +
                "\t0002) elliot: hola world" +
                "\t0003) elliot: hi world\r\n"));
    }

    @Test
    // For the request to succeed, the number of messages requested must be >= 1, otherwise an INVALID_VALUE_ERROR (error #24) should be returned.
    public void testGetMessageInvalidNumbers() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        String response = server.parseRequest("GET-MESSAGES\t47\t0\r\n");
        Assert.assertTrue(response.matches("FAILURE\t24.*\r\n"));

        response = server.parseRequest("GET-MESSAGES\t47\t-1\r\n");
        Assert.assertTrue(response.matches("FAILURE\t24.*\r\n"));
    }

    @Test // It can also return 0 messages if none are available
    public void testGetMessagesNone() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        String response = server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Assert.assertTrue(response.matches("SUCCESS\r\n"));
    }

    @Test
    // The number of messages required can be higher than the number of available messages, the function returns as many as possible.
    public void testGetMessagesCircularBuffer() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 3);
        server.parseRequest("POST-MESSAGE\t47\thello world\r\n");
        server.parseRequest("POST-MESSAGE\t47\twelcome world\r\n");
        server.parseRequest("POST-MESSAGE\t47\thola world\r\n");
        server.parseRequest("POST-MESSAGE\t47\thi world\r\n");
        String response = server.parseRequest("GET-MESSAGES\t47\t5\r\n");
        Assert.assertTrue(response.equals("SUCCESS" + "\t0001) elliot: welcome world" +
                "\t0002) elliot: hola world" +
                "\t0003) elliot: hi world\r\n"));
    }

    @Test // The server can refuse to execute a request... (e.g. requesting -5 messages)
    public void testGetMessagesNegativeNumber() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        server.parseRequest("POST-MESSAGE\t47\thello world\r\n");
        server.parseRequest("POST-MESSAGE\t47\thello\r\n");
        String response = server.parseRequest("GET-MESSAGES\t47\t-5\r\n");
        Assert.assertTrue(response.matches("FAILURE\t24.*\r\n"));
    }

    @Test // Should return no more than the number of messages requested
    public void testGetMessagesLessThanMax() {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        server.parseRequest("POST-MESSAGE\t47\thello world\r\n");
        server.parseRequest("POST-MESSAGE\t47\tOne to beam up\r\n");
        String response = server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Assert.assertEquals("SUCCESS\t0001) elliot: One to beam up\r\n", response);
    }

    @Test
    public void testGetMessagesMultipleUsers() {
        ChatServer server = new ChatServer(new User[]{
                new User("elliot", "password", new SessionCookie(47)),
                new User("picard", "password", new SessionCookie(1701))
        }, 100);
        server.parseRequest("POST-MESSAGE\t47\thello world\r\n");
        server.parseRequest("POST-MESSAGE\t1701\tENGAGE!\r\n");
        String response = server.parseRequest("GET-MESSAGES\t47\t2\r\n");
        Assert.assertEquals("SUCCESS\t0000) elliot: hello world\t0001) picard: ENGAGE!\r\n", response);
    }
    // </editor-fold>

    // <editor-fold desc="Cookie Tests">
    @Test
    public void testCookieExpiration() throws InterruptedException {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        SessionCookie.timeoutLength = 1;
        Thread.sleep(1100);
        String response = server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Assert.assertTrue(response.matches("FAILURE\t5.*\r\n"));
    }

    @Test
    public void testCookieVoiding() throws InterruptedException {
        User userElliot = new User("elliot", "password", new SessionCookie(47));
        ChatServer server = new ChatServer(new User[]{userElliot}, 100);
        SessionCookie.timeoutLength = 1;
        Thread.sleep(500);
        server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Thread.sleep(600);
        String response = server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Assert.assertTrue(response.matches("FAILURE\t5.*\r\n"));
        Assert.assertNull(userElliot.getCookie());
        SessionCookie.timeoutLength = 300;
    }

    @Test
    public void testCookieUpdateOnAddUser() throws InterruptedException {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        SessionCookie.timeoutLength = 1;
        Thread.sleep(500);
        server.parseRequest("ADD-USER\t47\tusername\tenterprise\r\n");
        Thread.sleep(600);
        String response = server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Assert.assertTrue(response.matches("SUCCESS.*\r\n"));
        SessionCookie.timeoutLength = 300;
    }

    @Test
    public void testCookieUpdateOnPostMessage() throws InterruptedException {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        SessionCookie.timeoutLength = 1;
        Thread.sleep(500);
        server.parseRequest("POST-MESSAGE\t47\thello world\r\n");
        Thread.sleep(600);
        String response = server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Assert.assertTrue(response.matches("SUCCESS.*\r\n"));
        SessionCookie.timeoutLength = 300;
    }

    @Test
    public void testCookieSameOnGetMessage() throws InterruptedException {
        ChatServer server = new ChatServer(new User[]{new User("elliot", "password", new SessionCookie(47))}, 100);
        SessionCookie.timeoutLength = 1;
        Thread.sleep(500);
        server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Thread.sleep(600);
        String response = server.parseRequest("GET-MESSAGES\t47\t1\r\n");
        Assert.assertTrue(response.matches("FAILURE\t5.*\r\n"));
        SessionCookie.timeoutLength = 300;
    }
    // </editor-fold>

    // <editor-fold desc="SessionCookie Class Tests">
    @Test
    public void testTimeoutLengthExists() {
        SessionCookie.timeoutLength = 3;
        SessionCookie.timeoutLength = 300; // timeoutLength is defined
    }

    @Test
    public void testSessionCookieTimeout() throws InterruptedException {
        SessionCookie.timeoutLength = 1;
        SessionCookie cookie = new SessionCookie(1234);
        Assert.assertFalse(cookie.hasTimedOut());
        Thread.sleep(1200);
        Assert.assertTrue(cookie.hasTimedOut());
        SessionCookie.timeoutLength = 300;
    }

    @Test
    public void testSessionCookieUpdateTimeOfActivity() throws InterruptedException {
        SessionCookie.timeoutLength = 1;
        SessionCookie cookie = new SessionCookie(1234);
        Assert.assertFalse(cookie.hasTimedOut());
        Thread.sleep(500);
        cookie.updateTimeOfActivity();
        Thread.sleep(800); // cookie would expire if time hasn't been updated
        Assert.assertFalse(cookie.hasTimedOut());
        SessionCookie.timeoutLength = 300;
    }

    @Test
    public void testSessionCookieGetID() {
        SessionCookie cookie = new SessionCookie(1234);
        Assert.assertEquals(1234, cookie.getID());
    }
    // </editor-fold>

    // <editor-fold desc="User Class Tests">
    @Test
    public void testUserClassGetName() {
        User user = new User("elliot", "password", new SessionCookie(47));
        Assert.assertEquals("elliot", user.getName());
    }

    @Test
    public void testUserClassCheckPassword() {
        User user = new User("elliot", "password", new SessionCookie(47));
        Assert.assertTrue(user.checkPassword("password"));
        Assert.assertFalse(user.checkPassword("notMyPassword"));
    }

    @Test
    public void testUserClassGetCookie() {
        SessionCookie cookie = new SessionCookie(47);
        User user = new User("elliot", "password", cookie);
        Assert.assertEquals(cookie, user.getCookie());
    }

    @Test
    public void testUserClassSetCookie() {
        User user = new User("elliot", "password", new SessionCookie(47));
        user.setCookie(null);
        Assert.assertNull(user.getCookie());
    }
    // </editor-fold>

    // <editor-fold desc="Circular Buffer Class Tests">
    @Test
    public void testCircularBuffer() {
        CircularBuffer buffer = new CircularBuffer(3);
        buffer.put("hello world");
        Assert.assertEquals("0000) hello world",
                String.join(" ", buffer.getNewest(1)));
        buffer.put("hi");
        Assert.assertEquals("0000) hello world 0001) hi",
                String.join(" ", buffer.getNewest(2)));
        buffer.put("what's up");
        Assert.assertEquals("0000) hello world 0001) hi 0002) what's up",
                String.join(" ", buffer.getNewest(3)));
        buffer.put("goodbye world");
        Assert.assertEquals("0001) hi 0002) what's up 0003) goodbye world",
                String.join(" ", buffer.getNewest(4)));
    }

    @Test
    public void testCircularBufferGetNewestNull() {
        CircularBuffer buffer = new CircularBuffer(50);
        buffer.put("hello world");
        Assert.assertNull(buffer.getNewest(-5));
    }

    @Test
    public void testCircularBufferGetNewestMaxed() {
        CircularBuffer buffer = new CircularBuffer(50);
        buffer.put("hello world");
        buffer.put("hi");
        buffer.put("what's up");
        buffer.put("goodbye world");
        Assert.assertEquals("0000) hello world 0001) hi 0002) what's up 0003) goodbye world",
                String.join(" ", buffer.getNewest(10)));
    }

    @Test
    public void testCircularBufferGetNewestZero() {
        CircularBuffer buffer = new CircularBuffer(50);
        buffer.put("hello world");
        Assert.assertEquals("",
                String.join(" ", buffer.getNewest(0)));
    }

    @Test
    public void testCircularBufferEmptyStrings() {
        CircularBuffer buffer = new CircularBuffer(50);
        buffer.put("hello world");
        buffer.put("");
        buffer.put("goodbye world");
        Assert.assertEquals("0000) hello world 0001) goodbye world",
                String.join(" ", buffer.getNewest(4)));
    }
    // </editor-fold>
}